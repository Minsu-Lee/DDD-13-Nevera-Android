package com.anddd.nevera.infra.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberPermissionState(
    permission: AppPermission,
    checker: PermissionChecker = DefaultPermissionChecker,
): PermissionState {
    val context = LocalContext.current
    return remember(context, permission) {
        val activity = context.findActivity() ?: return@remember PermissionState.Granted
        checker.checkState(activity, permission)
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
