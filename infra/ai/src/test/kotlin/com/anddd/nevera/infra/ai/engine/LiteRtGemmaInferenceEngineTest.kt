package com.anddd.nevera.infra.ai.engine

import com.anddd.nevera.domain.model.ai.GemmaGenerationError
import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaImagePromptRequest
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import com.anddd.nevera.domain.usecase.ai.GetGemmaModelPathUseCase
import com.anddd.nevera.infra.ai.image.GemmaImageNormalizer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class LiteRtGemmaInferenceEngineTest {

    private lateinit var getGemmaModelPath: GetGemmaModelPathUseCase
    private lateinit var imageNormalizer: GemmaImageNormalizer
    private lateinit var engine: LiteRtGemmaInferenceEngine

    @BeforeEach
    fun setUp() {
        getGemmaModelPath = mockk()
        imageNormalizer = mockk()
        engine = LiteRtGemmaInferenceEngine(
            getGemmaModelPath = getGemmaModelPath,
            imageNormalizer = imageNormalizer,
            cacheDir = File(System.getProperty("java.io.tmpdir"), "gemma_test"),
        )
    }

    @Test
    fun `modelPath가 null이면 generateText는 ModelNotReady를 emit한다`() = runTest {
        coEvery { getGemmaModelPath() } returns null

        val events = engine.generateText(GemmaPromptRequest(prompt = "테스트")).toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is GemmaGenerationEvent.Failed)
        assertEquals(
            GemmaGenerationError.ModelNotReady,
            (events[0] as GemmaGenerationEvent.Failed).error,
        )
    }

    @Test
    fun `blank prompt이면 generateText는 EmptyPrompt를 emit한다`() = runTest {
        val events = engine.generateText(GemmaPromptRequest(prompt = "   ")).toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is GemmaGenerationEvent.Failed)
        assertEquals(
            GemmaGenerationError.EmptyPrompt,
            (events[0] as GemmaGenerationEvent.Failed).error,
        )
    }

    @Test
    fun `modelPath가 null이면 analyzeImage는 ModelNotReady를 emit한다`() = runTest {
        coEvery { getGemmaModelPath() } returns null

        val events = engine.analyzeImage(
            GemmaImagePromptRequest(imageUri = "content://test", prompt = "분석"),
        ).toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is GemmaGenerationEvent.Failed)
        assertEquals(
            GemmaGenerationError.ModelNotReady,
            (events[0] as GemmaGenerationEvent.Failed).error,
        )
    }

    @Test
    fun `blank prompt이면 analyzeImage는 EmptyPrompt를 emit한다`() = runTest {
        val events = engine.analyzeImage(
            GemmaImagePromptRequest(imageUri = "content://test", prompt = ""),
        ).toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is GemmaGenerationEvent.Failed)
        assertEquals(
            GemmaGenerationError.EmptyPrompt,
            (events[0] as GemmaGenerationEvent.Failed).error,
        )
    }

    @Test
    fun `imageNormalizer가 null을 반환하면 analyzeImage는 ImageReadFailed를 emit한다`() = runTest {
        coEvery { getGemmaModelPath() } returns "/fake/model.litertlm"
        every { imageNormalizer.normalize(any(), any()) } returns null

        val events = engine.analyzeImage(
            GemmaImagePromptRequest(imageUri = "content://invalid", prompt = "분석"),
        ).toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is GemmaGenerationEvent.Failed)
        assertEquals(
            GemmaGenerationError.ImageReadFailed,
            (events[0] as GemmaGenerationEvent.Failed).error,
        )
    }

    @Test
    fun `imageNormalizer가 OOM을 던지면 analyzeImage는 ImageNormalizeFailed를 emit한다`() = runTest {
        coEvery { getGemmaModelPath() } returns "/fake/model.litertlm"
        every { imageNormalizer.normalize(any(), any()) } throws OutOfMemoryError("boom")

        val events = engine.analyzeImage(
            GemmaImagePromptRequest(imageUri = "content://invalid", prompt = "분석"),
        ).toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is GemmaGenerationEvent.Failed)
        assertEquals(
            GemmaGenerationError.ImageNormalizeFailed,
            (events[0] as GemmaGenerationEvent.Failed).error,
        )
    }

    @Test
    fun `imageNormalizer가 빈 파일을 반환하면 analyzeImage는 ImageNormalizeFailed를 emit한다`() = runTest {
        coEvery { getGemmaModelPath() } returns "/fake/model.litertlm"
        every { imageNormalizer.normalize(any(), any()) } returns File.createTempFile("gemma_test_", ".jpg")

        val events = engine.analyzeImage(
            GemmaImagePromptRequest(imageUri = "content://invalid", prompt = "분석"),
        ).toList()

        assertEquals(1, events.size)
        assertTrue(events[0] is GemmaGenerationEvent.Failed)
        assertEquals(
            GemmaGenerationError.ImageNormalizeFailed,
            (events[0] as GemmaGenerationEvent.Failed).error,
        )
    }
}
