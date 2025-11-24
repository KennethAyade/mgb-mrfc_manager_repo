package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * User Management API endpoints
 * Maps to backend /users routes
 * Most endpoints require admin authentication
 */
interface UserApiService {

    /**
     * Get all users with pagination and search
     * GET /users
     * Requires admin authentication
     */
    @GET("users")
    suspend fun getAllUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("is_active") isActive: Boolean? = null,
        @Query("sort_by") sortBy: String = "created_at",
        @Query("sort_order") sortOrder: String = "DESC"
    ): Response<ApiResponse<UserListResponse>>

    /**
     * Get user by ID
     * GET /users/:id
     * Requires authentication (users can view their own profile)
     */
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") id: Long
    ): Response<ApiResponse<UserDto>>

    /**
     * Create new user
     * POST /users
     * Requires admin authentication
     */
    @POST("users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<ApiResponse<UserDto>>

    /**
     * Update user
     * PUT /users/:id
     * Requires admin authentication
     */
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body request: UpdateUserRequest
    ): Response<ApiResponse<UserDto>>

    /**
     * Delete user (soft delete)
     * DELETE /users/:id
     * Requires admin authentication
     */
    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Long
    ): Response<ApiResponse<Any?>>

    /**
     * Toggle user active status
     * PUT /users/:id/toggle-status
     * Requires admin authentication
     */
    @PUT("users/{id}/toggle-status")
    suspend fun toggleUserStatus(
        @Path("id") id: Long
    ): Response<ApiResponse<UserDto>>

    /**
     * Grant MRFC access to user
     * POST /users/:id/grant-mrfc-access
     * Requires admin authentication
     */
    @POST("users/{id}/grant-mrfc-access")
    suspend fun grantMrfcAccess(
        @Path("id") id: Long,
        @Body request: GrantMrfcAccessRequest
    ): Response<ApiResponse<UserDto>>
}

