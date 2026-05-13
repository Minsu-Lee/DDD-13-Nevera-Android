package com.anddd.nevera.feature.mypage.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.anddd.nevera.core.designsystem.R
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme
import com.anddd.nevera.feature.mypage.main.model.ProfileUiModel

@Composable
internal fun ProfileContent(
    modifier: Modifier = Modifier,
    profile: ProfileUiModel,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(NeveraTheme.spacing.padding16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NeveraTheme.spacing.gap12)
    ) {
        if (profile.profileImage == null) {
            Image(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = "프로필 이미지"
            )
        } else {
            // TODO: 프로필 이미지 로드 구현 (Coil 등)
        }

        Text(
            text = profile.email,
            style = NeveraTheme.typography.captionLarge,
            color = NeveraTheme.colors.textCaption
        )
    }
}

@Preview(
    name = "ProfileContent - 프로필 이미지 없음",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun ProfileContentNoImagePreview() {
    NeveraTheme {
        ProfileContent(
            profile = ProfileUiModel("hong@example.com")
        )
    }
}

@Preview(
    name = "ProfileContent - 프로필 이미지 있음",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun ProfileContentWithImagePreview() {
    NeveraTheme {
        ProfileContent(
            profile = ProfileUiModel("hong@example.com")
        )
    }
}
