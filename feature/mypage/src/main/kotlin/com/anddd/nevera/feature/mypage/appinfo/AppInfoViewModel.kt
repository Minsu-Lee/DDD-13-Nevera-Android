package com.anddd.nevera.feature.mypage.appinfo

import android.content.Context
import com.anddd.nevera.core.mvi.NeveraViewModel
import com.anddd.nevera.feature.mypage.appinfo.model.AppInfoIntent
import com.anddd.nevera.feature.mypage.appinfo.model.AppInfoMutation
import com.anddd.nevera.feature.mypage.appinfo.model.AppInfoSideEffect
import com.anddd.nevera.feature.mypage.appinfo.model.AppInfoUiModel
import com.anddd.nevera.feature.mypage.appinfo.model.AppInfoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class AppInfoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : NeveraViewModel<AppInfoUiState, AppInfoSideEffect, AppInfoIntent, AppInfoMutation>(
    AppInfoUiState()
) {
    init {
        loadVersionName()
    }

    override fun handleIntent(intent: AppInfoIntent) {
        when (intent) {
            AppInfoIntent.NavigateBack -> onNavigateBack()
            AppInfoIntent.TermsClicked -> {}
            AppInfoIntent.PrivacyPolicyClicked -> {}
        }
    }

    private fun loadVersionName() = intent {
        val versionName = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName ?: ""
        applyMutation(AppInfoMutation.LoadCompleted(AppInfoUiModel(versionName = "V$versionName")))
    }

    private fun onNavigateBack() = intent {
        postSideEffect(AppInfoSideEffect.NavigateBack)
    }

    override suspend fun Syntax<AppInfoUiState, AppInfoSideEffect>.applyMutation(mutation: AppInfoMutation) {
        when (mutation) {
            AppInfoMutation.Loading -> reduce { state.copy(isLoading = true) }
            is AppInfoMutation.LoadCompleted -> reduce { state.copy(isLoading = false, appInfo = mutation.appInfo) }
        }
    }
}
