package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.AttendanceApiService
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAttendanceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Repository for attendance operations
 * Handles all attendance-related API calls including photo uploads
 */
class AttendanceRepository(private val apiService: AttendanceApiService) {

    /**
     * Get all attendance records with optional filters
     */
    suspend fun getAllAttendance(
        agendaId: Long? = null,
        userId: Long? = null
    ): Result<List<AttendanceDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllAttendance(agendaId, userId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch attendance records")
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
     * Get attendance record by ID
     */
    suspend fun getAttendanceById(id: Long): Result<AttendanceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAttendanceById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch attendance record")
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
     * Create a new attendance record
     */
    suspend fun createAttendance(request: CreateAttendanceRequest): Result<AttendanceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createAttendance(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to create attendance record")
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
     * Upload photo for attendance record
     */
    suspend fun uploadPhoto(attendanceId: Long, photoFile: File): Result<AttendanceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)

                val response = apiService.uploadPhoto(attendanceId, photoPart)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to upload photo")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Photo upload error occurred")
            }
        }
    }

    /**
     * Update an attendance record
     */
    suspend fun updateAttendance(id: Long, request: CreateAttendanceRequest): Result<AttendanceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateAttendance(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to update attendance record")
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
     * Delete an attendance record
     */
    suspend fun deleteAttendance(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteAttendance(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to delete attendance record")
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
