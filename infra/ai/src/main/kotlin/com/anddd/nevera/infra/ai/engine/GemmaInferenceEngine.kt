package com.anddd.nevera.infra.ai.engine

import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaImagePromptRequest
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import kotlinx.coroutines.flow.Flow

internal interface GemmaInferenceEngine {
    fun generateText(request: GemmaPromptRequest): Flow<GemmaGenerationEvent>
    fun analyzeImage(request: GemmaImagePromptRequest): Flow<GemmaGenerationEvent>
    suspend fun close()
}
