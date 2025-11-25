package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.ApiResponse
import com.mgb.mrfcmanager.data.remote.dto.CreateNoteRequest
import com.mgb.mrfcmanager.data.remote.dto.NotesDto
import com.mgb.mrfcmanager.data.remote.dto.PaginatedNotesResponse
import com.mgb.mrfcmanager.data.remote.dto.UpdateNoteRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for notes operations
 */
interface NotesApiService {

    /**
     * Get all notes with optional filters
     */
    @GET("notes")
    suspend fun getAllNotes(
        @Query("mrfc_id") mrfcId: Long? = null,
        @Query("agenda_id") agendaId: Long? = null
    ): Response<ApiResponse<PaginatedNotesResponse>>

    /**
     * Get note by ID
     */
    @GET("notes/{id}")
    suspend fun getNoteById(
        @Path("id") id: Long
    ): Response<ApiResponse<NotesDto>>

    /**
     * Create a new note
     */
    @POST("notes")
    suspend fun createNote(
        @Body request: CreateNoteRequest
    ): Response<ApiResponse<NotesDto>>

    /**
     * Update a note
     */
    @PUT("notes/{id}")
    suspend fun updateNote(
        @Path("id") id: Long,
        @Body request: UpdateNoteRequest
    ): Response<ApiResponse<NotesDto>>

    /**
     * Delete a note
     */
    @DELETE("notes/{id}")
    suspend fun deleteNote(
        @Path("id") id: Long
    ): Response<ApiResponse<Any?>>
}
