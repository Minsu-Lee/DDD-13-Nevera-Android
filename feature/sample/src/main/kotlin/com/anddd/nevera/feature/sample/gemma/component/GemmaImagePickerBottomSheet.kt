package com.anddd.nevera.feature.sample.gemma.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anddd.nevera.feature.sample.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GemmaImagePickerBottomSheet(
    sheetState: SheetState,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.gemma_test_image_picker_title),
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Button(
                onClick = onCameraClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.gemma_test_image_picker_camera))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onGalleryClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.gemma_test_image_picker_gallery))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
