package com.anddd.nevera.domain.model.ai

sealed interface GemmaGenerationError {
    data object ModelNotReady : GemmaGenerationError
    data object EmptyPrompt : GemmaGenerationError
    data object ImageReadFailed : GemmaGenerationError
    data object ImageNormalizeFailed : GemmaGenerationError
    data object EngineInitializeFailed : GemmaGenerationError
    data object GenerationFailed : GemmaGenerationError
    data object ResponseParseFailed : GemmaGenerationError
    data class Unknown(val throwable: Throwable? = null) : GemmaGenerationError
}
