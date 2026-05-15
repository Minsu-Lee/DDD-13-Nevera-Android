package com.anddd.nevera.feature.mypage.appinfo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme
import com.anddd.nevera.feature.mypage.appinfo.component.AppInfoContent
import com.anddd.nevera.feature.mypage.appinfo.model.AppInfoSideEffect
import com.anddd.nevera.feature.mypage.appinfo.model.AppInfoUiState
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AppInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppInfoViewModel = hiltViewModel(),
) {
    val uiState = viewModel.collectAsState().value

    viewModel.collectSideEffect { effect ->
        when (effect) {
            AppInfoSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    NeveraTheme {
        AppInfoContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent,
        )
    }
}
