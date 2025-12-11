package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.*
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _usersState = MutableLiveData<UserListState>()
    val usersState: LiveData<UserListState> = _usersState

    private val _createUserState = MutableLiveData<CreateUserState>()
    val createUserState: LiveData<CreateUserState> = _createUserState

    private val _updateUserState = MutableLiveData<UpdateUserState>()
    val updateUserState: LiveData<UpdateUserState> = _updateUserState

    private val _deleteUserState = MutableLiveData<DeleteUserState>()
    val deleteUserState: LiveData<DeleteUserState> = _deleteUserState

    private val _grantMrfcAccessState = MutableLiveData<GrantMrfcAccessState>()
    val grantMrfcAccessState: LiveData<GrantMrfcAccessState> = _grantMrfcAccessState

    private val _userDetailState = MutableLiveData<UserDetailState>()
    val userDetailState: LiveData<UserDetailState> = _userDetailState

    fun loadUsers(
        page: Int = 1,
        search: String? = null,
        role: String? = null,
        isActive: Boolean? = null
    ) {
        _usersState.value = UserListState.Loading

        viewModelScope.launch {
            when (val result = userRepository.getAllUsers(page, 50, search, role, isActive)) {
                is Result.Success -> {
                    _usersState.value = UserListState.Success(result.data)
                }
                is Result.Error -> {
                    _usersState.value = UserListState.Error(result.message)
                }
                is Result.Loading -> {
                    _usersState.value = UserListState.Loading
                }
            }
        }
    }

    fun loadUserById(userId: Long) {
        _userDetailState.value = UserDetailState.Loading

        viewModelScope.launch {
            when (val result = userRepository.getUserById(userId)) {
                is Result.Success -> {
                    _userDetailState.value = UserDetailState.Success(result.data)
                }
                is Result.Error -> {
                    _userDetailState.value = UserDetailState.Error(result.message)
                }
                is Result.Loading -> {
                    _userDetailState.value = UserDetailState.Loading
                }
            }
        }
    }

    fun createUser(request: CreateUserRequest) {
        _createUserState.value = CreateUserState.Loading

        viewModelScope.launch {
            when (val result = userRepository.createUser(request)) {
                is Result.Success -> {
                    _createUserState.value = CreateUserState.Success(result.data)
                }
                is Result.Error -> {
                    _createUserState.value = CreateUserState.Error(result.message)
                }
                is Result.Loading -> {
                    _createUserState.value = CreateUserState.Loading
                }
            }
        }
    }

    fun updateUser(userId: Long, request: UpdateUserRequest) {
        _updateUserState.value = UpdateUserState.Loading

        viewModelScope.launch {
            when (val result = userRepository.updateUser(userId, request)) {
                is Result.Success -> {
                    _updateUserState.value = UpdateUserState.Success(result.data)
                }
                is Result.Error -> {
                    _updateUserState.value = UpdateUserState.Error(result.message)
                }
                is Result.Loading -> {
                    _updateUserState.value = UpdateUserState.Loading
                }
            }
        }
    }

    fun deleteUser(userId: Long) {
        _deleteUserState.value = DeleteUserState.Loading

        viewModelScope.launch {
            when (val result = userRepository.deleteUser(userId)) {
                is Result.Success -> {
                    _deleteUserState.value = DeleteUserState.Success
                }
                is Result.Error -> {
                    _deleteUserState.value = DeleteUserState.Error(result.message)
                }
                is Result.Loading -> {
                    _deleteUserState.value = DeleteUserState.Loading
                }
            }
        }
    }

    fun toggleUserStatus(userId: Long) {
        viewModelScope.launch {
            when (userRepository.toggleUserStatus(userId)) {
                is Result.Success -> {
                    // Reload the list
                    loadUsers()
                }
                is Result.Error -> {
                    // Handle error if needed
                }
                else -> {}
            }
        }
    }

    fun resetCreateState() {
        _createUserState.value = CreateUserState.Idle
    }

    fun resetUpdateState() {
        _updateUserState.value = UpdateUserState.Idle
    }

    fun grantMrfcAccess(userId: Long, mrfcIds: List<Long>) {
        _grantMrfcAccessState.value = GrantMrfcAccessState.Loading

        viewModelScope.launch {
            when (val result = userRepository.grantMrfcAccess(userId, mrfcIds)) {
                is Result.Success -> {
                    _grantMrfcAccessState.value = GrantMrfcAccessState.Success(result.data)
                }
                is Result.Error -> {
                    _grantMrfcAccessState.value = GrantMrfcAccessState.Error(result.message)
                }
                is Result.Loading -> {
                    _grantMrfcAccessState.value = GrantMrfcAccessState.Loading
                }
            }
        }
    }

    fun resetGrantMrfcAccessState() {
        _grantMrfcAccessState.value = GrantMrfcAccessState.Idle
    }
}

sealed class UserListState {
    object Idle : UserListState()
    object Loading : UserListState()
    data class Success(val data: UserListResponse) : UserListState()
    data class Error(val message: String) : UserListState()
}

sealed class CreateUserState {
    object Idle : CreateUserState()
    object Loading : CreateUserState()
    data class Success(val user: UserDto) : CreateUserState()
    data class Error(val message: String) : CreateUserState()
}

sealed class UpdateUserState {
    object Idle : UpdateUserState()
    object Loading : UpdateUserState()
    data class Success(val user: UserDto) : UpdateUserState()
    data class Error(val message: String) : UpdateUserState()
}

sealed class DeleteUserState {
    object Idle : DeleteUserState()
    object Loading : DeleteUserState()
    object Success : DeleteUserState()
    data class Error(val message: String) : DeleteUserState()
}

sealed class GrantMrfcAccessState {
    object Idle : GrantMrfcAccessState()
    object Loading : GrantMrfcAccessState()
    data class Success(val user: UserDto) : GrantMrfcAccessState()
    data class Error(val message: String) : GrantMrfcAccessState()
}

sealed class UserDetailState {
    object Idle : UserDetailState()
    object Loading : UserDetailState()
    data class Success(val user: UserDto) : UserDetailState()
    data class Error(val message: String) : UserDetailState()
}
