package com.anddd.nevera.feature.mypage.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anddd.nevera.feature.mypage.main.MyPageScreen

const val MY_PAGE_ROUTE = "mypage"

fun NavGraphBuilder.myPageScreen(
    onNavigateToAppInfo: () -> Unit,
    onNavigateToAccountSetting: () -> Unit,
) {
    composable(route = MY_PAGE_ROUTE) {
        MyPageScreen(
            onNavigateToAppInfo = onNavigateToAppInfo,
            onNavigateToAccountSetting = onNavigateToAccountSetting,
        )
    }
}
