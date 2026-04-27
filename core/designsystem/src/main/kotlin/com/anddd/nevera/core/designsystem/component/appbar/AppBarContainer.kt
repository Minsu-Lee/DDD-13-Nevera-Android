package com.anddd.nevera.core.designsystem.component.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme
import com.anddd.nevera.core.designsystem.ui.theme.color.ColorPalette

@Composable
internal fun AppBarContainer(
    modifier: Modifier = Modifier,
    showBackground: Boolean = true,
    startPadding: Dp = AppBarDefault.horizontalSpacingMedium,
    endPadding: Dp = AppBarDefault.horizontalSpacingMedium,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(AppBarDefault.height)
            .background(
                color = if (showBackground) NeveraTheme.colors.backgroundPrimary
                else ColorPalette.transparent   // todo implement NeveraTheme.colors
            )
            .padding(start = startPadding, end = endPadding),
        contentAlignment = Alignment.CenterStart,
        content = content
    )
}
