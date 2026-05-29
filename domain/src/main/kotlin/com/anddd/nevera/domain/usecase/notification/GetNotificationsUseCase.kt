package com.anddd.nevera.domain.usecase.notification

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.common.CommonError
import com.anddd.nevera.domain.model.notification.AppNotification
import com.anddd.nevera.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(offset: Int = 0): NeveraResult<List<AppNotification>, CommonError> =
        notificationRepository.getNotifications(offset)
}
