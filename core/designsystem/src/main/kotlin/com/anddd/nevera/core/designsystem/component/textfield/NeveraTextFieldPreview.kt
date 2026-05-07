package com.anddd.nevera.core.designsystem.component.textfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme

// region — 상태별 단일 Preview

@Preview(name = "NeveraTextField - Normal", showBackground = true, widthDp = 360)
@Composable
private fun PreviewNeveraTextField_Normal() {
    NeveraTheme {
        NeveraTextField(
            value = "",
            onValueChange = {},
            config = NeveraTextFieldConfig(
                heading = "이메일",
                placeholder = "이메일을 입력하세요",
            ),
        )
    }
}

@Preview(name = "NeveraTextField - Positive", showBackground = true, widthDp = 360)
@Composable
private fun PreviewNeveraTextField_Positive() {
    NeveraTheme {
        NeveraTextField(
            value = "user@example.com",
            onValueChange = {},
            config = NeveraTextFieldConfig(
                heading = "이메일",
                state = NeveraTextFieldState.Positive,
                description = "사용 가능한 이메일입니다",
            ),
        )
    }
}

@Preview(name = "NeveraTextField - Negative", showBackground = true, widthDp = 360)
@Composable
private fun PreviewNeveraTextField_Negative() {
    NeveraTheme {
        NeveraTextField(
            value = "invalid-email",
            onValueChange = {},
            config = NeveraTextFieldConfig(
                heading = "이메일",
                state = NeveraTextFieldState.Negative,
                description = "이메일 형식이 올바르지 않습니다",
            ),
        )
    }
}

@Preview(name = "NeveraTextField - Disabled", showBackground = true, widthDp = 360)
@Composable
private fun PreviewNeveraTextField_Disabled() {
    NeveraTheme {
        NeveraTextField(
            value = "user@example.com",
            onValueChange = {},
            enabled = false,
            config = NeveraTextFieldConfig(
                heading = "이메일",
                description = "수정할 수 없습니다",
            ),
        )
    }
}

@Preview(name = "NeveraTextField - Password", showBackground = true, widthDp = 360)
@Composable
private fun PreviewNeveraTextField_Password() {
    NeveraTheme {
        NeveraTextField(
            value = "password123",
            onValueChange = {},
            config = NeveraTextFieldConfig(
                heading = "비밀번호",
                placeholder = "비밀번호를 입력하세요",
                isPassword = true,
            ),
        )
    }
}

// endregion

// region — MultiPreview (Type × State 조합)

@Preview(name = "Box / Normal", showBackground = true, widthDp = 360)
@Preview(name = "Box / Positive", showBackground = true, widthDp = 360)
@Preview(name = "Box / Negative", showBackground = true, widthDp = 360)
@Preview(name = "Underline / Normal", showBackground = true, widthDp = 360)
@Preview(name = "Underline / Positive", showBackground = true, widthDp = 360)
@Preview(name = "Underline / Negative", showBackground = true, widthDp = 360)
private annotation class TextFieldMultiPreview

@TextFieldMultiPreview
@Composable
private fun PreviewNeveraTextFieldMatrix() {
    NeveraTheme {
        NeveraTextField(
            value = "입력값",
            onValueChange = {},
            config = NeveraTextFieldConfig(
                heading = "레이블",
                description = "설명 텍스트",
            ),
        )
    }
}

// endregion

// region — PreviewParameterProvider

private class NeveraTextFieldConfigProvider : PreviewParameterProvider<NeveraTextFieldConfig> {
    override val values = sequenceOf(
        NeveraTextFieldConfig(
            heading = "Normal",
            placeholder = "내용을 입력하세요",
        ),
        NeveraTextFieldConfig(
            heading = "Positive",
            state = NeveraTextFieldState.Positive,
            description = "사용 가능합니다",
        ),
        NeveraTextFieldConfig(
            heading = "Negative",
            state = NeveraTextFieldState.Negative,
            description = "형식이 올바르지 않습니다",
        ),
        NeveraTextFieldConfig(
            heading = "Password",
            isPassword = true,
            placeholder = "비밀번호를 입력하세요",
        ),
        NeveraTextFieldConfig(
            heading = "Underline",
            type = NeveraTextFieldType.Underline,
            placeholder = "내용을 입력하세요",
        ),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewNeveraTextFieldParam(
    @PreviewParameter(NeveraTextFieldConfigProvider::class) config: NeveraTextFieldConfig,
) {
    NeveraTheme {
        NeveraTextField(
            value = "값",
            onValueChange = {},
            config = config,
        )
    }
}

// endregion
