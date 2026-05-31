package com.anddd.nevera.feature.sample.gemma.model

import com.anddd.nevera.core.mvi.NeveraSideEffect

sealed interface GemmaTestSideEffect : NeveraSideEffect {
    data class ShowToast(val message: String) : GemmaTestSideEffect
    data object ShowImagePickerBottomSheet : GemmaTestSideEffect
}
