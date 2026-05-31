package com.anddd.nevera.infra.ai.repository

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.ai.GemmaAnalysisResult
import com.anddd.nevera.domain.model.ai.GemmaGenerationError
import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaImagePromptRequest
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import com.anddd.nevera.domain.repository.GemmaPromptRepository
import com.anddd.nevera.infra.ai.engine.GemmaInferenceEngine
import com.anddd.nevera.infra.ai.parser.GemmaAnalysisResponseParser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GemmaPromptRepositoryImpl @Inject constructor(
    private val engine: GemmaInferenceEngine,
    private val parser: GemmaAnalysisResponseParser,
) : GemmaPromptRepository {

    override fun generatePrompt(request: GemmaPromptRequest): Flow<GemmaGenerationEvent> =
        engine.generateText(request)

    override fun analyzeImageWithPrompt(request: GemmaImagePromptRequest): Flow<GemmaGenerationEvent> =
        engine.analyzeImage(request)

    override suspend fun parseAnalysisResult(rawText: String): NeveraResult<GemmaAnalysisResult, GemmaGenerationError> =
        parser.parse(rawText)
}
