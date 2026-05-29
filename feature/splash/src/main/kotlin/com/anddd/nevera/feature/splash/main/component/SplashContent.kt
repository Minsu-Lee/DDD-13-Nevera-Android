package com.anddd.nevera.feature.splash.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anddd.nevera.core.designsystem.icon.NeveraIcons
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme
import com.anddd.nevera.feature.splash.R

private val LogoWidth = 224.dp
private val LogoHeight = 100.dp
private val ContentColumnHeight = 420.dp
private val IllustrationWidth = 290.dp
private val IllustrationHeight = 232.dp

@Composable
internal fun SplashContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize()
            .background(NeveraTheme.colors.backgroundPrimary),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
                .height(ContentColumnHeight)
                .padding(horizontal = NeveraTheme.spacing.padding16)
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = NeveraIcons.Logo100,
                contentDescription = stringResource(R.string.splash_logo_description),
                modifier = Modifier.size(
                    width = LogoWidth,
                    height = LogoHeight,
                ),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(NeveraTheme.spacing.gap8))

            Text(
                text = stringResource(R.string.splash_subtitle),
                style = NeveraTheme.typography.headlineSmall,
                color = NeveraTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(NeveraTheme.spacing.gap20))

        Image(
            painter = painterResource(R.drawable.img_splash),
            contentDescription = stringResource(R.string.splash_illustration_description),
            modifier = Modifier.size(
                width = IllustrationWidth,
                height = IllustrationHeight,
            ),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashContentPreview() {
    NeveraTheme {
        SplashContent()
    }
}
