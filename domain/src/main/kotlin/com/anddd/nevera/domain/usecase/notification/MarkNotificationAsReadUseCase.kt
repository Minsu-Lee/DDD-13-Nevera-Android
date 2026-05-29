package com.anddd.nevera.domain.usecase.notification

import com.anddd.nevera.core.common.NeveraResult
import com.anddd.nevera.domain.model.common.CommonError
import com.anddd.nevera.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(id: String): NeveraResult<Unit, CommonError> =
        notificationRepository.markAsRead(id)
}
