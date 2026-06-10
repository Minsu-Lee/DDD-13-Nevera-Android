package com.anddd.nevera.feature.mypage.settingaccount.model

import com.anddd.nevera.core.mvi.NeveraState

data class SettingAccountUiState(
    val isLoading: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showWithdrawDialog: Boolean = false,
) : NeveraState
