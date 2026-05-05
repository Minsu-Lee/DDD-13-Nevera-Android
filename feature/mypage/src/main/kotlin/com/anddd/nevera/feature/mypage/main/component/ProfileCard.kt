package com.anddd.nevera.feature.mypage.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anddd.nevera.core.designsystem.R
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme

@Composable
internal fun ProfileContent(
    modifier: Modifier = Modifier,
    profileImage: String?,
    name: String,
    email: String,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (profileImage == null) {
            Image(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = "프로필 이미지"
            )
        } else {        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = name,
                style = NeveraTheme.typography.titleLarge,
                color = NeveraTheme.colors.textSecondary
            )

            Text(
                text = email,
                style = NeveraTheme.typography.captionLarge,
                color = NeveraTheme.colors.textCaption
            )
        }
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
            profileImage = null,
            name = "홍길동",
            email = "hong@example.com",
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
            profileImage = "https://example.com/profile.jpg",
            name = "홍길동",
            email = "hong@example.com",
        )
    }
}
