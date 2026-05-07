package com.anddd.nevera.core.designsystem.component.textfield.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anddd.nevera.core.designsystem.R
import com.anddd.nevera.core.designsystem.component.textfield.NeveraTextFieldConfig
import com.anddd.nevera.core.designsystem.component.textfield.NeveraTextFieldState
import com.anddd.nevera.core.designsystem.component.textfield.NeveraTextFieldType
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme

@Composable
internal fun NeveraBaseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: NeveraTextFieldConfig,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val visualTransformation = when {
        config.isPassword && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val borderColor = NeveraTextFieldColors.borderColor(config.state, isFocused, enabled)
    val containerColor = NeveraTextFieldColors.containerColor(enabled)
    val textColor = NeveraTextFieldColors.textColor(enabled)
    val placeholderColor = NeveraTextFieldColors.placeholderColor(enabled)
    val descriptionColor = NeveraTextFieldColors.descriptionColor(config.state, enabled)

    val containerModifier = when (config.type) {
        NeveraTextFieldType.Box -> Modifier
            .background(containerColor, NeveraTextFieldDefaults.BoxShape)
            .border(NeveraTextFieldDefaults.BorderWidth, borderColor, NeveraTextFieldDefaults.BoxShape)

        // border()는 사방 테두리를 그리므로 drawBehind로 하단선만 직접 그린다.
        NeveraTextFieldType.Underline -> Modifier
            .background(containerColor)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = NeveraTextFieldDefaults.BorderWidth.toPx(),
                )
            }
    }

    Column(modifier = modifier) {
        config.heading?.let { heading ->
            Text(
                text = heading,
                modifier = Modifier.padding(start = NeveraTextFieldDefaults.LabelStartPadding),
                style = NeveraTheme.typography.titleXSmall,
                color = textColor,
            )
            Spacer(modifier = Modifier.height(NeveraTextFieldDefaults.HeadingBottomGap))
        }

        // Material3 TextField/OutlinedTextField는 내부 padding·color·shape이 고정되어 Underline 타입과 커스텀 배경 적용이 불가능하다.
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 48.dp),
            enabled = enabled,
            textStyle = NeveraTheme.typography.bodyMedium.copy(color = textColor),
            keyboardOptions = config.keyboardOptions,
            keyboardActions = config.keyboardActions,
            singleLine = config.singleLine,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    modifier = containerModifier
                        .fillMaxWidth()
                        .padding(NeveraTextFieldDefaults.ContentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && config.placeholder != null) {
                            Text(
                                text = config.placeholder,
                                style = NeveraTheme.typography.bodyMedium,
                                color = placeholderColor,
                            )
                        }
                        // innerTextField()를 호출하지 않으면 커서가 렌더링되지 않는다.
                        innerTextField()
                    }
                    TrailingIcons(
                        state = config.state,
                        isPassword = config.isPassword,
                        passwordVisible = passwordVisible,
                        enabled = enabled,
                        onEyeIconClick = { passwordVisible = !passwordVisible },
                    )
                }
            },
        )

        config.description?.let { description ->
            Spacer(modifier = Modifier.height(NeveraTextFieldDefaults.DescriptionTopGap))
            Text(
                text = description,
                modifier = Modifier.padding(start = NeveraTextFieldDefaults.LabelStartPadding),
                style = NeveraTheme.typography.captionLarge,
                color = descriptionColor,
            )
        }
    }
}

@Composable
private fun TrailingIcons(
    state: NeveraTextFieldState,
    isPassword: Boolean,
    passwordVisible: Boolean,
    enabled: Boolean,
    onEyeIconClick: () -> Unit,
) {
    val hasStateIcon = state != NeveraTextFieldState.Normal
    if (!hasStateIcon && !isPassword) return

    val stateIconColor = NeveraTextFieldColors.stateIconColor(state)
    val eyeIconColor = NeveraTextFieldColors.eyeIconColor(enabled)

    Row(
        horizontalArrangement = Arrangement.spacedBy(NeveraTheme.spacing.gap4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasStateIcon) {
            val stateIconPainter = when (state) {
                NeveraTextFieldState.Positive -> painterResource(R.drawable.ic_circle_check)
                NeveraTextFieldState.Negative -> painterResource(R.drawable.ic_circle_warning)
                NeveraTextFieldState.Normal -> error("unreachable")
            }
            Icon(
                painter = stateIconPainter,
                contentDescription = null,
                modifier = Modifier.size(NeveraTheme.iconSize.small),
                tint = stateIconColor,
            )
        }
        if (isPassword) {
            IconButton(onClick = onEyeIconClick) {
                Icon(
                    painter = if (passwordVisible) {
                        painterResource(R.drawable.ic_eyes)
                    } else {
                        painterResource(R.drawable.ic_eyes_off)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(NeveraTheme.iconSize.small),
                    tint = eyeIconColor,
                )
            }
        }
    }
}
