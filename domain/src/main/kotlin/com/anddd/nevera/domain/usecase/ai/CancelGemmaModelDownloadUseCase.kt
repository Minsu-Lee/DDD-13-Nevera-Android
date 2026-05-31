package com.anddd.nevera.domain.usecase.ai

import com.anddd.nevera.domain.repository.GemmaModelRepository
import javax.inject.Inject

class CancelGemmaModelDownloadUseCase @Inject constructor(
    private val repository: GemmaModelRepository,
) {
    suspend operator fun invoke() = repository.cancelGemmaModelDownload()
}
