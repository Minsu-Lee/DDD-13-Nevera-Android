package com.anddd.nevera.core.ui.ai

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

interface GemmaDownloadConfirmationLauncher {
    fun showConfirmationDialog(launcher: ActivityResultLauncher<IntentSenderRequest>): Boolean
}
