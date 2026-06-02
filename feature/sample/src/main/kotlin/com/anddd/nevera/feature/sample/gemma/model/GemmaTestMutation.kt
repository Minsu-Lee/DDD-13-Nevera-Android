package com.anddd.nevera.feature.sample.gemma.model

import com.anddd.nevera.core.mvi.NeveraMutation
import com.anddd.nevera.domain.model.ai.GemmaAnalysisResult

sealed interface GemmaTestMutation : NeveraMutation {
    data object GenerationStarted : GemmaTestMutation
    data class StreamingToken(val token: String) : GemmaTestMutation
    data class GenerationCompleted(val fullText: String) : GemmaTestMutation
    data class GenerationFailed(val message: String) : GemmaTestMutation
    data class ElapsedTimeUpdated(val text: String) : GemmaTestMutation
    data class AnalysisResultParsed(val result: GemmaAnalysisResult) : GemmaTestMutation
    data class PromptUpdated(val text: String) : GemmaTestMutation
    data class ImageUriUpdated(val uri: String) : GemmaTestMutation
    data object ResultCleared : GemmaTestMutation
}
