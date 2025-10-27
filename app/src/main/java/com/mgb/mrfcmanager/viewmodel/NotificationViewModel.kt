package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.CreateNotificationRequest
import com.mgb.mrfcmanager.data.remote.dto.NotificationDto
import com.mgb.mrfcmanager.data.repository.NotificationRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Notification management
 * Handles notification state and operations
 */
class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    private val _notificationListState = MutableLiveData<NotificationListState>()
    val notificationListState: LiveData<NotificationListState> = _notificationListState

    private val _notificationDetailState = MutableLiveData<NotificationDetailState>()
    val notificationDetailState: LiveData<NotificationDetailState> = _notificationDetailState

    /**
     * Load all notifications with optional filters
     */
    fun loadNotifications(userId: Long? = null, isRead: Boolean? = null) {
        _notificationListState.value = NotificationListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllNotifications(userId, isRead)) {
                is Result.Success -> {
                    _notificationListState.value = NotificationListState.Success(result.data)
                }
                is Result.Error -> {
                    _notificationListState.value = NotificationListState.Error(result.message)
                }
                is Result.Loading -> {
                    _notificationListState.value = NotificationListState.Loading
                }
            }
        }
    }

    /**
     * Load unread notifications for a user
     */
    fun loadUnreadNotifications(userId: Long) {
        loadNotifications(userId = userId, isRead = false)
    }

    /**
     * Load notification by ID
     */
    fun loadNotificationById(id: Long) {
        _notificationDetailState.value = NotificationDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getNotificationById(id)) {
                is Result.Success -> {
                    _notificationDetailState.value = NotificationDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _notificationDetailState.value = NotificationDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _notificationDetailState.value = NotificationDetailState.Loading
                }
            }
        }
    }

    /**
     * Create a new notification
     */
    suspend fun createNotification(request: CreateNotificationRequest): Result<NotificationDto> {
        return repository.createNotification(request)
    }

    /**
     * Mark notification as read
     */
    suspend fun markAsRead(id: Long): Result<NotificationDto> {
        return repository.markAsRead(id)
    }

    /**
     * Delete a notification
     */
    suspend fun deleteNotification(id: Long): Result<Unit> {
        return repository.deleteNotification(id)
    }

    /**
     * Refresh notifications list
     */
    fun refresh(userId: Long? = null, isRead: Boolean? = null) {
        loadNotifications(userId, isRead)
    }
}

/**
 * Sealed class representing notification list state
 */
sealed class NotificationListState {
    object Idle : NotificationListState()
    object Loading : NotificationListState()
    data class Success(val data: List<NotificationDto>) : NotificationListState()
    data class Error(val message: String) : NotificationListState()
}

/**
 * Sealed class representing notification detail state
 */
sealed class NotificationDetailState {
    object Idle : NotificationDetailState()
    object Loading : NotificationDetailState()
    data class Success(val data: NotificationDto) : NotificationDetailState()
    data class Error(val message: String) : NotificationDetailState()
}
