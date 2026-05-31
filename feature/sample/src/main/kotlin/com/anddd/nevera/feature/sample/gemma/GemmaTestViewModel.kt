package com.anddd.nevera.feature.sample.gemma

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.core.mvi.NeveraViewModel
import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaImagePromptRequest
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import com.anddd.nevera.domain.usecase.ai.AnalyzeGemmaImageWithPromptUseCase
import com.anddd.nevera.domain.usecase.ai.CancelGemmaModelDownloadUseCase
import com.anddd.nevera.domain.usecase.ai.GenerateGemmaPromptUseCase
import com.anddd.nevera.domain.usecase.ai.ObserveGemmaModelStateUseCase
import com.anddd.nevera.domain.usecase.ai.ParseGemmaAnalysisResultUseCase
import com.anddd.nevera.domain.usecase.ai.RequestGemmaModelDownloadUseCase
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestIntent
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestMutation
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestSideEffect
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class GemmaTestViewModel @Inject constructor(
    private val observeGemmaModelState: ObserveGemmaModelStateUseCase,
    private val requestGemmaModelDownload: RequestGemmaModelDownloadUseCase,
    private val cancelGemmaModelDownload: CancelGemmaModelDownloadUseCase,
    private val generateGemmaPrompt: GenerateGemmaPromptUseCase,
    private val analyzeGemmaImageWithPrompt: AnalyzeGemmaImageWithPromptUseCase,
    private val parseGemmaAnalysisResult: ParseGemmaAnalysisResultUseCase,
) : NeveraViewModel<GemmaTestUiState, GemmaTestSideEffect, GemmaTestIntent, GemmaTestMutation>(
    GemmaTestUiState(),
) {

    init {
        intent {
            observeGemmaModelState().collect { modelState ->
                applyMutation(GemmaTestMutation.ModelStateChanged(modelState))
            }
        }
    }

    override fun handleIntent(action: GemmaTestIntent) {
        when (action) {
            GemmaTestIntent.DownloadModel -> downloadModel()
            GemmaTestIntent.CancelDownload -> cancelDownload()
            is GemmaTestIntent.UpdatePrompt -> intent { applyMutation(GemmaTestMutation.PromptUpdated(action.text)) }
            is GemmaTestIntent.UpdateImageUri -> intent { applyMutation(GemmaTestMutation.ImageUriUpdated(action.uri)) }
            GemmaTestIntent.RunPrompt -> runPrompt()
            GemmaTestIntent.RunImageAnalysis -> runImageAnalysis()
            GemmaTestIntent.ClearResult -> intent { applyMutation(GemmaTestMutation.ResultCleared) }
        }
    }

    private fun downloadModel() = intent {
        requestGemmaModelDownload()
    }

    private fun cancelDownload() = intent {
        cancelGemmaModelDownload()
    }

    private fun runPrompt() = intent {
        val prompt = state.prompt
        generateGemmaPrompt(GemmaPromptRequest(prompt = prompt)).collect { event ->
            when (event) {
                GemmaGenerationEvent.Started -> applyMutation(GemmaTestMutation.GenerationStarted)
                is GemmaGenerationEvent.Token -> applyMutation(GemmaTestMutation.StreamingToken(event.text))
                is GemmaGenerationEvent.Completed -> applyMutation(GemmaTestMutation.GenerationCompleted(event.fullText))
                is GemmaGenerationEvent.Failed -> applyMutation(GemmaTestMutation.GenerationFailed(event.error.toString()))
            }
        }
    }

    private fun runImageAnalysis() = intent {
        val imageUri = state.imageUri
        val prompt = state.prompt
        var fullText = ""
        analyzeGemmaImageWithPrompt(
            GemmaImagePromptRequest(imageUri = imageUri, prompt = prompt),
        ).collect { event ->
            when (event) {
                GemmaGenerationEvent.Started -> applyMutation(GemmaTestMutation.GenerationStarted)
                is GemmaGenerationEvent.Token -> applyMutation(GemmaTestMutation.StreamingToken(event.text))
                is GemmaGenerationEvent.Completed -> {
                    fullText = event.fullText
                    applyMutation(GemmaTestMutation.GenerationCompleted(fullText))
                    val parseResult = parseGemmaAnalysisResult(fullText)
                    if (parseResult is NeveraResult.Success) {
                        applyMutation(GemmaTestMutation.AnalysisResultParsed(parseResult.data))
                    } else {
                        postSideEffect(GemmaTestSideEffect.ShowToast("JSON 파싱 실패"))
                    }
                }
                is GemmaGenerationEvent.Failed -> applyMutation(GemmaTestMutation.GenerationFailed(event.error.toString()))
            }
        }
    }

    override suspend fun Syntax<GemmaTestUiState, GemmaTestSideEffect>.applyMutation(mutation: GemmaTestMutation) {
        when (mutation) {
            is GemmaTestMutation.ModelStateChanged -> reduce { state.copy(modelState = mutation.modelState) }
            is GemmaTestMutation.GenerationStarted -> reduce { state.copy(isGenerating = true, streamingText = "", analysisResult = null, errorMessage = null) }
            is GemmaTestMutation.StreamingToken -> reduce { state.copy(streamingText = state.streamingText + mutation.token) }
            is GemmaTestMutation.GenerationCompleted -> reduce { state.copy(isGenerating = false, streamingText = mutation.fullText) }
            is GemmaTestMutation.GenerationFailed -> reduce { state.copy(isGenerating = false, errorMessage = mutation.message) }
            is GemmaTestMutation.AnalysisResultParsed -> reduce { state.copy(analysisResult = mutation.result) }
            is GemmaTestMutation.PromptUpdated -> reduce { state.copy(prompt = mutation.text) }
            is GemmaTestMutation.ImageUriUpdated -> reduce { state.copy(imageUri = mutation.uri) }
            GemmaTestMutation.ResultCleared -> reduce { state.copy(streamingText = "", analysisResult = null, errorMessage = null) }
        }
    }
}
