package com.anddd.nevera.feature.sample.gemma.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.anddd.nevera.feature.sample.gemma.GemmaTestScreen
import kotlinx.serialization.Serializable

@Serializable
data object GemmaTestRoute

fun NavGraphBuilder.gemmaTestScreen() {
    composable<GemmaTestRoute> {
        GemmaTestScreen()
    }
}
