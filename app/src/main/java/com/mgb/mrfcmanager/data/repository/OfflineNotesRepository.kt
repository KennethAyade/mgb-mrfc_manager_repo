package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.local.dao.NoteDao
import com.mgb.mrfcmanager.data.local.dao.PendingSyncDao
import com.mgb.mrfcmanager.data.local.entity.NoteEntity
import com.mgb.mrfcmanager.data.local.entity.PendingSyncEntity
import com.mgb.mrfcmanager.data.local.entity.SyncEntityType
import com.mgb.mrfcmanager.data.local.entity.SyncOperation
import com.mgb.mrfcmanager.data.local.entity.SyncStatus
import com.mgb.mrfcmanager.data.remote.api.NotesApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateNoteRequest
import com.mgb.mrfcmanager.data.remote.dto.NotesDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateNoteRequest
import com.mgb.mrfcmanager.utils.NetworkConnectivityManager
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Offline-first repository for Notes
 * Prioritizes local database for reads, syncs with server when online
 */
class OfflineNotesRepository(
    private val apiService: NotesApiService,
    private val noteDao: NoteDao,
    private val pendingSyncDao: PendingSyncDao,
    private val networkManager: NetworkConnectivityManager,
    private val moshi: Moshi
) {

    /**
     * Get all notes for a user as a Flow (reactive)
     * Returns local data immediately, syncs with server in background if online
     */
    fun getNotesFlow(userId: Long): Flow<List<NotesDto>> {
        return noteDao.getNotesByUser(userId).map { entities ->
            entities.map { it.toDto() }
        }
    }

    /**
     * Get notes for a specific meeting/agenda as a Flow
     */
    fun getNotesByAgendaFlow(userId: Long, agendaId: Long): Flow<List<NotesDto>> {
        return noteDao.getNotesByAgenda(userId, agendaId).map { entities ->
            entities.map { it.toDto() }
        }
    }

    /**
     * Search notes
     */
    fun searchNotes(userId: Long, query: String): Flow<List<NotesDto>> {
        return noteDao.searchNotes(userId, query).map { entities ->
            entities.map { it.toDto() }
        }
    }

    /**
     * Get pending sync count as a Flow
     */
    fun getPendingSyncCount(): Flow<Int> = noteDao.getPendingSyncCount()

    /**
     * Sync notes from server to local database
     * Call this on app start or when network becomes available
     */
    suspend fun syncFromServer(userId: Long, mrfcId: Long? = null, agendaId: Long? = null): Result<Unit> {
        return withContext(Dispatchers.IO) {
            if (!networkManager.isNetworkAvailable()) {
                return@withContext Result.Error("No network connection")
            }

            try {
                val response = apiService.getAllNotes(mrfcId, agendaId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val serverNotes = body.data.notes

                        // Convert to entities and save
                        val entities = serverNotes.map { NoteEntity.fromDto(it) }

                        // Clear synced notes and insert fresh data
                        noteDao.clearSyncedNotes()
                        noteDao.insertAll(entities)

                        Result.Success(Unit)
                    } else {
                        Result.Error(body?.error?.message ?: "Failed to sync notes")
                    }
                } else {
                    Result.Error("HTTP ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error(e.localizedMessage ?: "Sync failed")
            }
        }
    }

    /**
     * Create a new note (offline-first)
     * Creates locally first, syncs to server if online
     */
    suspend fun createNote(
        userId: Long,
        request: CreateNoteRequest
    ): Result<NotesDto> {
        return withContext(Dispatchers.IO) {
            // Generate local ID for offline-created note
            val localId = NoteEntity.generateLocalId()
            val now = System.currentTimeMillis()
            val nowString = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .format(java.util.Date(now))

            // Create local entity
            val localEntity = NoteEntity(
                id = localId,
                userId = userId,
                mrfcId = request.mrfcId,
                quarterId = request.quarterId,
                agendaId = request.agendaId,
                title = request.title,
                content = request.content,
                isPinned = request.isPinned,
                createdAt = nowString,
                updatedAt = nowString,
                syncStatus = if (networkManager.isNetworkAvailable()) SyncStatus.SYNCED else SyncStatus.PENDING_CREATE
            )

            if (networkManager.isNetworkAvailable()) {
                // Try to sync immediately
                try {
                    val response = apiService.createNote(request)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true && body.data != null) {
                            // Save with server ID
                            val serverEntity = NoteEntity.fromDto(body.data)
                            noteDao.insert(serverEntity)
                            return@withContext Result.Success(body.data)
                        }
                    }

                    // Server sync failed, save locally with pending status
                    noteDao.insert(localEntity.copy(syncStatus = SyncStatus.PENDING_CREATE))
                    queueForSync(localEntity.id, SyncOperation.CREATE, request)
                    return@withContext Result.Success(localEntity.toDto())

                } catch (e: Exception) {
                    // Network error, save locally
                    noteDao.insert(localEntity.copy(syncStatus = SyncStatus.PENDING_CREATE))
                    queueForSync(localEntity.id, SyncOperation.CREATE, request)
                    return@withContext Result.Success(localEntity.toDto())
                }
            } else {
                // Offline, save locally and queue for sync
                noteDao.insert(localEntity)
                queueForSync(localEntity.id, SyncOperation.CREATE, request)
                return@withContext Result.Success(localEntity.toDto())
            }
        }
    }

    /**
     * Update a note (offline-first)
     */
    suspend fun updateNote(
        noteId: Long,
        request: UpdateNoteRequest
    ): Result<NotesDto> {
        return withContext(Dispatchers.IO) {
            // Get existing note
            val existingNote = noteDao.getNoteById(noteId)
                ?: return@withContext Result.Error("Note not found")

            val now = System.currentTimeMillis()
            val nowString = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .format(java.util.Date(now))

            // Update local entity
            val updatedEntity = existingNote.copy(
                title = request.title ?: existingNote.title,
                content = request.content ?: existingNote.content,
                isPinned = request.isPinned ?: existingNote.isPinned,
                updatedAt = nowString,
                syncStatus = if (networkManager.isNetworkAvailable()) SyncStatus.SYNCED else SyncStatus.PENDING_UPDATE,
                localUpdatedAt = now
            )

            if (networkManager.isNetworkAvailable() && noteId > 0) { // Only sync if not a local-only note
                try {
                    val response = apiService.updateNote(noteId, request)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true && body.data != null) {
                            val serverEntity = NoteEntity.fromDto(body.data)
                            noteDao.insert(serverEntity)
                            return@withContext Result.Success(body.data)
                        }
                    }

                    // Server sync failed
                    noteDao.insert(updatedEntity.copy(syncStatus = SyncStatus.PENDING_UPDATE))
                    queueForSync(noteId, SyncOperation.UPDATE, request)
                    return@withContext Result.Success(updatedEntity.toDto())

                } catch (e: Exception) {
                    noteDao.insert(updatedEntity.copy(syncStatus = SyncStatus.PENDING_UPDATE))
                    queueForSync(noteId, SyncOperation.UPDATE, request)
                    return@withContext Result.Success(updatedEntity.toDto())
                }
            } else {
                // Offline or local-only note
                noteDao.insert(updatedEntity)
                if (noteId > 0) {
                    queueForSync(noteId, SyncOperation.UPDATE, request)
                }
                return@withContext Result.Success(updatedEntity.toDto())
            }
        }
    }

    /**
     * Delete a note (offline-first)
     */
    suspend fun deleteNote(noteId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            if (noteId < 0) {
                // Local-only note, just delete it
                noteDao.deleteById(noteId)
                pendingSyncDao.deleteForEntity(SyncEntityType.NOTE, noteId)
                return@withContext Result.Success(Unit)
            }

            if (networkManager.isNetworkAvailable()) {
                try {
                    val response = apiService.deleteNote(noteId)

                    if (response.isSuccessful) {
                        noteDao.deleteById(noteId)
                        return@withContext Result.Success(Unit)
                    }

                    // Server delete failed, mark for deletion
                    noteDao.updateSyncStatus(noteId, SyncStatus.PENDING_DELETE)
                    queueForSync(noteId, SyncOperation.DELETE, null)
                    return@withContext Result.Success(Unit)

                } catch (e: Exception) {
                    noteDao.updateSyncStatus(noteId, SyncStatus.PENDING_DELETE)
                    queueForSync(noteId, SyncOperation.DELETE, null)
                    return@withContext Result.Success(Unit)
                }
            } else {
                // Offline, mark for deletion
                noteDao.updateSyncStatus(noteId, SyncStatus.PENDING_DELETE)
                queueForSync(noteId, SyncOperation.DELETE, null)
                return@withContext Result.Success(Unit)
            }
        }
    }

    /**
     * Sync all pending changes to server
     * Call this when network becomes available
     */
    suspend fun syncPendingChanges(): Result<Int> {
        return withContext(Dispatchers.IO) {
            if (!networkManager.isNetworkAvailable()) {
                return@withContext Result.Error("No network connection")
            }

            val pendingNotes = noteDao.getPendingSyncNotes()
            var syncedCount = 0
            var errors = mutableListOf<String>()

            for (note in pendingNotes) {
                try {
                    when (note.syncStatus) {
                        SyncStatus.PENDING_CREATE -> {
                            val request = CreateNoteRequest(
                                mrfcId = note.mrfcId,
                                quarterId = note.quarterId,
                                agendaId = note.agendaId,
                                title = note.title,
                                content = note.content ?: "",
                                isPinned = note.isPinned
                            )
                            val response = apiService.createNote(request)
                            if (response.isSuccessful && response.body()?.success == true) {
                                response.body()?.data?.let { serverNote ->
                                    // Update with server ID
                                    noteDao.deleteById(note.id)
                                    noteDao.insert(NoteEntity.fromDto(serverNote))
                                    pendingSyncDao.deleteForEntity(SyncEntityType.NOTE, note.id)
                                    syncedCount++
                                }
                            }
                        }
                        SyncStatus.PENDING_UPDATE -> {
                            val request = UpdateNoteRequest(
                                title = note.title,
                                content = note.content,
                                isPinned = note.isPinned
                            )
                            val response = apiService.updateNote(note.id, request)
                            if (response.isSuccessful && response.body()?.success == true) {
                                response.body()?.data?.let { serverNote ->
                                    noteDao.insert(NoteEntity.fromDto(serverNote))
                                    pendingSyncDao.deleteForEntity(SyncEntityType.NOTE, note.id)
                                    syncedCount++
                                }
                            }
                        }
                        SyncStatus.PENDING_DELETE -> {
                            val response = apiService.deleteNote(note.id)
                            if (response.isSuccessful && response.body()?.success == true) {
                                noteDao.deleteById(note.id)
                                pendingSyncDao.deleteForEntity(SyncEntityType.NOTE, note.id)
                                syncedCount++
                            }
                        }
                        SyncStatus.SYNCED -> { /* Already synced */ }
                    }
                } catch (e: Exception) {
                    errors.add("Failed to sync note ${note.id}: ${e.message}")
                }
            }

            if (errors.isNotEmpty()) {
                Result.Error("Synced $syncedCount notes with ${errors.size} errors")
            } else {
                Result.Success(syncedCount)
            }
        }
    }

    /**
     * Queue an operation for later sync
     */
    private suspend fun queueForSync(entityId: Long, operation: SyncOperation, payload: Any?) {
        val payloadJson = payload?.let {
            try {
                moshi.adapter(Any::class.java).toJson(it)
            } catch (e: Exception) {
                null
            }
        }

        val pendingSync = PendingSyncEntity(
            entityType = SyncEntityType.NOTE,
            entityId = entityId,
            operation = operation,
            payload = payloadJson
        )

        pendingSyncDao.insert(pendingSync)
    }
}
