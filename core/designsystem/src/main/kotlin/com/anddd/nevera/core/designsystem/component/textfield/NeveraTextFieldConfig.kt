package com.anddd.nevera.core.designsystem.component.textfield

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

enum class NeveraTextFieldType {
    Box,
    Underline,
}

enum class NeveraTextFieldState {
    Normal,
    Positive,
    Negative,
}

data class NeveraTextFieldConfig(
    val type: NeveraTextFieldType = NeveraTextFieldType.Box,
    val state: NeveraTextFieldState = NeveraTextFieldState.Normal,
    val heading: String? = null,
    val placeholder: String? = null,
    val description: String? = null,
    // passwordVisible 상태는 컴포넌트 내부에서 관리하며 외부로 노출하지 않는다.
    val isPassword: Boolean = false,
    val singleLine: Boolean = true,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val keyboardActions: KeyboardActions = KeyboardActions.Default,
)