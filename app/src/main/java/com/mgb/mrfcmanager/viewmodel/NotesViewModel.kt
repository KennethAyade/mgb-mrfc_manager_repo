package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.CreateNoteRequest
import com.mgb.mrfcmanager.data.remote.dto.NotesDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateNoteRequest
import com.mgb.mrfcmanager.data.repository.OfflineNotesRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for Notes management
 * Supports offline-first operations with local persistence
 */
class NotesViewModel(
    private val repository: OfflineNotesRepository,
    private val userId: Long
) : ViewModel() {

    private val _notesListState = MutableLiveData<NotesListState>()
    val notesListState: LiveData<NotesListState> = _notesListState

    private val _noteDetailState = MutableLiveData<NoteDetailState>()
    val noteDetailState: LiveData<NoteDetailState> = _noteDetailState

    private val _saveState = MutableLiveData<SaveState>()
    val saveState: LiveData<SaveState> = _saveState

    // Current filter context
    private var currentMrfcId: Long? = null
    private var currentAgendaId: Long? = null

    /**
     * Load all notes with optional filters
     * First loads from local cache, then syncs with server
     */
    fun loadNotes(
        mrfcId: Long? = null,
        agendaId: Long? = null
    ) {
        currentMrfcId = mrfcId
        currentAgendaId = agendaId
        _notesListState.value = NotesListState.Loading

        viewModelScope.launch {
            // Observe local notes flow
            if (agendaId != null && agendaId > 0) {
                repository.getNotesByAgendaFlow(userId, agendaId).collectLatest { notes ->
                    _notesListState.value = NotesListState.Success(notes)
                }
            } else {
                repository.getNotesFlow(userId).collectLatest { notes ->
                    _notesListState.value = NotesListState.Success(notes)
                }
            }
        }

        // Sync from server in background
        viewModelScope.launch {
            repository.syncFromServer(userId, mrfcId, agendaId)
        }
    }

    /**
     * Load notes by MRFC ID
     */
    fun loadNotesByMrfc(mrfcId: Long) {
        loadNotes(mrfcId = mrfcId)
    }

    /**
     * Load notes by Agenda ID
     */
    fun loadNotesByAgenda(agendaId: Long) {
        loadNotes(agendaId = agendaId)
    }

    /**
     * Create a new note (offline-first)
     * Note is saved locally first, then synced to server
     */
    suspend fun createNote(request: CreateNoteRequest): Result<NotesDto> {
        _saveState.postValue(SaveState.Saving)
        return try {
            val result = repository.createNote(userId, request)
            when (result) {
                is Result.Success -> {
                    _saveState.postValue(SaveState.Success("Note saved"))
                }
                is Result.Error -> {
                    _saveState.postValue(SaveState.Error(result.message))
                }
                is Result.Loading -> {}
            }
            result
        } catch (e: Exception) {
            val error = Result.Error(e.message ?: "Failed to save note")
            _saveState.postValue(SaveState.Error(error.message))
            error
        }
    }

    /**
     * Update a note (offline-first)
     */
    suspend fun updateNote(id: Long, request: UpdateNoteRequest): Result<NotesDto> {
        _saveState.postValue(SaveState.Saving)
        return try {
            val result = repository.updateNote(id, request)
            when (result) {
                is Result.Success -> {
                    _saveState.postValue(SaveState.Success("Note updated"))
                }
                is Result.Error -> {
                    _saveState.postValue(SaveState.Error(result.message))
                }
                is Result.Loading -> {}
            }
            result
        } catch (e: Exception) {
            val error = Result.Error(e.message ?: "Failed to update note")
            _saveState.postValue(SaveState.Error(error.message))
            error
        }
    }

    /**
     * Delete a note (offline-first)
     */
    suspend fun deleteNote(id: Long): Result<Unit> {
        return repository.deleteNote(id)
    }

    /**
     * Sync pending changes to server
     */
    fun syncPendingChanges() {
        viewModelScope.launch {
            repository.syncPendingChanges()
        }
    }

    /**
     * Refresh notes list from server
     */
    fun refresh(
        mrfcId: Long? = null,
        agendaId: Long? = null
    ) {
        viewModelScope.launch {
            repository.syncFromServer(userId, mrfcId ?: currentMrfcId, agendaId ?: currentAgendaId)
        }
    }

    /**
     * Clear save state
     */
    fun clearSaveState() {
        _saveState.value = SaveState.Idle
    }
}

/**
 * Sealed class representing notes list state
 */
sealed class NotesListState {
    object Idle : NotesListState()
    object Loading : NotesListState()
    data class Success(val data: List<NotesDto>) : NotesListState()
    data class Error(val message: String) : NotesListState()
}

/**
 * Sealed class representing note detail state
 */
sealed class NoteDetailState {
    object Idle : NoteDetailState()
    object Loading : NoteDetailState()
    data class Success(val data: NotesDto) : NoteDetailState()
    data class Error(val message: String) : NoteDetailState()
}

/**
 * Sealed class representing save operation state
 */
sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val message: String) : SaveState()
    data class Error(val message: String) : SaveState()
}
