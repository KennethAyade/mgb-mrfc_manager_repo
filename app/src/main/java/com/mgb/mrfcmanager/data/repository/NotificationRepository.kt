package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.NotificationApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateNotificationRequest
import com.mgb.mrfcmanager.data.remote.dto.NotificationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for notification operations
 * Handles all notification-related API calls
 */
class NotificationRepository(private val apiService: NotificationApiService) {

    /**
     * Get all notifications with optional filters
     */
    suspend fun getAllNotifications(
        userId: Long? = null,
        isRead: Boolean? = null
    ): Result<List<NotificationDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllNotifications(userId, isRead)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch notifications")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Get notification by ID
     */
    suspend fun getNotificationById(id: Long): Result<NotificationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNotificationById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch notification")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Create a new notification
     */
    suspend fun createNotification(request: CreateNotificationRequest): Result<NotificationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createNotification(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to create notification")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Mark notification as read
     */
    suspend fun markAsRead(id: Long): Result<NotificationDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.markAsRead(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to mark as read")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    /**
     * Delete a notification
     */
    suspend fun deleteNotification(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteNotification(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to delete notification")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }
}
