package com.anddd.nevera.domain.usecase.ai

import com.anddd.nevera.domain.model.ai.GemmaModelState
import com.anddd.nevera.domain.repository.GemmaModelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGemmaModelStateUseCase @Inject constructor(
    private val repository: GemmaModelRepository,
) {
    operator fun invoke(): Flow<GemmaModelState> = repository.observeGemmaModelState()
}
