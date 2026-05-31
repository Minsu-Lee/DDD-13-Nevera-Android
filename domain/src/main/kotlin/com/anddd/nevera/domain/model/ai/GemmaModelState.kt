package com.anddd.nevera.domain.model.ai

sealed interface GemmaModelState {
    data object NotRequested : GemmaModelState
    data object Checking : GemmaModelState
    data object NotInstalled : GemmaModelState
    data object Pending : GemmaModelState
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val percent: Float,
    ) : GemmaModelState
    data class Transferring(val percent: Float?) : GemmaModelState
    data object Canceling : GemmaModelState
    data object Canceled : GemmaModelState
    data class Ready(val modelPath: String) : GemmaModelState
    data object WaitingForWifi : GemmaModelState
    data object RequiresUserConfirmation : GemmaModelState
    data class Failed(val error: GemmaModelError) : GemmaModelState
}
