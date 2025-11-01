package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto
import com.mgb.mrfcmanager.data.remote.dto.AttendanceListResponse
import com.mgb.mrfcmanager.data.remote.dto.AttendanceSummary
import com.mgb.mrfcmanager.data.remote.dto.CreateAttendanceRequest
import com.mgb.mrfcmanager.data.repository.AttendanceRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for Attendance management
 * Handles attendance state and operations including photo uploads
 * Updated to match tested backend with summary statistics
 */
class AttendanceViewModel(private val repository: AttendanceRepository) : ViewModel() {

    private val _attendanceListState = MutableLiveData<AttendanceListState>()
    val attendanceListState: LiveData<AttendanceListState> = _attendanceListState

    private val _attendanceSummary = MutableLiveData<AttendanceSummary?>()
    val attendanceSummary: LiveData<AttendanceSummary?> = _attendanceSummary

    private val _photoUploadState = MutableLiveData<PhotoUploadState>()
    val photoUploadState: LiveData<PhotoUploadState> = _photoUploadState

    /**
     * Load attendance records for a specific meeting with summary statistics
     */
    fun loadAttendanceByMeeting(agendaId: Long) {
        _attendanceListState.value = AttendanceListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAttendanceByMeeting(agendaId)) {
                is Result.Success -> {
                    _attendanceListState.value = AttendanceListState.Success(result.data.attendance)
                    _attendanceSummary.value = result.data.summary
                }
                is Result.Error -> {
                    _attendanceListState.value = AttendanceListState.Error(result.message)
                    _attendanceSummary.value = null
                }
                is Result.Loading -> {
                    _attendanceListState.value = AttendanceListState.Loading
                }
            }
        }
    }

    /**
     * Create attendance record without photo
     */
    fun createAttendance(
        agendaId: Long,
        proponentId: Long? = null,
        attendeeName: String? = null,
        attendeePosition: String? = null,
        attendeeDepartment: String? = null,
        isPresent: Boolean = true,
        remarks: String? = null,
        onComplete: (Result<AttendanceDto>) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateAttendanceRequest(
                agendaId = agendaId,
                proponentId = proponentId,
                attendeeName = attendeeName,
                attendeePosition = attendeePosition,
                attendeeDepartment = attendeeDepartment,
                isPresent = isPresent,
                remarks = remarks
            )
            val result = repository.createAttendance(request)
            onComplete(result)

            // Reload the list
            if (result is Result.Success) {
                loadAttendanceByMeeting(agendaId)
            }
        }
    }

    /**
     * Create attendance record with photo upload
     */
    fun createAttendanceWithPhoto(
        agendaId: Long,
        proponentId: Long? = null,
        attendeeName: String? = null,
        attendeePosition: String? = null,
        attendeeDepartment: String? = null,
        isPresent: Boolean = true,
        remarks: String? = null,
        photoFile: File
    ) {
        _photoUploadState.value = PhotoUploadState.Uploading

        viewModelScope.launch {
            when (val result = repository.createAttendanceWithPhoto(
                agendaId = agendaId,
                proponentId = proponentId,
                attendeeName = attendeeName,
                attendeePosition = attendeePosition,
                attendeeDepartment = attendeeDepartment,
                isPresent = isPresent,
                remarks = remarks,
                photoFile = photoFile
            )) {
                is Result.Success -> {
                    _photoUploadState.value = PhotoUploadState.Success(result.data)
                    // Reload the list
                    loadAttendanceByMeeting(agendaId)
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
     * Update attendance record (only status and remarks)
     */
    fun updateAttendance(
        id: Long,
        agendaId: Long,
        isPresent: Boolean? = null,
        remarks: String? = null,
        onComplete: (Result<AttendanceDto>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.updateAttendance(id, isPresent, remarks)
            onComplete(result)

            // Reload the list
            if (result is Result.Success) {
                loadAttendanceByMeeting(agendaId)
            }
        }
    }

    /**
     * Delete attendance record
     */
    fun deleteAttendance(id: Long, agendaId: Long, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteAttendance(id)
            onComplete(result)

            // Reload the list
            if (result is Result.Success) {
                loadAttendanceByMeeting(agendaId)
            }
        }
    }

    /**
     * Refresh attendance list
     */
    fun refresh(agendaId: Long) {
        loadAttendanceByMeeting(agendaId)
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
 * Sealed class representing photo upload state
 */
sealed class PhotoUploadState {
    object Idle : PhotoUploadState()
    object Uploading : PhotoUploadState()
    data class Success(val data: AttendanceDto) : PhotoUploadState()
    data class Error(val message: String) : PhotoUploadState()
}
