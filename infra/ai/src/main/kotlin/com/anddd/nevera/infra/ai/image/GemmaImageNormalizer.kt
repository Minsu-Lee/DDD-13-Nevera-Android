package com.anddd.nevera.infra.ai.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

internal class GemmaImageNormalizer(
    private val context: Context,
    private val outputDir: File,
) {

    fun normalize(imageUri: String): File? {
        return try {
            outputDir.mkdirs()
            val uri = imageUri.toUri()
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            var targetWidth = 0
            var targetHeight = 0

            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val size = info.size
                val scale = minOf(1f, MAX_LONG_EDGE.toFloat() / max(size.width, size.height))
                targetWidth = (size.width * scale).roundToInt().coerceAtLeast(1)
                targetHeight = (size.height * scale).roundToInt().coerceAtLeast(1)
                decoder.setTargetSize(targetWidth, targetHeight)
            }

            val jpegBytes = try {
                compressToJpeg(bitmap, targetWidth, targetHeight)
            } finally {
                bitmap.recycle()
            }

            val outFile = File(outputDir, "gemma_input_${System.currentTimeMillis()}.jpg")
            outFile.writeBytes(jpegBytes)
            outFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to normalize image: $imageUri")
            null
        }
    }

    private fun compressToJpeg(bitmap: Bitmap, initialWidth: Int, initialHeight: Int): ByteArray {
        var currentBitmap = bitmap
        var width = initialWidth
        var height = initialHeight
        var quality = INITIAL_QUALITY

        try {
            while (true) {
                val bytes = currentBitmap.toJpegBytes(quality)
                if (bytes.size <= MAX_BYTES) return bytes

                if (quality > MIN_QUALITY) {
                    quality = max(MIN_QUALITY, quality - QUALITY_STEP)
                    continue
                }

                width = (width * RESIZE_SCALE).roundToInt().coerceAtLeast(1)
                height = (height * RESIZE_SCALE).roundToInt().coerceAtLeast(1)
                val resized = Bitmap.createScaledBitmap(currentBitmap, width, height, true)
                if (currentBitmap !== bitmap) currentBitmap.recycle()
                currentBitmap = resized
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

    companion object {
        private const val MAX_LONG_EDGE = 1_600
        private const val MAX_BYTES = 1_500 * 1_024
        private const val INITIAL_QUALITY = 88
        private const val MIN_QUALITY = 60
        private const val QUALITY_STEP = 8
        private const val RESIZE_SCALE = 0.85f
    }
}
