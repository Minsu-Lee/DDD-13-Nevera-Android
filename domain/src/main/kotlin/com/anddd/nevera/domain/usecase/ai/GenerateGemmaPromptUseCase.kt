package com.anddd.nevera.domain.usecase.ai

import com.anddd.nevera.domain.model.ai.GemmaGenerationEvent
import com.anddd.nevera.domain.model.ai.GemmaPromptRequest
import com.anddd.nevera.domain.repository.GemmaPromptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GenerateGemmaPromptUseCase @Inject constructor(
    private val repository: GemmaPromptRepository,
) {
    operator fun invoke(request: GemmaPromptRequest): Flow<GemmaGenerationEvent> =
        repository.generatePrompt(request)
}
