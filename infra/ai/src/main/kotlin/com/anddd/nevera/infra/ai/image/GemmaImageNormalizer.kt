package com.anddd.nevera.infra.ai.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import com.anddd.nevera.infra.ai.performance.GemmaImageAnalysisTrace
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

internal class GemmaImageNormalizer(
    private val context: Context,
    private val outputDir: File,
) {

    fun normalize(
        imageUri: String,
        trace: GemmaImageAnalysisTrace? = null,
    ): File? {
        val preprocessStartedAtNs = System.nanoTime()
        return try {
            outputDir.mkdirs()
            val uri = imageUri.toUri()
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            var targetWidth = 0
            var targetHeight = 0
            var sourceWidth = 0
            var sourceHeight = 0

            val bitmap = measure(trace, "imageDecode") {
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val size = info.size
                    sourceWidth = size.width
                    sourceHeight = size.height
                    val scale = minOf(1f, MAX_LONG_EDGE.toFloat() / max(size.width, size.height))
                    targetWidth = (size.width * scale).roundToInt().coerceAtLeast(1)
                    targetHeight = (size.height * scale).roundToInt().coerceAtLeast(1)
                    decoder.setTargetSize(targetWidth, targetHeight)
                }
            }

            val compressionResult = try {
                compressToJpeg(bitmap, targetWidth, targetHeight)
            } finally {
                bitmap.recycle()
            }
            trace?.record(
                stage = "imageResize",
                elapsedMs = compressionResult.resizeElapsedMs,
                detail = "source=${sourceWidth}x${sourceHeight} target=${targetWidth}x${targetHeight} " +
                    "final=${compressionResult.width}x${compressionResult.height}",
            )

            val outFile = File.createTempFile("gemma_input_", ".jpg", outputDir)
            outFile.writeBytes(compressionResult.bytes)
            trace?.value("normalizedImageBytes", compressionResult.bytes.size)
            outFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to normalize image: $imageUri")
            null
        } finally {
            trace?.record("imagePreprocess", GemmaImageAnalysisTrace.elapsedMs(preprocessStartedAtNs))
        }
    }

    private fun compressToJpeg(bitmap: Bitmap, initialWidth: Int, initialHeight: Int): JpegCompressionResult {
        var currentBitmap = bitmap
        var width = initialWidth
        var height = initialHeight
        var quality = INITIAL_QUALITY
        var resizeElapsedMs = 0L

        try {
            while (true) {
                val bytes = currentBitmap.toJpegBytes(quality)
                if (bytes.size <= MAX_BYTES) {
                    return JpegCompressionResult(
                        bytes = bytes,
                        width = width,
                        height = height,
                        resizeElapsedMs = resizeElapsedMs,
                    )
                }

                if (quality > MIN_QUALITY) {
                    quality = max(MIN_QUALITY, quality - QUALITY_STEP)
                    continue
                }

                width = (width * RESIZE_SCALE).roundToInt().coerceAtLeast(1)
                height = (height * RESIZE_SCALE).roundToInt().coerceAtLeast(1)
                val resizeStartedAtNs = System.nanoTime()
                val resized = Bitmap.createScaledBitmap(currentBitmap, width, height, true)
                resizeElapsedMs += GemmaImageAnalysisTrace.elapsedMs(resizeStartedAtNs)
                if (resized !== currentBitmap) {
                    if (currentBitmap !== bitmap) currentBitmap.recycle()
                    currentBitmap = resized
                }
                quality = INITIAL_QUALITY
            }
        } finally {
            if (currentBitmap !== bitmap) currentBitmap.recycle()
        }
    }

    private fun Bitmap.toJpegBytes(quality: Int): ByteArray =
        ByteArrayOutputStream().use { out ->
            compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }

    private data class JpegCompressionResult(
        val bytes: ByteArray,
        val width: Int,
        val height: Int,
        val resizeElapsedMs: Long,
    )

    companion object {
        private const val MAX_LONG_EDGE = 1_280
        private const val MAX_BYTES = 2 * 1_024 * 1_024
        private const val INITIAL_QUALITY = 88
        private const val MIN_QUALITY = 60
        private const val QUALITY_STEP = 8
        private const val RESIZE_SCALE = 0.85f

        private fun <T> measure(
            trace: GemmaImageAnalysisTrace?,
            stage: String,
            block: () -> T,
        ): T = trace?.measure(stage, block) ?: block()
    }
}
