package com.anddd.nevera.core.designsystem.component.textfield.internal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme

internal object NeveraTextFieldDefaults {

    // NeveraTheme은 CompositionLocal 기반이므로 Composable 컨텍스트 안에서만 읽어야 한다.
    val BoxShape: Shape
        @Composable get() = RoundedCornerShape(NeveraTheme.spacing.padding8)

    val BoxContentPadding: PaddingValues
        @Composable get() = PaddingValues(all = NeveraTheme.spacing.padding12)

    val UnderlineContentPadding: PaddingValues
        @Composable get() = PaddingValues(
            start = NeveraTheme.spacing.padding4,
            top = NeveraTheme.spacing.padding4,
            end = NeveraTheme.spacing.padding8,
            bottom = NeveraTheme.spacing.padding4
        )

    val BorderWidth: Dp = 1.dp

    val LabelStartPadding: Dp
        @Composable get() = NeveraTheme.spacing.padding4

    val HeadingBottomGap: Dp
        @Composable get() = NeveraTheme.spacing.gap8

    val DescriptionTopGap: Dp
        @Composable get() = NeveraTheme.spacing.gap4
}
