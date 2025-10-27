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
        agendaId: Long? = null,
        noteType: String? = null,
        isPrivate: Boolean? = null
    ): Result<List<NotesDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllNotes(mrfcId, agendaId, noteType, isPrivate)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
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
                        Result.Error(body?.error?.message ?: "Failed to create note")
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
