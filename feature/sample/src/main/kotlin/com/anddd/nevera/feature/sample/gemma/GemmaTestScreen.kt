package com.anddd.nevera.feature.sample.gemma

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.anddd.nevera.feature.sample.R
import com.anddd.nevera.feature.sample.gemma.component.GemmaImagePickerBottomSheet
import com.anddd.nevera.feature.sample.gemma.component.GemmaTestContent
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestIntent
import com.anddd.nevera.feature.sample.gemma.model.GemmaTestSideEffect
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemmaTestScreen(
    viewModel: GemmaTestViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state = viewModel.collectAsState().value
    val imagePickerSheetState = rememberModalBottomSheetState()
    var showImagePickerBottomSheet by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCameraLaunchAfterPermission by rememberSaveable { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { viewModel.handleIntent(GemmaTestIntent.UpdateImageUriFromPicker(it.toString())) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            pendingCameraUri?.let { viewModel.handleIntent(GemmaTestIntent.UpdateImageUriFromPicker(it.toString())) }
        }
        pendingCameraUri = null
        pendingCameraLaunchAfterPermission = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val uri = pendingCameraUri
        if (granted && uri != null && pendingCameraLaunchAfterPermission) {
            cameraLauncher.launch(uri)
        } else if (!granted && pendingCameraLaunchAfterPermission) {
            Toast.makeText(context, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            pendingCameraUri = null
        }
        pendingCameraLaunchAfterPermission = false
    }

    val imagePickerTitle = stringResource(R.string.gemma_test_image_picker_title)

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is GemmaTestSideEffect.ShowToast ->
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()

            GemmaTestSideEffect.ShowImagePickerBottomSheet ->
                showImagePickerBottomSheet = true
        }
    }

    GemmaTestContent(
        uiState = state,
        onIntent = viewModel::handleIntent,
    )

    if (showImagePickerBottomSheet) {
        GemmaImagePickerBottomSheet(
            sheetState = imagePickerSheetState,
            onCameraClick = {
                showImagePickerBottomSheet = false
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "gemma_test_${System.currentTimeMillis()}.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    },
                )
                if (uri != null) {
                    pendingCameraUri = uri
                    val cameraGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (cameraGranted) {
                        runCatching {
                            cameraLauncher.launch(uri)
                        }.onFailure {
                            pendingCameraUri = null
                            pendingCameraLaunchAfterPermission = false
                            Toast.makeText(context, "카메라를 실행할 수 없습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        pendingCameraLaunchAfterPermission = true
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                } else {
                    Toast.makeText(context, imagePickerTitle, Toast.LENGTH_SHORT).show()
                }
            },
            onGalleryClick = {
                showImagePickerBottomSheet = false
                galleryLauncher.launch("image/*")
            },
            onDismiss = { showImagePickerBottomSheet = false },
        )
    }
}
