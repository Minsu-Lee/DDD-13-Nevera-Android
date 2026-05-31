package com.anddd.nevera.feature.sample.gemma

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.anddd.nevera.feature.sample.gemma.component.GemmaTestContent
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestIntent
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestSideEffect
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun GemmaTestScreen(
    viewModel: GemmaTestViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state = viewModel.collectAsState().value

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is GemmaTestSideEffect.ShowToast ->
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
        }
    }

    GemmaTestContent(
        uiState = state,
        onIntent = viewModel::handleIntent,
    )
}
