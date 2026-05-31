package com.anddd.nevera.domain.model.ai

sealed interface GemmaGenerationEvent {
    data object Started : GemmaGenerationEvent
    data class Token(val text: String) : GemmaGenerationEvent
    data class Completed(val fullText: String) : GemmaGenerationEvent
    data class Failed(val error: GemmaGenerationError) : GemmaGenerationEvent
}
