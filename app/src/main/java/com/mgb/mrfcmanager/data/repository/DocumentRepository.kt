package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.DocumentApiService
import com.mgb.mrfcmanager.data.remote.dto.DocumentCategory
import com.mgb.mrfcmanager.data.remote.dto.DocumentDto
import com.mgb.mrfcmanager.data.remote.dto.DocumentUploadResponse
import com.mgb.mrfcmanager.data.remote.dto.UpdateDocumentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File

/**
 * Repository for document operations
 * Handles all document-related API calls
 */
class DocumentRepository(private val apiService: DocumentApiService) {

    /**
     * Get documents by MRFC ID
     */
    suspend fun getDocumentsByMrfc(
        mrfcId: Long,
        category: String? = null,
        status: String? = null
    ): Result<List<DocumentDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocumentsByMrfc(
                    mrfcId = mrfcId,
                    category = category,
                    status = status
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        // Return empty list if data is null
                        Result.Success(body.data ?: emptyList())
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch documents")
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
     * Get documents by Proponent ID
     */
    suspend fun getDocumentsByProponent(
        proponentId: Long,
        category: String? = null,
        status: String? = null
    ): Result<List<DocumentDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocumentsByProponent(
                    proponentId = proponentId,
                    category = category,
                    status = status
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        // Return empty list if data is null
                        Result.Success(body.data ?: emptyList())
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch documents")
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
     * Get document by ID
     */
    suspend fun getDocumentById(id: Long): Result<DocumentDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocumentById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch document")
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
     * Upload a document file
     */
    suspend fun uploadDocument(
        file: File,
        category: DocumentCategory,
        mrfcId: Long? = null,
        proponentId: Long? = null,
        quarterId: Long? = null,
        description: String? = null
    ): Result<DocumentUploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Determine MIME type from file extension
                val mimeType = getMimeType(file.name)

                // Create file part
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Create other parts - category is required, others are optional
                val categoryPart = category.name.toRequestBody("text/plain".toMediaTypeOrNull())
                val mrfcIdPart = mrfcId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val proponentIdPart = proponentId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val quarterIdPart = quarterId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadDocument(
                    file = filePart,
                    category = categoryPart,
                    mrfcId = mrfcIdPart,
                    proponentId = proponentIdPart,
                    quarterId = quarterIdPart,
                    description = descriptionPart
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to upload document")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Upload error occurred")
            }
        }
    }

    /**
     * Update document metadata
     */
    suspend fun updateDocument(id: Long, request: UpdateDocumentRequest): Result<DocumentDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateDocument(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to update document")
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
     * Delete a document
     */
    suspend fun deleteDocument(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteDocument(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to delete document")
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
     * Download document (returns download URL from backend)
     */
    suspend fun downloadDocument(id: Long): Result<Map<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.downloadDocument(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to get download URL")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Download error occurred")
            }
        }
    }

    /**
     * Get MIME type from file extension
     */
    private fun getMimeType(filename: String): String {
        return when (filename.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            else -> "application/octet-stream"
        }
    }
}
