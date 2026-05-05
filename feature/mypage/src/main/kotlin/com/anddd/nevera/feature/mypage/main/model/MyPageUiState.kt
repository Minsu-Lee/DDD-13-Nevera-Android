package com.anddd.nevera.feature.mypage.main.model

import com.anddd.nevera.core.designsystem.R

data class MyPageUiState(
    val status: MyPageStatus = MyPageStatus.Idle,
    val profile: ProfileUiModel = ProfileUiModel(),
    val settingItems: List<SettingItemUiModel> = listOf(
        SettingItemUiModel(
            iconRes = R.drawable.ic_bell,
            label = "알림",
            type = SettingItemType.Notification,
        ),
        SettingItemUiModel(
            iconRes = R.drawable.ic_user_circle,
            label = "계정",
            type = SettingItemType.Account,
        ),
        SettingItemUiModel(
            iconRes = R.drawable.ic_info,
            label = "앱정보",
            type = SettingItemType.AppInfo,
        )
    ),
)

sealed interface MyPageStatus {
    data object Idle : MyPageStatus
    data object Loading : MyPageStatus
    data object Success : MyPageStatus
}
