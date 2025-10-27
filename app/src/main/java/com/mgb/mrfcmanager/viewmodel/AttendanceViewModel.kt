package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAttendanceRequest
import com.mgb.mrfcmanager.data.repository.AttendanceRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for Attendance management
 * Handles attendance state and operations including photo uploads
 */
class AttendanceViewModel(private val repository: AttendanceRepository) : ViewModel() {

    private val _attendanceListState = MutableLiveData<AttendanceListState>()
    val attendanceListState: LiveData<AttendanceListState> = _attendanceListState

    private val _attendanceDetailState = MutableLiveData<AttendanceDetailState>()
    val attendanceDetailState: LiveData<AttendanceDetailState> = _attendanceDetailState

    private val _photoUploadState = MutableLiveData<PhotoUploadState>()
    val photoUploadState: LiveData<PhotoUploadState> = _photoUploadState

    /**
     * Load all attendance records with optional filters
     */
    fun loadAttendance(agendaId: Long? = null, userId: Long? = null) {
        _attendanceListState.value = AttendanceListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllAttendance(agendaId, userId)) {
                is Result.Success -> {
                    _attendanceListState.value = AttendanceListState.Success(result.data)
                }
                is Result.Error -> {
                    _attendanceListState.value = AttendanceListState.Error(result.message)
                }
                is Result.Loading -> {
                    _attendanceListState.value = AttendanceListState.Loading
                }
            }
        }
    }

    /**
     * Load attendance record by ID
     */
    fun loadAttendanceById(id: Long) {
        _attendanceDetailState.value = AttendanceDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getAttendanceById(id)) {
                is Result.Success -> {
                    _attendanceDetailState.value = AttendanceDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _attendanceDetailState.value = AttendanceDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _attendanceDetailState.value = AttendanceDetailState.Loading
                }
            }
        }
    }

    /**
     * Create a new attendance record
     */
    suspend fun createAttendance(request: CreateAttendanceRequest): Result<AttendanceDto> {
        return repository.createAttendance(request)
    }

    /**
     * Upload photo for attendance record
     */
    fun uploadPhoto(attendanceId: Long, photoFile: File) {
        _photoUploadState.value = PhotoUploadState.Uploading

        viewModelScope.launch {
            when (val result = repository.uploadPhoto(attendanceId, photoFile)) {
                is Result.Success -> {
                    _photoUploadState.value = PhotoUploadState.Success(result.data)
                }
                is Result.Error -> {
                    _photoUploadState.value = PhotoUploadState.Error(result.message)
                }
                is Result.Loading -> {
                    _photoUploadState.value = PhotoUploadState.Uploading
                }
            }
        }
    }

    /**
     * Update an attendance record
     */
    suspend fun updateAttendance(id: Long, request: CreateAttendanceRequest): Result<AttendanceDto> {
        return repository.updateAttendance(id, request)
    }

    /**
     * Delete an attendance record
     */
    suspend fun deleteAttendance(id: Long): Result<Unit> {
        return repository.deleteAttendance(id)
    }

    /**
     * Refresh attendance list
     */
    fun refresh(agendaId: Long? = null, userId: Long? = null) {
        loadAttendance(agendaId, userId)
    }

    /**
     * Reset photo upload state
     */
    fun resetPhotoUploadState() {
        _photoUploadState.value = PhotoUploadState.Idle
    }
}

/**
 * Sealed class representing attendance list state
 */
sealed class AttendanceListState {
    object Idle : AttendanceListState()
    object Loading : AttendanceListState()
    data class Success(val data: List<AttendanceDto>) : AttendanceListState()
    data class Error(val message: String) : AttendanceListState()
}

/**
 * Sealed class representing attendance detail state
 */
sealed class AttendanceDetailState {
    object Idle : AttendanceDetailState()
    object Loading : AttendanceDetailState()
    data class Success(val data: AttendanceDto) : AttendanceDetailState()
    data class Error(val message: String) : AttendanceDetailState()
}

/**
 * Sealed class representing photo upload state
 */
sealed class PhotoUploadState {
    object Idle : PhotoUploadState()
    object Uploading : PhotoUploadState()
    data class Success(val data: AttendanceDto) : PhotoUploadState()
    data class Error(val message: String) : PhotoUploadState()
}
