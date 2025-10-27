package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Notification DTO for API responses
 */
data class NotificationDto(
    @Json(name = "id") val id: Long,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "notification_type") val notificationType: String, // MEETING, DEADLINE, DOCUMENT, etc.
    @Json(name = "related_id") val relatedId: Long? = null,
    @Json(name = "related_type") val relatedType: String? = null, // AGENDA, PROPONENT, DOCUMENT
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "read_at") val readAt: String? = null
)

/**
 * Request for creating a notification
 */
data class CreateNotificationRequest(
    @Json(name = "user_id") val userId: Long,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "notification_type") val notificationType: String,
    @Json(name = "related_id") val relatedId: Long? = null,
    @Json(name = "related_type") val relatedType: String? = null
)
