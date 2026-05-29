package com.anddd.nevera.domain.repository

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.common.CommonError
import com.anddd.nevera.domain.model.notification.AppNotification

interface NotificationRepository {
    suspend fun getNotifications(offset: Int = 0): NeveraResult<List<AppNotification>, CommonError>
    suspend fun markAsRead(id: String): NeveraResult<Unit, CommonError>
}
