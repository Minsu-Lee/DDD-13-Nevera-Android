package com.anddd.nevera.core.designsystem.component.appbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anddd.nevera.core.designsystem.icon.NeveraIcons
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme

@Composable
internal fun AppBarNavigationSlot(navigation: AppBarNavigation) {
    when (navigation) {
        is AppBarNavigation.Back -> IconButton(onClick = navigation.onClick) {
            Icon(
                painter = NeveraIcons.ArrowBack,
                contentDescription = "뒤로가기",
                tint = NeveraTheme.colors.iconPrimary,
            )
        }

        is AppBarNavigation.Close -> IconButton(onClick = navigation.onClick) {
            Icon(
                painter = NeveraIcons.Close,
                contentDescription = "닫기",
                tint = NeveraTheme.colors.iconPrimary,
            )
        }

        is AppBarNavigation.Menu -> IconButton(onClick = navigation.onClick) {
            Icon(
                painter = NeveraIcons.Menu,
                contentDescription = "메뉴",
                tint = NeveraTheme.colors.iconPrimary,
            )
        }

        AppBarNavigation.None -> Unit
    }
}

@Composable
internal fun AppBarActionSlot(action: AppBarAction) {
    when (action) {
        is AppBarAction.Icons -> Row {
            action.items.forEach { item ->
                IconButton(onClick = item.onClick) {
                    Icon(
                        painter = item.painter,
                        contentDescription = item.contentDescription,
                        tint = NeveraTheme.colors.iconPrimary,
                    )
                }
            }
        }

        is AppBarAction.Text -> {
            val textColor = when (action.tone) {
                AppBarAction.Text.Tone.Primary -> NeveraTheme.colors.primaryNormal
                AppBarAction.Text.Tone.Tertiary -> NeveraTheme.colors.textTertiary
            }

            TextButton(onClick = action.onClick) {
                Text(
                    text = action.label,
                    style = NeveraTheme.typography.titleXSmall,
                    color = textColor,
                )
            }
        }

        AppBarAction.None -> Unit
    }
}
