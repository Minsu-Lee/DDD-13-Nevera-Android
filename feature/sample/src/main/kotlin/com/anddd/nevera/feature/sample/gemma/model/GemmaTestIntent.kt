package com.anddd.nevera.feature.sample.gemma.model

import com.anddd.nevera.core.mvi.NeveraIntent

sealed interface GemmaTestIntent : NeveraIntent {
    data object DownloadModel : GemmaTestIntent
    data object CancelDownload : GemmaTestIntent
    data class UpdatePrompt(val text: String) : GemmaTestIntent
    data class UpdateImageUri(val uri: String) : GemmaTestIntent
    data object RunPrompt : GemmaTestIntent
    data object RunImageAnalysis : GemmaTestIntent
    data object ClearResult : GemmaTestIntent
    data object OpenImagePicker : GemmaTestIntent
}
