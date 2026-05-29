package com.anddd.nevera.domain.model.notification

data class AppNotification(
    val id: String,
    val type: AppNotificationType,
    val title: String,
    val subtitle: String?,
    val receivedAt: Long,
    val isRead: Boolean,
    val deeplink: String,
)

enum class AppNotificationType {
    EXPIRY_DATE,
}
