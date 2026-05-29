package com.anddd.nevera.feature.notification.main.model

import com.anddd.nevera.core.mvi.NeveraMutation

sealed interface NotificationMutation : NeveraMutation {
    data object Loading : NotificationMutation
    data object LoadComplete : NotificationMutation
    data class PermissionUpdated(val isGranted: Boolean) : NotificationMutation
    data class NotificationsLoaded(val items: List<NotificationItemUiModel>) : NotificationMutation
    data class NotificationRead(val id: String) : NotificationMutation
}
