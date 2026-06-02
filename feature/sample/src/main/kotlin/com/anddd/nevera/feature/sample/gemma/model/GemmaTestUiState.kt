package com.anddd.nevera.feature.sample.gemma.model

import com.anddd.nevera.core.mvi.NeveraState
import com.anddd.nevera.domain.model.ai.GemmaAnalysisResult

data class GemmaTestUiState(
    val prompt: String = "",
    val imageUri: String = "",
    val streamingText: String = "",
    val elapsedTimeText: String? = null,
    val isGenerating: Boolean = false,
    val analysisResult: GemmaAnalysisResult? = null,
    val errorMessage: String? = null,
) : NeveraState
