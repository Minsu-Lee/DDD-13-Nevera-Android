package com.anddd.nevera.domain.usecase.ai

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.ai.GemmaAnalysisResult
import com.anddd.nevera.domain.model.ai.GemmaGenerationError
import com.anddd.nevera.domain.repository.GemmaPromptRepository
import javax.inject.Inject

class ParseGemmaAnalysisResultUseCase @Inject constructor(
    private val repository: GemmaPromptRepository,
) {
    suspend operator fun invoke(rawText: String): NeveraResult<GemmaAnalysisResult, GemmaGenerationError> =
        repository.parseAnalysisResult(rawText)
}
