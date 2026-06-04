package com.anddd.nevera.feature.ingredient.ocrcapture.model

import com.anddd.nevera.core.mvi.NeveraState

data class OcrCaptureUiState(
    val openGallery: Boolean = false,
) : NeveraState
