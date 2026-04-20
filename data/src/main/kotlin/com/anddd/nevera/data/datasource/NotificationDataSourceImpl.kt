package com.anddd.nevera.data.datasource

import com.anddd.nevera.core.network.model.ApiResponse
import com.anddd.nevera.data.api.NotificationApi
import com.anddd.nevera.data.model.notification.RegisterFcmTokenRequest
import com.anddd.nevera.data.model.notification.RegisterFcmTokenResponse
import javax.inject.Inject

internal class NotificationDataSourceImpl @Inject constructor(
    private val notificationApi: NotificationApi,
) : NotificationDataSource {

    override suspend fun registerFcmToken(pushToken: String): ApiResponse<RegisterFcmTokenResponse>? {
        val request = RegisterFcmTokenRequest(pushToken)
        return notificationApi.registerFcmToken(request)
    }
}
