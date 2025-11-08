package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.DocumentCategory
import com.mgb.mrfcmanager.data.remote.dto.DocumentDto
import com.mgb.mrfcmanager.data.remote.dto.DocumentUploadResponse
import com.mgb.mrfcmanager.data.remote.dto.UpdateDocumentRequest
import com.mgb.mrfcmanager.data.repository.DocumentRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for Document management
 * Handles document state and operations
 */
class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {

    private val _documentListState = MutableLiveData<DocumentListState>()
    val documentListState: LiveData<DocumentListState> = _documentListState

    private val _documentDetailState = MutableLiveData<DocumentDetailState>()
    val documentDetailState: LiveData<DocumentDetailState> = _documentDetailState

    private val _uploadState = MutableLiveData<DocumentUploadState>()
    val uploadState: LiveData<DocumentUploadState> = _uploadState

    private val _uploadProgress = MutableLiveData<Int>()
    val uploadProgress: LiveData<Int> = _uploadProgress

    private val _downloadState = MutableLiveData<DocumentDownloadState>()
    val downloadState: LiveData<DocumentDownloadState> = _downloadState

    /**
     * Load documents by MRFC ID
     */
    fun loadDocumentsByMrfc(
        mrfcId: Long,
        category: String? = null,
        status: String? = null
    ) {
        _documentListState.value = DocumentListState.Loading

        viewModelScope.launch {
            when (val result = repository.getDocumentsByMrfc(mrfcId, category, status)) {
                is Result.Success -> {
                    _documentListState.value = DocumentListState.Success(result.data)
                }
                is Result.Error -> {
                    _documentListState.value = DocumentListState.Error(result.message)
                }
                is Result.Loading -> {
                    _documentListState.value = DocumentListState.Loading
                }
            }
        }
    }

    /**
     * Load documents by Proponent ID
     */
    fun loadDocumentsByProponent(
        proponentId: Long,
        category: String? = null,
        status: String? = null
    ) {
        _documentListState.value = DocumentListState.Loading

        viewModelScope.launch {
            when (val result = repository.getDocumentsByProponent(proponentId, category, status)) {
                is Result.Success -> {
                    _documentListState.value = DocumentListState.Success(result.data)
                }
                is Result.Error -> {
                    _documentListState.value = DocumentListState.Error(result.message)
                }
                is Result.Loading -> {
                    _documentListState.value = DocumentListState.Loading
                }
            }
        }
    }

    /**
     * Load document by ID
     */
    fun loadDocumentById(id: Long) {
        _documentDetailState.value = DocumentDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getDocumentById(id)) {
                is Result.Success -> {
                    _documentDetailState.value = DocumentDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _documentDetailState.value = DocumentDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _documentDetailState.value = DocumentDetailState.Loading
                }
            }
        }
    }

    /**
     * Upload a document
     */
    fun uploadDocument(
        file: File,
        category: DocumentCategory,
        mrfcId: Long? = null,
        proponentId: Long? = null,
        quarterId: Long? = null,
        description: String? = null
    ) {
        _uploadState.value = DocumentUploadState.Loading
        _uploadProgress.value = 0

        viewModelScope.launch {
            when (val result = repository.uploadDocument(
                file = file,
                category = category,
                mrfcId = mrfcId,
                proponentId = proponentId,
                quarterId = quarterId,
                description = description,
                onProgress = { progress ->
                    _uploadProgress.postValue(progress)
                }
            )) {
                is Result.Success -> {
                    _uploadProgress.postValue(100)
                    _uploadState.value = DocumentUploadState.Success(result.data)
                }
                is Result.Error -> {
                    _uploadState.value = DocumentUploadState.Error(result.message)
                }
                is Result.Loading -> {
                    _uploadState.value = DocumentUploadState.Loading
                }
            }
        }
    }

    /**
     * Update document metadata
     */
    suspend fun updateDocument(id: Long, request: UpdateDocumentRequest): Result<DocumentDto> {
        return repository.updateDocument(id, request)
    }

    /**
     * Delete a document
     */
    suspend fun deleteDocument(id: Long): Result<Unit> {
        return repository.deleteDocument(id)
    }

    /**
     * Download a document
     */
    fun downloadDocument(id: Long) {
        _downloadState.value = DocumentDownloadState.Loading

        viewModelScope.launch {
            when (val result = repository.downloadDocument(id)) {
                is Result.Success -> {
                    _downloadState.value = DocumentDownloadState.Success(result.data)
                }
                is Result.Error -> {
                    _downloadState.value = DocumentDownloadState.Error(result.message)
                }
                is Result.Loading -> {
                    _downloadState.value = DocumentDownloadState.Loading
                }
            }
        }
    }

    /**
     * Refresh document list by MRFC
     */
    fun refreshByMrfc(mrfcId: Long) {
        loadDocumentsByMrfc(mrfcId)
    }

    /**
     * Refresh document list by Proponent
     */
    fun refreshByProponent(proponentId: Long) {
        loadDocumentsByProponent(proponentId)
    }

    /**
     * Reset upload state
     */
    fun resetUploadState() {
        _uploadState.value = DocumentUploadState.Idle
    }

    /**
     * Reset download state
     */
    fun resetDownloadState() {
        _downloadState.value = DocumentDownloadState.Idle
    }
}

/**
 * Sealed class representing document list state
 */
sealed class DocumentListState {
    object Idle : DocumentListState()
    object Loading : DocumentListState()
    data class Success(val data: List<DocumentDto>) : DocumentListState()
    data class Error(val message: String) : DocumentListState()
}

/**
 * Sealed class representing document detail state
 */
sealed class DocumentDetailState {
    object Idle : DocumentDetailState()
    object Loading : DocumentDetailState()
    data class Success(val data: DocumentDto) : DocumentDetailState()
    data class Error(val message: String) : DocumentDetailState()
}

/**
 * Sealed class representing document upload state
 */
sealed class DocumentUploadState {
    object Idle : DocumentUploadState()
    object Loading : DocumentUploadState()
    data class Success(val data: DocumentUploadResponse) : DocumentUploadState()
    data class Error(val message: String) : DocumentUploadState()
}

/**
 * Sealed class representing document download state
 */
sealed class DocumentDownloadState {
    object Idle : DocumentDownloadState()
    object Loading : DocumentDownloadState()
    data class Success(val data: Map<String, String>) : DocumentDownloadState()
    data class Error(val message: String) : DocumentDownloadState()
}
