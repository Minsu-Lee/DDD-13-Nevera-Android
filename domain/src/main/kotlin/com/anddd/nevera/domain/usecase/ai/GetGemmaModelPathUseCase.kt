package com.anddd.nevera.domain.usecase.ai

import com.anddd.nevera.domain.repository.GemmaModelRepository
import javax.inject.Inject

class GetGemmaModelPathUseCase @Inject constructor(
    private val repository: GemmaModelRepository,
) {
    suspend operator fun invoke(): String? = repository.getGemmaModelPath()
}
