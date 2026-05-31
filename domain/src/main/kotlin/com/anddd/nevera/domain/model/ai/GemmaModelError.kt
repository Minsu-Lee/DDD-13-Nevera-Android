package com.anddd.nevera.domain.model.ai

sealed interface GemmaModelError {
    data object UnknownPack : GemmaModelError
    data object PlayStoreUnavailable : GemmaModelError
    data object DownloadFailed : GemmaModelError
    data object CanceledByUser : GemmaModelError
    data object MissingPackLocation : GemmaModelError
    data object MissingShard : GemmaModelError
    data object MergeFailed : GemmaModelError
    data object ChecksumMismatch : GemmaModelError
    data class PlayError(val code: Int) : GemmaModelError
    data class Unknown(val throwable: Throwable? = null) : GemmaModelError
}
