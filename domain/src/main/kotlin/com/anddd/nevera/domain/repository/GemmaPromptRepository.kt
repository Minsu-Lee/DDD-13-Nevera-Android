package com.anddd.nevera.domain.repository

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.ai.GemmaAnalysisResult
import com.anddd.nevera.domain.model.ai.GemmaGenerationError
import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaImagePromptRequest
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import kotlinx.coroutines.flow.Flow

interface GemmaPromptRepository {
    fun generatePrompt(request: GemmaPromptRequest): Flow<GemmaGenerationEvent>
    fun analyzeImageWithPrompt(request: GemmaImagePromptRequest): Flow<GemmaGenerationEvent>
    suspend fun parseAnalysisResult(rawText: String): NeveraResult<GemmaAnalysisResult, GemmaGenerationError>
}
