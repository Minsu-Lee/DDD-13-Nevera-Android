package com.anddd.nevera.data.datasource

import com.anddd.nevera.core.network.model.ApiResponse
import com.anddd.nevera.data.model.notification.NotificationListResponse
import com.anddd.nevera.data.model.notification.NotificationTimeResponse
import com.anddd.nevera.data.model.notification.UpdateNotificationTimeRequest

internal interface NotificationRemoteDataSource {
    suspend fun getNotifications(offset: Int): ApiResponse<List<NotificationListResponse>>
    suspend fun getNotificationTime(): ApiResponse<NotificationTimeResponse>
    suspend fun updateNotificationTime(request: UpdateNotificationTimeRequest): ApiResponse<NotificationTimeResponse>
}
