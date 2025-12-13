package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.NotesApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateNoteRequest
import com.mgb.mrfcmanager.data.remote.dto.NotesDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateNoteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for notes operations
 * Handles all notes-related API calls
 */
class NotesRepository(private val apiService: NotesApiService) {

    /**
     * Get all notes with optional filters
     */
    suspend fun getAllNotes(
        mrfcId: Long? = null,
        agendaId: Long? = null
    ): Result<List<NotesDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllNotes(mrfcId, agendaId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data.notes)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch notes")
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
     * Get note by ID
     */
    suspend fun getNoteById(id: Long): Result<NotesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNoteById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to fetch note")
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
     * Create a new note
     */
    suspend fun createNote(request: CreateNoteRequest): Result<NotesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createNote(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to save note")
                    }
                } else {
                    // Better error messages based on HTTP status
                    val errorMessage = when (response.code()) {
                        401 -> "Session expired. Please log in again."
                        429 -> "Too many requests. Please wait and try again."
                        500, 502, 503, 504 -> "Server error. Please try again later."
                        else -> "Failed to save note (Error ${response.code()})"
                    }
                    Result.Error(errorMessage)
                }
            } catch (e: Exception) {
                // Better error message for network issues
                val message = when {
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Request timed out. Check your connection and try again."
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("connect", ignoreCase = true) == true ->
                        "No internet connection. Please check your network."
                    else -> "Failed to save note. Please try again."
                }
                Result.Error(message)
            }
        }
    }

    /**
     * Update a note
     */
    suspend fun updateNote(id: Long, request: UpdateNoteRequest): Result<NotesDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateNote(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to update note")
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
     * Delete a note
     */
    suspend fun deleteNote(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteNote(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to delete note")
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
