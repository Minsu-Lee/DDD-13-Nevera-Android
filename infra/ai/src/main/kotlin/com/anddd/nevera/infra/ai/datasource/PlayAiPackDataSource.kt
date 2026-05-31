package com.anddd.nevera.infra.ai.datasource

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.tasks.Task
import com.google.android.play.core.aipacks.AiPackLocation
import com.google.android.play.core.aipacks.AiPackManager
import com.google.android.play.core.aipacks.AiPackState
import com.google.android.play.core.aipacks.AiPackStateUpdateListener
import com.google.android.play.core.aipacks.AiPackStates
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal interface PlayAiPackDataSource {
    fun registerListener(listener: AiPackStateUpdateListener)
    fun unregisterListener(listener: AiPackStateUpdateListener)
    suspend fun fetch(packNames: List<String>): AiPackStates
    suspend fun getPackStates(packNames: List<String>): AiPackStates
    fun cancel(packNames: List<String>): AiPackStates
    fun getPackLocation(packName: String): AiPackLocation?
    fun getPackLocations(): Map<String, AiPackLocation>
    fun showConfirmationDialog(launcher: ActivityResultLauncher<IntentSenderRequest>): Boolean
}

internal class PlayAiPackDataSourceImpl(
    private val manager: AiPackManager,
) : PlayAiPackDataSource {

    override fun registerListener(listener: AiPackStateUpdateListener) {
        manager.registerListener(listener)
    }

    override fun unregisterListener(listener: AiPackStateUpdateListener) {
        manager.unregisterListener(listener)
    }

    override suspend fun fetch(packNames: List<String>): AiPackStates =
        manager.fetch(packNames).await()

    override suspend fun getPackStates(packNames: List<String>): AiPackStates =
        manager.getPackStates(packNames).await()

    override fun cancel(packNames: List<String>): AiPackStates =
        manager.cancel(packNames)

    override fun getPackLocation(packName: String): AiPackLocation? =
        manager.getPackLocation(packName)

    override fun getPackLocations(): Map<String, AiPackLocation> =
        manager.packLocations

    override fun showConfirmationDialog(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ): Boolean = manager.showConfirmationDialog(launcher)

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> if (cont.isActive) cont.resume(result) }
        addOnFailureListener { e -> if (cont.isActive) cont.resumeWithException(e) }
        addOnCanceledListener { if (cont.isActive) cont.cancel() }
    }
}
