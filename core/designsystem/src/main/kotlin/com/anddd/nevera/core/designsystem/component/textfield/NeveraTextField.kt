package com.anddd.nevera.core.designsystem.component.textfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anddd.nevera.core.designsystem.component.textfield.internal.NeveraBaseTextField

@Composable
fun NeveraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: NeveraTextFieldConfig = NeveraTextFieldConfig(),
) {
    NeveraBaseTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        config = config,
    )
}
