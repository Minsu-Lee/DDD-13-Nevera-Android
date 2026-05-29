package com.anddd.nevera.feature.fridge.main

import com.anddd.nevera.core.mvi.NeveraViewModel
import com.anddd.nevera.feature.fridge.main.model.FridgeIntent
import com.anddd.nevera.feature.fridge.main.model.FridgeMutation
import com.anddd.nevera.feature.fridge.main.model.FridgeSideEffect
import com.anddd.nevera.feature.fridge.main.model.FridgeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class FridgeViewModel @Inject constructor() :
    NeveraViewModel<FridgeUiState, FridgeSideEffect, FridgeIntent, FridgeMutation>(FridgeUiState()) {

    init {
        handleIntent(FridgeIntent.Load)
    }

    override fun handleIntent(intent: FridgeIntent) {
        when (intent) {
            FridgeIntent.Load -> load()
            FridgeIntent.Submit -> submit()
        }
    }

    private fun load() = intent {
        reduce { state.copy(isLoading = true) }
        // TODO: UseCase로 초기 데이터 로드
        applyMutation(FridgeMutation.LoadSuccess)
    }

    private fun submit() = intent {
        reduce { state.copy(isLoading = true) }
        // TODO: UseCase로 제출 처리
        postSideEffect(FridgeSideEffect.ShowToast("submitted"))
    }

    override suspend fun Syntax<FridgeUiState, FridgeSideEffect>.applyMutation(
        mutation: FridgeMutation,
    ) {
        when (mutation) {
            FridgeMutation.LoadSuccess -> reduce { state.copy(isLoading = false) }
        }
    }
}
