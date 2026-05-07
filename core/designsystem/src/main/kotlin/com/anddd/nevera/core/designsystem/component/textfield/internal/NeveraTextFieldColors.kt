package com.anddd.nevera.core.designsystem.component.textfield.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.anddd.nevera.core.designsystem.component.textfield.NeveraTextFieldState
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme

internal object NeveraTextFieldColors {

    // 우선순위: disabled > 상태 색상(Negative/Positive) > 포커스 > 기본
    @Composable
    fun borderColor(
        state: NeveraTextFieldState,
        isFocused: Boolean,
        enabled: Boolean,
    ): Color = when {
        !enabled -> Color.Transparent
        state == NeveraTextFieldState.Negative -> NeveraTheme.colors.statusNegativeNormal
        state == NeveraTextFieldState.Positive -> NeveraTheme.colors.secondaryWeak
        isFocused -> NeveraTheme.colors.primaryNormal
        else -> NeveraTheme.colors.borderStrong
    }

    @Composable
    fun containerColor(enabled: Boolean): Color = when {
        !enabled -> NeveraTheme.colors.surfaceSecondary
        else -> NeveraTheme.colors.surfacePrimary
    }

    @Composable
    fun textColor(enabled: Boolean): Color = when {
        !enabled -> NeveraTheme.colors.textDisabled
        else -> NeveraTheme.colors.textPrimary
    }

    @Composable
    fun placeholderColor(enabled: Boolean): Color = when {
        !enabled -> NeveraTheme.colors.textDisabled
        else -> NeveraTheme.colors.textCaption
    }

    @Composable
    fun descriptionColor(
        state: NeveraTextFieldState,
        enabled: Boolean,
    ): Color = when {
        !enabled -> NeveraTheme.colors.textDisabled
        state == NeveraTextFieldState.Negative -> NeveraTheme.colors.statusNegativeNormal
        state == NeveraTextFieldState.Positive -> NeveraTheme.colors.statusPositiveNormal
        else -> NeveraTheme.colors.textCaption
    }

    @Composable
    fun stateIconColor(state: NeveraTextFieldState): Color = when (state) {
        NeveraTextFieldState.Positive -> NeveraTheme.colors.statusPositiveNormal
        NeveraTextFieldState.Negative -> NeveraTheme.colors.statusNegativeNormal
        NeveraTextFieldState.Normal -> NeveraTheme.colors.iconPrimary
    }

    @Composable
    fun eyeIconColor(enabled: Boolean): Color = when {
        !enabled -> NeveraTheme.colors.iconDisabled
        else -> NeveraTheme.colors.iconPrimary
    }
}