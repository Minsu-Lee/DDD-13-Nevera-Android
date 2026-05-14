package com.anddd.nevera.infra.notification.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.anddd.nevera.core.common.fold
import com.anddd.nevera.domain.usecase.SyncDeviceTokenUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class FcmTokenSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncDeviceTokenUseCase: SyncDeviceTokenUseCase,
) : CoroutineWorker(context, workerParams) {

    private val isMaxRetriesExceeded: Boolean
        get() = runAttemptCount >= MAX_RETRIES

    override suspend fun doWork(): Result {
        val inputToken = inputData.getString(KEY_FCM_TOKEN)
        return syncDeviceTokenUseCase(inputToken)
            .fold(
                transformSuccess = { Result.success() },
                transformFailure = { if (isMaxRetriesExceeded) Result.failure() else Result.retry() }
            )
    }

    companion object {
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val MAX_RETRIES = 5
        const val WORK_NAME = "fcm_token_sync"

        private val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun createUpdateRequest(newFcmToken: String): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<FcmTokenSyncWorker>()
                .setInputData(workDataOf(KEY_FCM_TOKEN to newFcmToken))
                .setConstraints(networkConstraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()

        fun createSyncRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<FcmTokenSyncWorker>()
                .setConstraints(networkConstraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()
    }
}
