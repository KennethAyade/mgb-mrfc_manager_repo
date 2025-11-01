package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.api.ItemOrderUpdate
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaItemRequest
import com.mgb.mrfcmanager.data.repository.AgendaItemRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Agenda Items (Discussion Topics)
 * Manages UI state for meeting topics and reordering
 */
class AgendaItemViewModel(private val repository: AgendaItemRepository) : ViewModel() {

    private val _itemsListState = MutableLiveData<ItemsListState>()
    val itemsListState: LiveData<ItemsListState> = _itemsListState

    private val _itemDetailState = MutableLiveData<ItemDetailState>()
    val itemDetailState: LiveData<ItemDetailState> = _itemDetailState

    /**
     * Load all agenda items for a specific meeting
     */
    fun loadItemsByAgenda(agendaId: Long) {
        _itemsListState.value = ItemsListState.Loading

        viewModelScope.launch {
            when (val result = repository.getItemsByAgenda(agendaId)) {
                is Result.Success -> {
                    _itemsListState.value = ItemsListState.Success(result.data)
                }
                is Result.Error -> {
                    _itemsListState.value = ItemsListState.Error(result.message)
                }
                is Result.Loading -> {
                    _itemsListState.value = ItemsListState.Loading
                }
            }
        }
    }

    /**
     * Load agenda item by ID
     */
    fun loadItemById(id: Long) {
        _itemDetailState.value = ItemDetailState.Loading

        viewModelScope.launch {
            when (val result = repository.getItemById(id)) {
                is Result.Success -> {
                    _itemDetailState.value = ItemDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _itemDetailState.value = ItemDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _itemDetailState.value = ItemDetailState.Loading
                }
            }
        }
    }

    /**
     * Create new agenda item (discussion topic)
     */
    fun createItem(
        agendaId: Long,
        title: String,
        description: String? = null,
        orderIndex: Int = 0,
        onComplete: (Result<AgendaItemDto>) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateAgendaItemRequest(
                agendaId = agendaId,
                title = title,
                description = description,
                orderIndex = orderIndex
            )
            val result = repository.createItem(request)
            onComplete(result)

            // Reload the list
            if (result is Result.Success) {
                loadItemsByAgenda(agendaId)
            }
        }
    }

    /**
     * Update existing agenda item
     */
    fun updateItem(
        id: Long,
        agendaId: Long,
        title: String,
        description: String? = null,
        orderIndex: Int = 0,
        onComplete: (Result<AgendaItemDto>) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateAgendaItemRequest(
                agendaId = agendaId,
                title = title,
                description = description,
                orderIndex = orderIndex
            )
            val result = repository.updateItem(id, request)
            onComplete(result)

            // Reload the list and update detail
            if (result is Result.Success) {
                _itemDetailState.value = ItemDetailState.Success(result.data)
                loadItemsByAgenda(agendaId)
            }
        }
    }

    /**
     * Reorder agenda items (drag and drop)
     */
    fun reorderItems(items: List<ItemOrderUpdate>, agendaId: Long) {
        viewModelScope.launch {
            when (val result = repository.reorderItems(items)) {
                is Result.Success -> {
                    _itemsListState.value = ItemsListState.Success(result.data)
                }
                is Result.Error -> {
                    _itemsListState.value = ItemsListState.Error(result.message)
                    // Reload original order on error
                    loadItemsByAgenda(agendaId)
                }
                is Result.Loading -> {
                    _itemsListState.value = ItemsListState.Loading
                }
            }
        }
    }

    /**
     * Delete agenda item
     */
    fun deleteItem(id: Long, agendaId: Long, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteItem(id)
            onComplete(result)

            // Reload the list
            if (result is Result.Success) {
                loadItemsByAgenda(agendaId)
            }
        }
    }

    /**
     * Refresh items list
     */
    fun refresh(agendaId: Long) {
        loadItemsByAgenda(agendaId)
    }

    /**
     * Clear selected item
     */
    fun clearSelectedItem() {
        _itemDetailState.value = ItemDetailState.Idle
    }
}

/**
 * Sealed class representing agenda items list state
 */
sealed class ItemsListState {
    object Idle : ItemsListState()
    object Loading : ItemsListState()
    data class Success(val data: List<AgendaItemDto>) : ItemsListState()
    data class Error(val message: String) : ItemsListState()
}

/**
 * Sealed class representing item detail state
 */
sealed class ItemDetailState {
    object Idle : ItemDetailState()
    object Loading : ItemDetailState()
    data class Success(val data: AgendaItemDto) : ItemDetailState()
    data class Error(val message: String) : ItemDetailState()
}
