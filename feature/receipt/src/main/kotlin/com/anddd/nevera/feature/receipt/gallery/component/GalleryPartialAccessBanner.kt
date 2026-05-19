package com.anddd.nevera.feature.receipt.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.anddd.nevera.core.designsystem.component.button.NeveraButtonSize
import com.anddd.nevera.core.designsystem.component.button.NeveraOutlinedButton
import com.anddd.nevera.core.designsystem.ui.theme.NeveraTheme
import com.anddd.nevera.feature.receipt.R

/**
 * Android 14(API 34)의 부분 미디어 접근(READ_MEDIA_VISUAL_USER_SELECTED) 대응 배너.
 *
 * 사용자가 갤러리 권한을 "일부 허용"으로 선택한 경우, 허용된 사진만 그리드에 노출된다.
 * 이 배너는 해당 상태를 사용자에게 알리고, 추가 사진을 선택할 수 있도록 유도한다.
 * "추가" 버튼 클릭 시 권한 재요청으로 시스템 사진 선택기(추가 모드)가 열린다.
 *
 * TODO: 디자이너와 디자인 확정 후 스타일(색상, 텍스트, 레이아웃) 업데이트 필요.
 *       확정 전까지 임시 스타일(backgroundPrimary + textPrimary)이 적용되어 있음.
 */
@Composable
internal fun GalleryPartialAccessBanner(
    onSelectMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NeveraTheme.colors.backgroundPrimary)
            .padding(
                horizontal = NeveraTheme.spacing.padding16,
                vertical = NeveraTheme.spacing.padding8,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.receipt_gallery_partial_access),
            style = NeveraTheme.typography.bodySmall,
            color = NeveraTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        NeveraOutlinedButton(
            label = stringResource(R.string.receipt_gallery_partial_access_select),
            onClick = onSelectMore,
            size = NeveraButtonSize.XSmall,
            shape = RoundedCornerShape(NeveraTheme.radius.max)
        )
    }
}

@Preview(widthDp = 360, showBackground = true)
@Composable
private fun GalleryPartialAccessBannerPreview() {
    NeveraTheme {
        GalleryPartialAccessBanner(onSelectMore = {})
    }
}