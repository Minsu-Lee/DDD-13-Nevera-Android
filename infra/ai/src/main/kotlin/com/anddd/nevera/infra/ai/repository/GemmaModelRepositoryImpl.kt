package com.anddd.nevera.infra.ai.repository

import android.content.Context
import com.anddd.nevera.domain.model.ai.GemmaModelError
import com.anddd.nevera.domain.model.ai.GemmaModelState
import com.anddd.nevera.domain.repository.GemmaModelRepository
import com.anddd.nevera.infra.ai.GemmaAiPackConstants
import com.anddd.nevera.infra.ai.datasource.PlayAiPackDataSource
import com.anddd.nevera.infra.ai.merger.GemmaShardMerger
import com.google.android.play.core.aipacks.AiPackState
import com.google.android.play.core.aipacks.AiPackStateUpdateListener
import com.google.android.play.core.aipacks.model.AiPackErrorCode
import com.google.android.play.core.aipacks.model.AiPackStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GemmaModelRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataSource: PlayAiPackDataSource,
) : GemmaModelRepository {

    private val _state = MutableStateFlow<GemmaModelState>(GemmaModelState.NotRequested)
    private val merger = GemmaShardMerger(outputDir = File(context.noBackupFilesDir, "gemma4"))

    // per-pack state 추적 (listener가 pack별로 단일 AiPackState를 전달하기 때문)
    private val packStateCache = ConcurrentHashMap<String, AiPackState>()
    private var listenerRegistered = false

    private val listener = AiPackStateUpdateListener { packState ->
        packStateCache[packState.name()] = packState
        val allPackStates = GemmaAiPackConstants.PACK_NAMES.mapNotNull { packStateCache[it] }
        val combined = combinePackStates(allPackStates)
        Timber.d("AI pack state update [${packState.name()}]: $combined")
        _state.value = combined
        if (isTerminalState(combined)) {
            unregisterListenerIfNeeded()
        }
    }

    override fun observeGemmaModelState(): Flow<GemmaModelState> = _state.asStateFlow()

    override suspend fun refreshGemmaModelState(): GemmaModelState {
        _state.value = GemmaModelState.Checking
        return try {
            if (merger.isModelReady()) {
                GemmaModelState.Ready(merger.modelPath()).also { _state.value = it }
            } else {
                val states = dataSource.getPackStates(GemmaAiPackConstants.PACK_NAMES)
                val packStates = states.packStates().values.toList()
                packStates.forEach { packStateCache[it.name()] = it }
                val allCompleted = GemmaAiPackConstants.PACK_NAMES.all { name ->
                    packStateCache[name]?.status() == AiPackStatus.COMPLETED
                }
                if (allCompleted) {
                    mergeShards()
                } else {
                    GemmaModelState.NotInstalled.also { _state.value = it }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing Gemma model state")
            GemmaModelState.Failed(GemmaModelError.Unknown(e)).also { _state.value = it }
        }
    }

    override suspend fun requestGemmaModelDownload() {
        _state.value = GemmaModelState.Checking

        if (merger.isModelReady()) {
            _state.value = GemmaModelState.Ready(merger.modelPath())
            return
        }

        try {
            registerListenerIfNeeded()
            val states = dataSource.fetch(GemmaAiPackConstants.PACK_NAMES)
            states.packStates().values.forEach { packStateCache[it.name()] = it }
            val combined = combinePackStates(packStateCache.values.toList())
            _state.value = combined
            if (isTerminalState(combined)) {
                unregisterListenerIfNeeded()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error requesting Gemma model download")
            unregisterListenerIfNeeded()
            _state.value = GemmaModelState.Failed(GemmaModelError.Unknown(e))
        }
    }

    override suspend fun cancelGemmaModelDownload() {
        _state.value = GemmaModelState.Canceling
        try {
            val result = dataSource.cancel(GemmaAiPackConstants.PACK_NAMES)
            val packStates = result.packStates().values.toList()
            val hasCanceled = packStates.any { it.status() == AiPackStatus.CANCELED }
            if (hasCanceled || packStates.isEmpty()) {
                _state.value = GemmaModelState.Canceled
            }
            packStateCache.clear()
            unregisterListenerIfNeeded()
        } catch (e: Exception) {
            Timber.e(e, "Error canceling Gemma model download")
            _state.value = GemmaModelState.Failed(GemmaModelError.Unknown(e))
        }
    }

    override suspend fun getGemmaModelPath(): String? =
        if (merger.isModelReady()) merger.modelPath() else null

    internal fun combinePackStates(packStates: List<AiPackState>): GemmaModelState {
        if (packStates.isEmpty()) return GemmaModelState.NotInstalled

        val statuses = packStates.map { it.status() }

        if (statuses.any { it == AiPackStatus.FAILED }) {
            val errorCode = packStates.firstOrNull { it.status() == AiPackStatus.FAILED }
                ?.errorCode() ?: AiPackErrorCode.NO_ERROR
            return GemmaModelState.Failed(GemmaModelError.PlayError(errorCode))
        }
        if (statuses.any { it == AiPackStatus.UNKNOWN }) {
            return GemmaModelState.Failed(GemmaModelError.UnknownPack)
        }
        if (statuses.any { it == AiPackStatus.REQUIRES_USER_CONFIRMATION }) {
            return GemmaModelState.RequiresUserConfirmation
        }
        if (statuses.any { it == AiPackStatus.WAITING_FOR_WIFI }) {
            return GemmaModelState.WaitingForWifi
        }
        if (statuses.any { it == AiPackStatus.CANCELED }) {
            return GemmaModelState.Canceled
        }
        if (statuses.all { it == AiPackStatus.COMPLETED }) {
            return mergeShards()
        }
        if (statuses.any { it == AiPackStatus.TRANSFERRING }) {
            val avgPercent = packStates.map { it.transferProgressPercentage() }
                .average().toFloat().div(100f)
            return GemmaModelState.Transferring(avgPercent)
        }
        if (statuses.any { it == AiPackStatus.DOWNLOADING }) {
            val totalDownloaded = packStates.sumOf { it.bytesDownloaded() }
            val totalBytes = packStates.sumOf { it.totalBytesToDownload() }
            val percent = if (totalBytes > 0L) {
                (totalDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
            } else 0f
            return GemmaModelState.Downloading(totalDownloaded, totalBytes, percent)
        }
        if (statuses.any { it == AiPackStatus.PENDING }) {
            return GemmaModelState.Pending
        }
        return GemmaModelState.NotInstalled
    }

    private fun mergeShards(): GemmaModelState {
        val shardFiles = GemmaAiPackConstants.PARTS.map { part ->
            val location = dataSource.getPackLocation(part.packName)
                ?: return GemmaModelState.Failed(GemmaModelError.MissingPackLocation)
            val assetsPath = location.assetsPath()
                ?: return GemmaModelState.Failed(GemmaModelError.MissingPackLocation)
            File(assetsPath, part.relativeAssetPath)
        }
        return merger.merge(shardFiles).also { _state.value = it }
    }

    private fun isTerminalState(state: GemmaModelState): Boolean =
        state is GemmaModelState.Ready ||
            state is GemmaModelState.Failed ||
            state is GemmaModelState.Canceled ||
            state is GemmaModelState.NotInstalled

    private fun registerListenerIfNeeded() {
        if (!listenerRegistered) {
            dataSource.registerListener(listener)
            listenerRegistered = true
        }
    }

    private fun unregisterListenerIfNeeded() {
        if (listenerRegistered) {
            dataSource.unregisterListener(listener)
            listenerRegistered = false
        }
    }
}
