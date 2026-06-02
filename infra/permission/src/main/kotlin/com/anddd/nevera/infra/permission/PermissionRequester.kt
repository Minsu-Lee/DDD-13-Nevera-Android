package com.anddd.nevera.infra.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import timber.log.Timber

@Composable
fun PermissionRequester(
    permission: AppPermission,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    content: @Composable (onConfirm: () -> Unit, onDismiss: () -> Unit) -> Unit,
    rationaleContent: @Composable ((onConfirm: () -> Unit, onDismiss: () -> Unit) -> Unit)? = null,
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: run {
        Timber.w("PermissionRequester requires Activity context")
        LaunchedEffect(Unit) { onDenied() }
        return
    }

    var showRationale by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            onGranted()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission.manifestPermission,
            )
        ) {
            showRationale = true
        } else {
            context.openAppSettings()
            onDenied()
        }
    }

    if (showRationale) {
        val onConfirm = { launcher.launch(permission.manifestPermission) }
        val onDismiss = { onDenied() }
        (rationaleContent ?: content)(onConfirm, onDismiss)
    } else {
        val onConfirm = { launcher.launch(permission.manifestPermission) }
        val onDismiss = { onDenied() }
        content(onConfirm, onDismiss)
    }
}

private fun Context.openAppSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }.let(::startActivity)
}
