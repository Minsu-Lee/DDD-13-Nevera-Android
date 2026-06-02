package com.anddd.nevera.infra.ai.repository

import com.anddd.nevera.domain.model.ai.GemmaModelError
import com.anddd.nevera.domain.model.ai.GemmaModelState
import com.anddd.nevera.domain.repository.GemmaModelRepository
import com.anddd.nevera.infra.ai.GemmaAiPackConstants
import com.anddd.nevera.infra.ai.datasource.PlayAiPackDataSource
import com.anddd.nevera.infra.ai.merger.ShardMerger
import com.google.android.play.core.aipacks.AiPackState
import com.google.android.play.core.aipacks.AiPackStateUpdateListener
import com.google.android.play.core.aipacks.model.AiPackErrorCode
import com.google.android.play.core.aipacks.model.AiPackStatus
import com.google.android.play.core.assetpacks.AssetPackException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GemmaModelRepositoryImpl @Inject constructor(
    private val dataSource: PlayAiPackDataSource,
    private val merger: ShardMerger,
) : GemmaModelRepository {

    private val _state = MutableStateFlow<GemmaModelState>(GemmaModelState.NotRequested)
    private val packStateCache = ConcurrentHashMap<String, AiPackState>()
    private var listenerRegistered = false

    // 리스너 콜백에서 블로킹 I/O를 실행하기 위한 전용 스코프
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val listener = AiPackStateUpdateListener { packState ->
        packStateCache[packState.name()] = packState
        val allPackStates = GemmaAiPackConstants.PACK_NAMES.mapNotNull { packStateCache[it] }

        val allCompleted = allPackStates.size == GemmaAiPackConstants.PACK_NAMES.size &&
            allPackStates.all { it.status() == AiPackStatus.COMPLETED }

        if (allCompleted) {
            // 모든 pack이 COMPLETED → IO 스코프에서 블로킹 머지 실행
            repositoryScope.launch {
                val result = mergeShards()
                _state.value = result
                unregisterListenerIfNeeded()
            }
        } else {
            val combined = mapPackStates(allPackStates)
            Timber.d("AI pack state update [${packState.name()}]: $combined")
            _state.value = combined
            if (isTerminalState(combined)) {
                unregisterListenerIfNeeded()
            }
        }
    }

    override fun observeGemmaModelState(): Flow<GemmaModelState> = _state.asStateFlow()

    override suspend fun refreshGemmaModelState(): GemmaModelState {
        _state.value = GemmaModelState.Checking
        return try {
            if (withContext(Dispatchers.IO) { merger.isModelReady() }) {
                GemmaModelState.Ready(merger.modelPath()).also { _state.value = it }
            } else {
                val states = dataSource.getPackStates(GemmaAiPackConstants.PACK_NAMES)
                val packStates = states.packStates().values.toList()
                packStates.forEach { packStateCache[it.name()] = it }
                val allCompleted = packStates.size == GemmaAiPackConstants.PACK_NAMES.size &&
                    packStates.all { it.status() == AiPackStatus.COMPLETED }
                if (allCompleted) {
                    mergeShards()
                } else {
                    mapPackStates(packStates).also { state ->
                        _state.value = state
                        if (!isTerminalState(state)) {
                            registerListenerIfNeeded()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing Gemma model state")
            GemmaModelState.Failed(e.toGemmaModelError()).also { _state.value = it }
        }
    }

    override suspend fun requestGemmaModelDownload() {
        _state.value = GemmaModelState.Checking

        if (withContext(Dispatchers.IO) { merger.isModelReady() }) {
            _state.value = GemmaModelState.Ready(merger.modelPath())
            return
        }

        try {
            registerListenerIfNeeded()
            val states = dataSource.fetch(GemmaAiPackConstants.PACK_NAMES)
            val packStates = states.packStates().values.toList()
            packStates.forEach { packStateCache[it.name()] = it }

            val allCompleted = packStates.size == GemmaAiPackConstants.PACK_NAMES.size &&
                packStates.all { it.status() == AiPackStatus.COMPLETED }

            if (allCompleted) {
                val result = mergeShards()
                _state.value = result
                unregisterListenerIfNeeded()
            } else {
                val combined = mapPackStates(packStates)
                _state.value = combined
                if (isTerminalState(combined)) {
                    unregisterListenerIfNeeded()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error requesting Gemma model download")
            unregisterListenerIfNeeded()
            _state.value = GemmaModelState.Failed(e.toGemmaModelError())
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
            _state.value = GemmaModelState.Failed(e.toGemmaModelError())
        }
    }

    override suspend fun getGemmaModelPath(): String? =
        withContext(Dispatchers.IO) {
            if (merger.isModelReady()) merger.modelPath() else null
        }

    // 순수 상태 매핑 — 부수효과 없음. COMPLETED 처리는 호출 측에서 담당
    internal fun mapPackStates(packStates: List<AiPackState>): GemmaModelState {
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

    // 블로킹 I/O → 항상 Dispatchers.IO에서 실행
    private suspend fun mergeShards(): GemmaModelState = withContext(Dispatchers.IO) {
        val shardFiles = GemmaAiPackConstants.PARTS.map { part ->
            val location = dataSource.getPackLocation(part.packName)
                ?: return@withContext GemmaModelState.Failed(GemmaModelError.MissingPackLocation)
            val assetsPath = location.assetsPath()
                ?: return@withContext GemmaModelState.Failed(GemmaModelError.MissingPackLocation)
            File(assetsPath, part.relativeAssetPath)
        }
        merger.merge(shardFiles).also { _state.value = it }
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

    private fun Throwable.toGemmaModelError(): GemmaModelError {
        val assetPackException = generateSequence(this) { throwable ->
            throwable.cause?.takeUnless { cause -> cause === throwable }
        }.filterIsInstance<AssetPackException>().firstOrNull()

        return assetPackException?.errorCode?.toGemmaModelError()
            ?: GemmaModelError.Unknown(this)
    }

    private fun Int.toGemmaModelError(): GemmaModelError =
        when (this) {
            AiPackErrorCode.APP_NOT_OWNED -> GemmaModelError.AppNotOwned
            AiPackErrorCode.APP_UNAVAILABLE,
            AiPackErrorCode.API_NOT_AVAILABLE -> GemmaModelError.PlayStoreUnavailable
            AiPackErrorCode.NETWORK_ERROR -> GemmaModelError.DownloadFailed
            else -> GemmaModelError.PlayError(this)
        }
}
