package com.mgb.mrfcmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.data.remote.dto.LoginResponse
import com.mgb.mrfcmanager.data.repository.AuthRepository
import com.mgb.mrfcmanager.data.repository.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for Login screen
 * Handles login logic and UI state
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    /**
     * Attempt to login with username and password
     */
    fun login(username: String, password: String) {
        // Validation
        if (username.isBlank()) {
            _loginState.value = LoginState.Error("Username is required")
            return
        }

        if (password.isBlank()) {
            _loginState.value = LoginState.Error("Password is required")
            return
        }

        if (password.length < 6) {
            _loginState.value = LoginState.Error("Password must be at least 6 characters")
            return
        }

        // Show loading state
        _loginState.value = LoginState.Loading

        // Perform login
        viewModelScope.launch {
            when (val result = authRepository.login(username, password)) {
                is Result.Success -> {
                    _loginState.value = LoginState.Success(result.data)
                }
                is Result.Error -> {
                    _loginState.value = LoginState.Error(result.message)
                }
                is Result.Loading -> {
                    _loginState.value = LoginState.Loading
                }
            }
        }
    }

    /**
     * Reset the login state (for clearing errors)
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

/**
 * Sealed class representing different login states
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val data: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}
