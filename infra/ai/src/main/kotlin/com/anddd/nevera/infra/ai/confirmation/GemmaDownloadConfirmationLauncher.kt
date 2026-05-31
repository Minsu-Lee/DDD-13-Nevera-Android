package com.anddd.nevera.infra.ai.confirmation

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.aipacks.AiPackManager

interface GemmaDownloadConfirmationLauncher {
    fun showConfirmationDialog(launcher: ActivityResultLauncher<IntentSenderRequest>): Boolean
}

internal class GemmaDownloadConfirmationLauncherImpl(
    private val manager: AiPackManager,
) : GemmaDownloadConfirmationLauncher {

    override fun showConfirmationDialog(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ): Boolean = manager.showConfirmationDialog(launcher)
}
