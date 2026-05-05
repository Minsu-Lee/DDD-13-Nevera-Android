package com.anddd.nevera.feature.mypage.main.model

enum class SettingItemType { Notification, Account, AppInfo }
data class SettingItemUiModel(
    val iconRes: Int,
    val label: String,
    val type: SettingItemType,
)
