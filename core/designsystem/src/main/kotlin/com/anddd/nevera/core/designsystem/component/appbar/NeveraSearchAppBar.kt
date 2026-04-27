package com.anddd.nevera.core.designsystem.component.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anddd.nevera.core.designsystem.icon.NeveraIcons
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme

@Composable
fun NeveraSearchAppBar(
    modifier: Modifier = Modifier,
    navigation: AppBarNavigation = AppBarNavigation.None,
    action: AppBarAction = AppBarAction.None,
    showBackground: Boolean = true,
    searchBar: @Composable () -> Unit,
) {
    AppBarContainer(
        modifier = modifier,
        showBackground = showBackground,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBarNavigationSlot(navigation = navigation)
            Box(modifier = Modifier.weight(1f)) {
                searchBar()
            }
            AppBarActionSlot(action = action)
        }
    }
}

@Preview(
    name = "NeveraSearchAppBar - Default",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun NeveraSearchAppBarPreview() {
    NeveraTheme {
        NeveraSearchAppBar(
            searchBar = { SearchBarPreviewContent() },
        )
    }
}

@Preview(
    name = "NeveraSearchAppBar - Navigation Back",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun NeveraSearchAppBarNavigationBackPreview() {
    NeveraTheme {
        NeveraSearchAppBar(
            navigation = AppBarNavigation.Back(onClick = {}),
            searchBar = { SearchBarPreviewContent() },
        )
    }
}

@Preview(
    name = "NeveraSearchAppBar - Action Icons",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun NeveraSearchAppBarActionIconsPreview() {
    NeveraTheme {
        NeveraSearchAppBar(
            navigation = AppBarNavigation.Back(onClick = {}),
            action = AppBarAction.Icons(
                AppBarAction.Icons.Item(
                    painter = NeveraIcons.Close,
                    contentDescription = "닫기",
                    onClick = {},
                ),
            ),
            searchBar = { SearchBarPreviewContent() },
        )
    }
}

@Preview(
    name = "NeveraSearchAppBar - No Background",
    showBackground = false,
    widthDp = 360,
)
@Composable
private fun NeveraSearchAppBarNoBackgroundPreview() {
    NeveraTheme {
        NeveraSearchAppBar(
            navigation = AppBarNavigation.Back(onClick = {}),
            showBackground = false,
            searchBar = { SearchBarPreviewContent() },
        )
    }
}

@Composable
private fun SearchBarPreviewContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(
                color = NeveraTheme.colors.backgroundSecondary,
                shape = RoundedCornerShape(NeveraTheme.radius.medium),
            ),
    )
}
