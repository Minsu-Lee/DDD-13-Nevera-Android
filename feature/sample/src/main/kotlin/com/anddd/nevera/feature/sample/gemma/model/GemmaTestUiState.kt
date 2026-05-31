package com.anddd.nevera.feature.sample.gemma.model

import com.anddd.nevera.core.mvi.NeveraState
import com.anddd.nevera.domain.model.ai.GemmaAnalysisResult
import com.anddd.nevera.domain.model.ai.GemmaModelState

data class GemmaTestUiState(
    val modelState: GemmaModelState = GemmaModelState.NotRequested,
    val prompt: String = "",
    val imageUri: String = "",
    val streamingText: String = "",
    val isGenerating: Boolean = false,
    val analysisResult: GemmaAnalysisResult? = null,
    val errorMessage: String? = null,
) : NeveraState
