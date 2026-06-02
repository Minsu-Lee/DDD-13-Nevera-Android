package com.anddd.nevera.feature.sample.gemma

import android.os.SystemClock
import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.core.mvi.NeveraViewModel
import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaImagePromptRequest
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import com.anddd.nevera.domain.usecase.ai.AnalyzeGemmaImageWithPromptUseCase
import com.anddd.nevera.domain.usecase.ai.GenerateGemmaPromptUseCase
import com.anddd.nevera.domain.usecase.ai.ParseGemmaAnalysisResultUseCase
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestIntent
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestMutation
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestSideEffect
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.syntax.Syntax
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GemmaTestViewModel @Inject constructor(
    private val generateGemmaPrompt: GenerateGemmaPromptUseCase,
    private val analyzeGemmaImageWithPrompt: AnalyzeGemmaImageWithPromptUseCase,
    private val parseGemmaAnalysisResult: ParseGemmaAnalysisResultUseCase,
) : NeveraViewModel<GemmaTestUiState, GemmaTestSideEffect, GemmaTestIntent, GemmaTestMutation>(
    GemmaTestUiState(),
) {
    private var elapsedTimerJob: Job? = null
    private var generationStartedAtMs: Long = 0L

    private val defaultImageAnalysisPrompt = """
        영수증 또는 이커머스 주문내역 텍스트에서 식재료를 추출해 JSON 배열만 반환해. 설명 없이 JSON만.

        규칙:
        - name: 핵심 재료명 (브랜드 제거, 예: "CJ 두부" → "두부")
        - category: [Veg, Fruit, MeatEggs, Sea, Dairy, Sauce, Drink, Processed, Etc] 중 택1, 애매하거나 분류 불가능한 경우 Etc 선택
        - location: [Fridge, Freezer, Pantry] 중 택1
        - quantity: 숫자만 (불명확하면 1)
        - cost: 최종 금액 숫자만 (할인가 적용된 금액), 금액을 읽을 수 없는 경우 0

        식재료가 아닌 항목(배송비, 쿠폰, 포인트 등)은 제외

        출력 예시:
        [{"name":"두부","category":"Etc","location":"Fridge","quantity":1,"cost":1500}]
    """.trimIndent()

    override fun handleIntent(action: GemmaTestIntent) {
        when (action) {
            is GemmaTestIntent.UpdatePrompt -> intent { applyMutation(GemmaTestMutation.PromptUpdated(action.text)) }
            is GemmaTestIntent.UpdateImageUri -> intent { applyMutation(GemmaTestMutation.ImageUriUpdated(action.uri)) }
            is GemmaTestIntent.UpdateImageUriFromPicker -> intent {
                applyMutation(GemmaTestMutation.ImageUriUpdated(action.uri))
                applyMutation(GemmaTestMutation.PromptUpdated(defaultImageAnalysisPrompt))
            }
            GemmaTestIntent.RunPrompt -> runPrompt()
            GemmaTestIntent.RunImageAnalysis -> runImageAnalysis()
            GemmaTestIntent.ClearResult -> intent {
                elapsedTimerJob?.cancel()
                elapsedTimerJob = null
                generationStartedAtMs = 0L
                applyMutation(GemmaTestMutation.ResultCleared)
            }
            GemmaTestIntent.OpenImagePicker -> intent { postSideEffect(GemmaTestSideEffect.ShowImagePickerBottomSheet) }
        }
    }

    private fun runPrompt() = intent {
        applyMutation(GemmaTestMutation.GenerationStarted)
        startElapsedTimer()
        val prompt = state.prompt
        try {
            generateGemmaPrompt(GemmaPromptRequest(prompt = prompt)).collect { event ->
                when (event) {
                    GemmaGenerationEvent.Started -> Unit
                    is GemmaGenerationEvent.Token -> applyMutation(GemmaTestMutation.StreamingToken(event.text))
                    is GemmaGenerationEvent.Completed -> {
                        val elapsedTimeText = stopElapsedTimer()
                        applyMutation(GemmaTestMutation.ElapsedTimeUpdated(elapsedTimeText))
                        applyMutation(GemmaTestMutation.GenerationCompleted(event.fullText))
                    }
                    is GemmaGenerationEvent.Failed -> {
                        val elapsedTimeText = stopElapsedTimer()
                        applyMutation(GemmaTestMutation.ElapsedTimeUpdated(elapsedTimeText))
                        applyMutation(GemmaTestMutation.GenerationFailed(event.error.toString()))
                    }
                }
            }
        } catch (e: OutOfMemoryError) {
            val elapsedTimeText = stopElapsedTimer()
            applyMutation(GemmaTestMutation.ElapsedTimeUpdated(elapsedTimeText))
            Timber.e(e, "Prompt generation ran out of memory")
            applyMutation(GemmaTestMutation.GenerationFailed("OutOfMemoryError: ${e.message ?: "unknown"}"))
        }
    }

    private fun runImageAnalysis() = intent {
        applyMutation(GemmaTestMutation.GenerationStarted)
        val totalStartedAtMs = SystemClock.elapsedRealtime()
        val traceId = newImageAnalysisTraceId(totalStartedAtMs)
        startElapsedTimer(totalStartedAtMs)
        val imageUri = state.imageUri
        val prompt = state.prompt
        var fullText = ""
        try {
            analyzeGemmaImageWithPrompt(
                GemmaImagePromptRequest(
                    imageUri = imageUri,
                    prompt = prompt,
                    traceId = traceId,
                ),
            ).collect { event ->
                when (event) {
                    GemmaGenerationEvent.Started -> Unit
                    is GemmaGenerationEvent.Token -> Unit
                    is GemmaGenerationEvent.Completed -> {
                        fullText = event.fullText
                        val elapsedTimeText = stopElapsedTimer()
                        applyMutation(GemmaTestMutation.ElapsedTimeUpdated(elapsedTimeText))
                        applyMutation(GemmaTestMutation.GenerationCompleted(fullText))
                        val jsonParseStartedAtMs = SystemClock.elapsedRealtime()
                        val parseResult = parseGemmaAnalysisResult(fullText)
                        logImageAnalysisPerf(
                            traceId = traceId,
                            stage = "jsonParse",
                            elapsedMs = elapsedSince(jsonParseStartedAtMs),
                        )
                        if (parseResult is NeveraResult.Success) {
                            applyMutation(GemmaTestMutation.AnalysisResultParsed(parseResult.data))
                        } else {
                            postSideEffect(GemmaTestSideEffect.ShowToast("JSON 파싱 실패"))
                        }
                        logImageAnalysisPerf(
                            traceId = traceId,
                            stage = "totalElapsed",
                            elapsedMs = elapsedSince(totalStartedAtMs),
                        )
                    }
                    is GemmaGenerationEvent.Failed -> {
                        val elapsedTimeText = stopElapsedTimer()
                        applyMutation(GemmaTestMutation.ElapsedTimeUpdated(elapsedTimeText))
                        applyMutation(GemmaTestMutation.GenerationFailed(event.error.toString()))
                        logImageAnalysisPerf(
                            traceId = traceId,
                            stage = "totalElapsed",
                            elapsedMs = elapsedSince(totalStartedAtMs),
                        )
                    }
                }
            }
        } catch (e: OutOfMemoryError) {
            val elapsedTimeText = stopElapsedTimer()
            applyMutation(GemmaTestMutation.ElapsedTimeUpdated(elapsedTimeText))
            Timber.e(e, "Image analysis ran out of memory")
            applyMutation(GemmaTestMutation.GenerationFailed("OutOfMemoryError: ${e.message ?: "unknown"}"))
            postSideEffect(GemmaTestSideEffect.ShowToast("이미지 분석 중 메모리가 부족합니다"))
            logImageAnalysisPerf(
                traceId = traceId,
                stage = "totalElapsed",
                elapsedMs = elapsedSince(totalStartedAtMs),
            )
        }
    }

    private fun startElapsedTimer(startedAtMs: Long = SystemClock.elapsedRealtime()) {
        elapsedTimerJob?.cancel()
        generationStartedAtMs = startedAtMs
        elapsedTimerJob = intent {
            while (true) {
                applyMutation(GemmaTestMutation.ElapsedTimeUpdated(formatElapsedTime()))
                delay(1_000)
            }
        }
    }

    private fun stopElapsedTimer(): String {
        elapsedTimerJob?.cancel()
        elapsedTimerJob = null
        return formatElapsedTime()
    }

    private fun formatElapsedTime(): String {
        if (generationStartedAtMs == 0L) return "00:00"
        val elapsedSeconds = ((SystemClock.elapsedRealtime() - generationStartedAtMs) / 1_000).coerceAtLeast(0)
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun newImageAnalysisTraceId(startedAtMs: Long): String = "gemma-image-$startedAtMs"

    private fun elapsedSince(startedAtMs: Long): Long =
        (SystemClock.elapsedRealtime() - startedAtMs).coerceAtLeast(0L)

    private fun logImageAnalysisPerf(traceId: String, stage: String, elapsedMs: Long) {
        Timber.d("GemmaImageAnalysisPerf traceId=%s %s=%dms", traceId, stage, elapsedMs)
    }

    override suspend fun Syntax<GemmaTestUiState, GemmaTestSideEffect>.applyMutation(mutation: GemmaTestMutation) {
        when (mutation) {
            is GemmaTestMutation.GenerationStarted -> reduce { state.copy(isGenerating = true, streamingText = "", analysisResult = null, errorMessage = null, elapsedTimeText = "00:00") }
            is GemmaTestMutation.StreamingToken -> reduce { state.copy(streamingText = state.streamingText + mutation.token) }
            is GemmaTestMutation.GenerationCompleted -> reduce { state.copy(isGenerating = false, streamingText = mutation.fullText) }
            is GemmaTestMutation.GenerationFailed -> reduce { state.copy(isGenerating = false, errorMessage = mutation.message) }
            is GemmaTestMutation.ElapsedTimeUpdated -> reduce { state.copy(elapsedTimeText = mutation.text) }
            is GemmaTestMutation.AnalysisResultParsed -> reduce { state.copy(analysisResult = mutation.result) }
            is GemmaTestMutation.PromptUpdated -> reduce { state.copy(prompt = mutation.text) }
            is GemmaTestMutation.ImageUriUpdated -> reduce { state.copy(imageUri = mutation.uri) }
            GemmaTestMutation.ResultCleared -> reduce {
                state.copy(
                    isGenerating = false,
                    streamingText = "",
                    analysisResult = null,
                    errorMessage = null,
                    elapsedTimeText = null,
                )
            }
        }
    }
}
