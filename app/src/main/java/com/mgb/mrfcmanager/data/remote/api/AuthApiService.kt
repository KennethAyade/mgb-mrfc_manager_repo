package com.mgb.mrfcmanager.data.remote.api

import com.mgb.mrfcmanager.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Authentication API endpoints
 * Maps to backend /auth routes
 */
interface AuthApiService {

    /**
     * Register a new user
     * POST /auth/register
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<UserDto>>

    /**
     * Login with username and password
     * POST /auth/login
     * Returns user info and JWT tokens
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    /**
     * Logout current user
     * POST /auth/logout
     * Requires authentication
     */
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    /**
     * Change user password
     * POST /auth/change-password
     * Requires authentication
     */
    @POST("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Unit>>

    /**
     * Refresh access token using refresh token
     * POST /auth/refresh
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<LoginResponse>>
}
