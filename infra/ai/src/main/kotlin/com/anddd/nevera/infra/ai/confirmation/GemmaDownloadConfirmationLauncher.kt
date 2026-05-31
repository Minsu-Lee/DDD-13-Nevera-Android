package com.anddd.nevera.infra.ai.confirmation

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.anddd.nevera.core.ui.ai.GemmaDownloadConfirmationLauncher
import com.google.android.play.core.aipacks.AiPackManager

internal class GemmaDownloadConfirmationLauncherImpl(
    private val manager: AiPackManager,
) : GemmaDownloadConfirmationLauncher {

    override fun showConfirmationDialog(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ): Boolean = manager.showConfirmationDialog(launcher)
}
