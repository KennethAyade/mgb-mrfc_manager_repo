package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.AuthApiService
import com.mgb.mrfcmanager.data.remote.dto.*
import com.mgb.mrfcmanager.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for authentication operations
 * Handles login, logout, token management
 */
class AuthRepository(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {

    /**
     * Login with username and password
     * Saves tokens on success
     */
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.login(
                    LoginRequest(username, password)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        // Save tokens and user info
                        val loginData = body.data
                        tokenManager.saveTokens(
                            accessToken = loginData.token,
                            refreshToken = loginData.refreshToken,
                            userId = loginData.user.id,
                            role = loginData.user.role ?: "USER", // Default to USER if role is null
                            username = loginData.user.username,
                            fullName = loginData.user.fullName
                        )
                        Result.Success(loginData)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: body?.message ?: "Login failed",
                            code = body?.error?.code
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.Error(
                        message = errorBody ?: "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error. Please check your connection.",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Register a new user
     */
    suspend fun register(
        username: String,
        password: String,
        fullName: String,
        email: String
    ): Result<UserDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.register(
                    RegisterRequest(username, password, fullName, email)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Registration failed",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Logout current user
     * Clears tokens even if API call fails
     */
    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                authApiService.logout()
                tokenManager.clearTokens()
                Result.Success(Unit)
            } catch (e: Exception) {
                // Clear tokens anyway on logout
                tokenManager.clearTokens()
                Result.Success(Unit)
            }
        }
    }

    /**
     * Change user password
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.changePassword(
                    ChangePasswordRequest(oldPassword, newPassword)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Password change failed",
                            code = body?.error?.code
                        )
                    }
                } else {
                    Result.Error(
                        message = "HTTP ${response.code()}: ${response.message()}",
                        code = response.code().toString()
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Ensure TokenManager is initialized (call on app startup)
     */
    suspend fun ensureInitialized() {
        tokenManager.ensureInitialized()
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * Get current user role
     */
    fun getUserRole(): String? {
        return tokenManager.getUserRole()
    }

    /**
     * Get current user ID
     */
    fun getUserId(): Long? {
        return tokenManager.getUserId()
    }

    /**
     * Check if current user is admin
     */
    fun isAdmin(): Boolean {
        return tokenManager.isAdmin()
    }
}
