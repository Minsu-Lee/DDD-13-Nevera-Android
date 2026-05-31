package com.anddd.nevera.infra.ai.merger

import com.anddd.nevera.domain.model.ai.GemmaModelError
import com.anddd.nevera.domain.model.ai.GemmaModelState
import com.anddd.nevera.infra.ai.GemmaAiPackConstants
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

internal class GemmaShardMerger(
    private val outputDir: File,
) {
    private val modelFile = File(outputDir, GemmaAiPackConstants.MODEL_FILE_NAME)
    private val tmpFile = File(outputDir, "${GemmaAiPackConstants.MODEL_FILE_NAME}.tmp")

    fun isModelReady(): Boolean {
        if (!modelFile.exists() || modelFile.length() == 0L) return false
        val expectedSha = GemmaAiPackConstants.EXPECTED_FULL_SHA256 ?: return true
        return sha256(modelFile) == expectedSha
    }

    fun modelPath(): String = modelFile.absolutePath

    fun merge(shardFiles: List<File>): GemmaModelState {
        outputDir.mkdirs()
        tmpFile.delete()

        try {
            tmpFile.outputStream().buffered().use { out ->
                shardFiles.forEachIndexed { index, shard ->
                    if (!shard.exists()) {
                        Timber.e("Shard missing: ${shard.absolutePath}")
                        return GemmaModelState.Failed(GemmaModelError.MissingShard)
                    }
                    val expectedSha = GemmaAiPackConstants.EXPECTED_PART_SHA256.getOrNull(index)
                    if (expectedSha != null && sha256(shard) != expectedSha) {
                        Timber.e("Checksum mismatch for shard $index")
                        tmpFile.delete()
                        return GemmaModelState.Failed(GemmaModelError.ChecksumMismatch)
                    }
                    shard.inputStream().buffered().use { it.copyTo(out) }
                }
            }

            val expectedFullSha = GemmaAiPackConstants.EXPECTED_FULL_SHA256
            if (expectedFullSha != null && sha256(tmpFile) != expectedFullSha) {
                Timber.e("Full model checksum mismatch after merge")
                tmpFile.delete()
                return GemmaModelState.Failed(GemmaModelError.ChecksumMismatch)
            }

            if (!tmpFile.renameTo(modelFile)) {
                Timber.e("Failed to rename tmp model file")
                tmpFile.delete()
                return GemmaModelState.Failed(GemmaModelError.MergeFailed)
            }

            Timber.d("Model merge successful: ${modelFile.absolutePath}")
            return GemmaModelState.Ready(modelFile.absolutePath)
        } catch (e: Exception) {
            Timber.e(e, "Exception during shard merge")
            tmpFile.delete()
            return GemmaModelState.Failed(GemmaModelError.MergeFailed)
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
