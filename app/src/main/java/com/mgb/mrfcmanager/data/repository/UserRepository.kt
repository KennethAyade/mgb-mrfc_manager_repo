package com.mgb.mrfcmanager.data.repository

import com.mgb.mrfcmanager.data.remote.api.UserApiService
import com.mgb.mrfcmanager.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for User Management operations
 * Handles data operations for users
 */
class UserRepository(private val userApiService: UserApiService) {

    /**
     * Get all users with optional filtering
     */
    suspend fun getAllUsers(
        page: Int = 1,
        limit: Int = 50,
        search: String? = null,
        role: String? = null,
        activeOnly: Boolean? = null
    ): Result<UserListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userApiService.getAllUsers(
                    page = page,
                    limit = limit,
                    search = search,
                    role = role,
                    isActive = activeOnly
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to fetch users",
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
                    message = e.localizedMessage ?: "Network error. Please check your connection.",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Get user by ID
     */
    suspend fun getUserById(id: Long): Result<UserDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userApiService.getUserById(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "User not found",
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
     * Create new user
     */
    suspend fun createUser(request: CreateUserRequest): Result<UserDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userApiService.createUser(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to create user",
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
                    message = e.localizedMessage ?: "Network error",
                    code = "NETWORK_ERROR"
                )
            }
        }
    }

    /**
     * Update existing user
     */
    suspend fun updateUser(id: Long, request: UpdateUserRequest): Result<UserDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userApiService.updateUser(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to update user",
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
     * Delete user
     */
    suspend fun deleteUser(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userApiService.deleteUser(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to delete user",
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
     * Toggle user active status
     */
    suspend fun toggleUserStatus(id: Long): Result<UserDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userApiService.toggleUserStatus(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.Success(body.data)
                    } else {
                        Result.Error(
                            message = body?.error?.message ?: "Failed to toggle user status",
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
}
