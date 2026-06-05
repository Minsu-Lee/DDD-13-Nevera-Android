package com.anddd.nevera.domain.usecase.notification

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.notification.GetNotificationTimeError
import com.anddd.nevera.domain.model.notification.NotificationTime
import com.anddd.nevera.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationTimeUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(): NeveraResult<NotificationTime, GetNotificationTimeError> =
        notificationRepository.getNotificationTime()
}
