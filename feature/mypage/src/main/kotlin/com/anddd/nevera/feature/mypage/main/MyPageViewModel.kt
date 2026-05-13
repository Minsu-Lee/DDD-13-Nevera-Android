package com.anddd.nevera.feature.mypage.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anddd.nevera.feature.mypage.main.model.MyPageIntent
import com.anddd.nevera.feature.mypage.main.model.MyPageSideEffect
import com.anddd.nevera.feature.mypage.main.model.MyPageStatus
import com.anddd.nevera.feature.mypage.main.model.MyPageUiState
import com.anddd.nevera.feature.mypage.main.model.SettingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor() : ViewModel() {

    private val intents = Channel<MyPageIntent>(Channel.BUFFERED)

    val uiState: StateFlow<MyPageUiState> = intents.receiveAsFlow()
        .onEach(::handleEffect)
        .runningFold(initialState, ::reduce)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialState,
        )

    private val _sideEffect = Channel<MyPageSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        processIntent(MyPageIntent.Load)
    }

    // ① 단일 진입점 — View → ViewModel 유일한 경로 (UDF 강제)
    fun processIntent(intent: MyPageIntent) {
        viewModelScope.launch { intents.send(intent) }
    }

    // ② 순수 함수 — 동기적 상태 전이 (부수 효과 없음, 테스트 용이)
    private fun reduce(state: MyPageUiState, intent: MyPageIntent): MyPageUiState = when (intent) {
        MyPageIntent.Load -> state.copy(status = MyPageStatus.Loading)
        MyPageIntent.LoadComplete -> state.copy(status = MyPageStatus.Idle)
        is MyPageIntent.SettingItemClicked -> state
    }

    // ③ 비동기 부수 효과 분기 — 새 비동기 Intent 추가 시 여기와 private 함수만 수정
    private fun handleEffect(intent: MyPageIntent) {
        when (intent) {
            MyPageIntent.Load -> load()
            MyPageIntent.LoadComplete -> {}
            is MyPageIntent.SettingItemClicked -> when (intent.item) {
                SettingItem.Notification -> {}
                SettingItem.Account -> {}
                SettingItem.AppInfo -> {}
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            // TODO: UseCase로 초기 데이터 로드
            intents.send(MyPageIntent.LoadComplete)
        }
    }

    companion object {
        private val initialState = MyPageUiState(
            settingItems = listOf(
                SettingItem.Notification,
                SettingItem.Account,
                SettingItem.AppInfo,
            ),
        )
    }
}
