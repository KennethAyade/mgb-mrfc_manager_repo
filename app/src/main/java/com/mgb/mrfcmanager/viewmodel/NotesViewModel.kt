package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.CreateNoteRequest
import com.mgb.mrfcmanager.data.remote.dto.NotesDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateNoteRequest
import com.mgb.mrfcmanager.data.repository.NotesRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Notes management
 * Handles notes state and operations
 */
class NotesViewModel(private val repository: NotesRepository) : ViewModel() {

    private val _notesListState = MutableLiveData<NotesListState>()
    val notesListState: LiveData<NotesListState> = _notesListState

    private val _noteDetailState = MutableLiveData<NoteDetailState>()
    val noteDetailState: LiveData<NoteDetailState> = _noteDetailState

    /**
     * Load all notes with optional filters
     */
    fun loadNotes(
        mrfcId: Long? = null,
        agendaId: Long? = null
    ) {
        _notesListState.value = NotesListState.Loading

        viewModelScope.launch {
            when (val result = repository.getAllNotes(mrfcId, agendaId)) {
                is Result.Success -> {
                    _notesListState.value = NotesListState.Success(result.data)
                }
                is Result.Error -> {
                    _notesListState.value = NotesListState.Error(result.message)
                }
                is Result.Loading -> {
                    _notesListState.value = NotesListState.Loading
                }
            }
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
     * Load note by ID
     */
    fun loadNoteById(id: Long) {
        _noteDetailState.value = NoteDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getNoteById(id)) {
                is Result.Success -> {
                    _noteDetailState.value = NoteDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _noteDetailState.value = NoteDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _noteDetailState.value = NoteDetailState.Loading
                }
            }
        }
    }

    /**
     * Create a new note
     */
    suspend fun createNote(request: CreateNoteRequest): Result<NotesDto> {
        return repository.createNote(request)
    }

    /**
     * Update a note
     */
    suspend fun updateNote(id: Long, request: UpdateNoteRequest): Result<NotesDto> {
        return repository.updateNote(id, request)
    }

    /**
     * Delete a note
     */
    suspend fun deleteNote(id: Long): Result<Unit> {
        return repository.deleteNote(id)
    }

    /**
     * Refresh notes list
     */
    fun refresh(
        mrfcId: Long? = null,
        agendaId: Long? = null
    ) {
        loadNotes(mrfcId, agendaId)
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
