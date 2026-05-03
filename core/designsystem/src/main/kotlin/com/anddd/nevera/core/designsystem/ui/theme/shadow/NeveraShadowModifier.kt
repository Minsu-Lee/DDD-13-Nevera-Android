package com.anddd.nevera.core.designsystem.ui.theme.shadow

import android.graphics.BlurMaskFilter
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme

fun Modifier.neveraShadow(
    layers: List<ShadowLayer> = NeveraTheme.shadow.default,
    cornerRadius: Dp,
): Modifier = composed {
    val paints = remember(layers) {
        layers.map { layer ->
            Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = layer.color.toArgb()
                }
            }
        }
    }

    this.graphicsLayer { clip = false }
        .drawBehind {
            val radius = cornerRadius.toPx()
            layers.forEachIndexed { index, layer ->
                val blurPx = layer.blur.toPx()
                if (blurPx > 0f) {
                    paints[index].asFrameworkPaint().maskFilter = BlurMaskFilter(
                        blurPx,
                        BlurMaskFilter.Blur.NORMAL
                    )
                }
                drawIntoCanvas { canvas ->
                    canvas.drawRoundRect(
                        left = 0f,
                        top = layer.offsetY.toPx(),
                        right = size.width,
                        bottom = size.height + layer.offsetY.toPx(),
                        radiusX = radius,
                        radiusY = radius,
                        paint = paints[index],
                    )
                }
            }
        }
}