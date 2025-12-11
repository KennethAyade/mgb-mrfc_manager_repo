package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.VoiceRecordingDto
import com.mgb.mrfcmanager.data.repository.VoiceRecordingRepository
import kotlinx.coroutines.launch
import java.io.File

/**
 * State for voice recordings list
 */
sealed class VoiceRecordingsState {
    object Idle : VoiceRecordingsState()
    object Loading : VoiceRecordingsState()
    data class Success(val recordings: List<VoiceRecordingDto>) : VoiceRecordingsState()
    data class Error(val message: String) : VoiceRecordingsState()
}

/**
 * State for upload operation
 */
sealed class VoiceRecordingUploadState {
    object Idle : VoiceRecordingUploadState()
    object Uploading : VoiceRecordingUploadState()
    data class Success(val recording: VoiceRecordingDto) : VoiceRecordingUploadState()
    data class Error(val message: String) : VoiceRecordingUploadState()
}

/**
 * State for delete operation
 */
sealed class VoiceRecordingDeleteState {
    object Idle : VoiceRecordingDeleteState()
    object Deleting : VoiceRecordingDeleteState()
    data class Success(val message: String) : VoiceRecordingDeleteState()
    data class Error(val message: String) : VoiceRecordingDeleteState()
}

/**
 * ViewModel for voice recording operations
 */
class VoiceRecordingViewModel(
    private val repository: VoiceRecordingRepository
) : ViewModel() {

    private val _recordingsState = MutableLiveData<VoiceRecordingsState>(VoiceRecordingsState.Idle)
    val recordingsState: LiveData<VoiceRecordingsState> = _recordingsState

    private val _uploadState = MutableLiveData<VoiceRecordingUploadState>(VoiceRecordingUploadState.Idle)
    val uploadState: LiveData<VoiceRecordingUploadState> = _uploadState

    private val _deleteState = MutableLiveData<VoiceRecordingDeleteState>(VoiceRecordingDeleteState.Idle)
    val deleteState: LiveData<VoiceRecordingDeleteState> = _deleteState

    /**
     * Load all voice recordings for a meeting/agenda
     */
    fun loadVoiceRecordings(agendaId: Long) {
        _recordingsState.value = VoiceRecordingsState.Loading

        viewModelScope.launch {
            try {
                val response = repository.getVoiceRecordingsByAgenda(agendaId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val recordings = response.body()?.data?.recordings ?: emptyList()
                    _recordingsState.value = VoiceRecordingsState.Success(recordings)
                } else {
                    val errorMessage = response.body()?.error?.message
                        ?: response.errorBody()?.string()
                        ?: "Failed to load voice recordings"
                    _recordingsState.value = VoiceRecordingsState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _recordingsState.value = VoiceRecordingsState.Error(
                    e.message ?: "Network error occurred"
                )
            }
        }
    }

    /**
     * Upload a new voice recording
     */
    fun uploadVoiceRecording(
        audioFile: File,
        agendaId: Long,
        recordingName: String,
        description: String? = null,
        durationSeconds: Int? = null
    ) {
        _uploadState.value = VoiceRecordingUploadState.Uploading

        viewModelScope.launch {
            try {
                val response = repository.uploadVoiceRecording(
                    audioFile = audioFile,
                    agendaId = agendaId,
                    recordingName = recordingName,
                    description = description,
                    durationSeconds = durationSeconds
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val recording = response.body()?.data
                    if (recording != null) {
                        _uploadState.value = VoiceRecordingUploadState.Success(recording)
                        // Refresh the recordings list
                        loadVoiceRecordings(agendaId)
                    } else {
                        _uploadState.value = VoiceRecordingUploadState.Error("No data returned")
                    }
                } else {
                    val errorMessage = response.body()?.error?.message
                        ?: response.errorBody()?.string()
                        ?: "Failed to upload voice recording"
                    _uploadState.value = VoiceRecordingUploadState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uploadState.value = VoiceRecordingUploadState.Error(
                    e.message ?: "Network error occurred"
                )
            }
        }
    }

    /**
     * Delete a voice recording
     */
    fun deleteVoiceRecording(id: Long, agendaId: Long) {
        _deleteState.value = VoiceRecordingDeleteState.Deleting

        viewModelScope.launch {
            try {
                val response = repository.deleteVoiceRecording(id)

                if (response.isSuccessful && response.body()?.success == true) {
                    _deleteState.value = VoiceRecordingDeleteState.Success(
                        response.body()?.message ?: "Recording deleted successfully"
                    )
                    // Refresh the recordings list
                    loadVoiceRecordings(agendaId)
                } else {
                    val errorMessage = response.body()?.error?.message
                        ?: response.errorBody()?.string()
                        ?: "Failed to delete voice recording"
                    _deleteState.value = VoiceRecordingDeleteState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _deleteState.value = VoiceRecordingDeleteState.Error(
                    e.message ?: "Network error occurred"
                )
            }
        }
    }

    /**
     * Reset upload state
     */
    fun resetUploadState() {
        _uploadState.value = VoiceRecordingUploadState.Idle
    }

    /**
     * Reset delete state
     */
    fun resetDeleteState() {
        _deleteState.value = VoiceRecordingDeleteState.Idle
    }
}

/**
 * Factory for creating VoiceRecordingViewModel
 */
class VoiceRecordingViewModelFactory(
    private val repository: VoiceRecordingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoiceRecordingViewModel::class.java)) {
            return VoiceRecordingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
