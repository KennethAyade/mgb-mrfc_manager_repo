package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.AttendanceApiService
import com.mgb.mrfcmanager.data.remote.api.UpdateAttendanceRequest
import com.mgb.mrfcmanager.data.remote.dto.AttendanceDto
import com.mgb.mrfcmanager.data.remote.dto.AttendanceListResponse
import com.mgb.mrfcmanager.data.remote.dto.CreateAttendanceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Repository for attendance operations
 * Handles all attendance-related API calls including photo uploads
 * Updated to match tested backend endpoints
 */
class AttendanceRepository(private val apiService: AttendanceApiService) {

    /**
     * Get attendance records for a specific meeting with summary statistics
     * Uses the new endpoint: GET /attendance/meeting/:agendaId
     */
    suspend fun getAttendanceByMeeting(agendaId: Long): Result<AttendanceListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAttendanceByMeeting(agendaId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to fetch attendance records",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error. Please check your connection.",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Create attendance record without photo
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
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create attendance record",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Create attendance record with photo upload
     * Uses multipart/form-data for photo upload
     */
    suspend fun createAttendanceWithPhoto(
        agendaId: Long,
        proponentId: Long? = null,
        attendeeName: String? = null,
        attendeePosition: String? = null,
        attendeeDepartment: String? = null,
        isPresent: Boolean = true,
        remarks: String? = null,
        photoFile: File? = null
    ): Result<AttendanceDto> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert all parameters to RequestBody
                val agendaIdBody = agendaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val isPresentBody = isPresent.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val proponentIdBody = proponentId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val attendeeNameBody = attendeeName?.toRequestBody("text/plain".toMediaTypeOrNull())
                val attendeePositionBody = attendeePosition?.toRequestBody("text/plain".toMediaTypeOrNull())
                val attendeeDepartmentBody = attendeeDepartment?.toRequestBody("text/plain".toMediaTypeOrNull())
                val remarksBody = remarks?.toRequestBody("text/plain".toMediaTypeOrNull())

                // Prepare photo part if file exists
                val photoPart = photoFile?.let { file ->
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photo", file.name, requestFile)
                }

                val response = apiService.createAttendanceWithPhoto(
                    agendaId = agendaIdBody,
                    proponentId = proponentIdBody,
                    attendeeName = attendeeNameBody,
                    attendeePosition = attendeePositionBody,
                    attendeeDepartment = attendeeDepartmentBody,
                    isPresent = isPresentBody,
                    remarks = remarksBody,
                    photo = photoPart
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create attendance with photo",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Photo upload error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Update attendance record (only status and remarks can be updated)
     */
    suspend fun updateAttendance(
        id: Long,
        isPresent: Boolean? = null,
        remarks: String? = null
    ): Result<AttendanceDto> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateAttendanceRequest(
                    isPresent = isPresent,
                    remarks = remarks
                )
                val response = apiService.updateAttendance(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to update attendance record",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Delete attendance record
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
                        Result.Error(
                            message = body?.error?.message ?: "Failed to delete attendance record",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }
}
