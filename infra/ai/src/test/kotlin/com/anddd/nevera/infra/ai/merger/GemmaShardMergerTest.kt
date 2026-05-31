package com.anddd.nevera.infra.ai.merger

import com.anddd.nevera.domain.model.ai.GemmaModelError
import com.anddd.nevera.domain.model.ai.GemmaModelState
import com.anddd.nevera.infra.ai.GemmaAiPackConstants
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class GemmaShardMergerTest {

    private lateinit var tmpDir: File
    private lateinit var merger: GemmaShardMerger

    @BeforeEach
    fun setUp() {
        tmpDir = Files.createTempDirectory("gemma_test").toFile()
        merger = GemmaShardMerger(tmpDir)
    }

    @AfterEach
    fun tearDown() {
        tmpDir.deleteRecursively()
    }

    @Test
    fun `모든 shard가 있을 때 merge가 성공하면 Ready를 반환한다`() {
        val content1 = ByteArray(100) { it.toByte() }
        val content2 = ByteArray(100) { (it + 100).toByte() }
        val shard1 = File(tmpDir, "part01").apply { writeBytes(content1) }
        val shard2 = File(tmpDir, "part02").apply { writeBytes(content2) }

        val result = merger.merge(listOf(shard1, shard2))

        assertTrue(result is GemmaModelState.Ready)
        val modelFile = File(tmpDir, GemmaAiPackConstants.MODEL_FILE_NAME)
        assertTrue(modelFile.exists())
        assertEquals(200, modelFile.length())
    }

    @Test
    fun `shard 파일이 없으면 MissingShard를 반환한다`() {
        val missingShard = File(tmpDir, "nonexistent_part01")

        val result = merger.merge(listOf(missingShard))

        assertEquals(GemmaModelState.Failed(GemmaModelError.MissingShard), result)
    }

    @Test
    fun `merge 성공 후 isModelReady는 true를 반환한다`() {
        val shard = File(tmpDir, "part01").apply { writeBytes(ByteArray(50) { it.toByte() }) }
        merger.merge(listOf(shard))

        assertTrue(merger.isModelReady())
    }

    @Test
    fun `모델 파일이 없으면 isModelReady는 false를 반환한다`() {
        assertFalse(merger.isModelReady())
    }

    @Test
    fun `모델 파일이 비어있으면 isModelReady는 false를 반환한다`() {
        File(tmpDir, GemmaAiPackConstants.MODEL_FILE_NAME).createNewFile()

        assertFalse(merger.isModelReady())
    }

    @Test
    fun `merge 성공 후 modelPath는 올바른 경로를 반환한다`() {
        val shard = File(tmpDir, "part01").apply { writeBytes(ByteArray(10) { it.toByte() }) }
        merger.merge(listOf(shard))

        val expectedPath = File(tmpDir, GemmaAiPackConstants.MODEL_FILE_NAME).absolutePath
        assertEquals(expectedPath, merger.modelPath())
    }

    @Test
    fun `tmp 파일은 merge 성공 후 남아있지 않아야 한다`() {
        val shard = File(tmpDir, "part01").apply { writeBytes(ByteArray(10) { it.toByte() }) }
        merger.merge(listOf(shard))

        val tmpFile = File(tmpDir, "${GemmaAiPackConstants.MODEL_FILE_NAME}.tmp")
        assertFalse(tmpFile.exists())
    }
}
