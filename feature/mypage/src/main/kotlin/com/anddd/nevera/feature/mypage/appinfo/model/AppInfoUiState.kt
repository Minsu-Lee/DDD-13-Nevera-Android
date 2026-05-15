package com.anddd.nevera.feature.mypage.appinfo.model

import com.anddd.nevera.core.mvi.NeveraState

data class AppInfoUiState(
    val isLoading: Boolean = false,
    val appInfo: AppInfoUiModel = AppInfoUiModel(),
) : NeveraState

data class AppInfoUiModel(
    val termsUrl: String = "https://sikgu.notion.site/35a2b85edd1f8018a836c7db401110f4?source=copy_link",
    val privacyPolicyUrl: String = "https://sikgu.notion.site/35a2b85edd1f80828c2aea00237b6fa6?source=copy_link\n",
    val versionName: String = "",
)