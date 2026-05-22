package com.anddd.nevera.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.anddd.nevera.feature.main.home.navigation.HomeRoute
import com.anddd.nevera.feature.main.home.navigation.homeScreen
import com.anddd.nevera.feature.mypage.main.navigation.MyPageRoute
import com.anddd.nevera.feature.mypage.main.navigation.myPageScreen
import kotlinx.serialization.Serializable

@Serializable
data object MainRoute

fun NavGraphBuilder.mainNavGraph(
    onNavigateToAppInfo: () -> Unit,
    onNavigateToAccountSetting: () -> Unit,
) {
    navigation<MainRoute>(startDestination = HomeRoute) {
        homeScreen()
        myPageScreen(
            onNavigateToAppInfo = onNavigateToAppInfo,
            onNavigateToAccountSetting = onNavigateToAccountSetting,
        )
    }
}
