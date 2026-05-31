package com.anddd.nevera.domain.repository

import com.anddd.nevera.domain.model.ai.GemmaModelState
import kotlinx.coroutines.flow.Flow

interface GemmaModelRepository {
    fun observeGemmaModelState(): Flow<GemmaModelState>
    suspend fun refreshGemmaModelState(): GemmaModelState
    suspend fun requestGemmaModelDownload()
    suspend fun cancelGemmaModelDownload()
    suspend fun getGemmaModelPath(): String?
}
