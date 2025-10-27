package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.CreateNotificationRequest
import com.mgb.mrfcmanager.data.remote.dto.NotificationDto
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for notification operations
 */
interface NotificationApiService {

    @GET("notifications")
    suspend fun getAllNotifications(
        @Query("user_id") userId: Long? = null,
        @Query("is_read") isRead: Boolean? = null
    ): Response<ApiResponse<List<NotificationDto>>>

    @GET("notifications/{id}")
    suspend fun getNotificationById(
        @Path("id") id: Long
    ): Response<ApiResponse<NotificationDto>>

    @POST("notifications")
    suspend fun createNotification(
        @Body request: CreateNotificationRequest
    ): Response<ApiResponse<NotificationDto>>

    @PUT("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: Long
    ): Response<ApiResponse<NotificationDto>>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>
}
