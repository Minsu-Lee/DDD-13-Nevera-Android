package com.anddd.nevera.feature.splash.main.model

import com.anddd.nevera.core.mvi.NeveraSideEffect

sealed interface SplashSideEffect : NeveraSideEffect {
    data class MoveToHome(val accessToken: String) : SplashSideEffect
    data object MoveToLogin : SplashSideEffect
}