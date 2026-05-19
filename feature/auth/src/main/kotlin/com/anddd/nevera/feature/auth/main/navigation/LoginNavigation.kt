package com.anddd.nevera.feature.auth.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anddd.nevera.feature.auth.main.LoginScreen
import com.anddd.nevera.feature.auth.main.google.GoogleAuthClient

const val LOGIN_ROUTE = "login"

fun NavGraphBuilder.loginScreen(
    googleAuthClient: GoogleAuthClient,
    onNavigateToHome: () -> Unit,
    onNavigateToSignup: () -> Unit,
) {
    composable(route = LOGIN_ROUTE) {
        LoginScreen(
            googleAuthClient = googleAuthClient,
            onNavigateToHome = onNavigateToHome,
            onNavigateToSignup = onNavigateToSignup,
        )
    }
}
