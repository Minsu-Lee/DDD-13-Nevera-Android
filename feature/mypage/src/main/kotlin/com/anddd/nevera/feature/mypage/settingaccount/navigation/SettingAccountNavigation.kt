package com.anddd.nevera.feature.mypage.settingaccount.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anddd.nevera.feature.mypage.settingaccount.SettingAccountScreen

const val SETTING_ACCOUNT_ROUTE = "setting_account"

fun NavGraphBuilder.settingAccountScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    composable(route = SETTING_ACCOUNT_ROUTE) {
        SettingAccountScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToLogin = onNavigateToLogin,
        )
    }
}
