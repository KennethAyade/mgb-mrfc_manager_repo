package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

// ========== REQUEST DTOs ==========

data class LoginRequest(
    @Json(name = "username")
    val username: String,

    @Json(name = "password")
    val password: String
)

data class RegisterRequest(
    @Json(name = "username")
    val username: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "email")
    val email: String
)

data class ChangePasswordRequest(
    @Json(name = "old_password")
    val oldPassword: String,

    @Json(name = "new_password")
    val newPassword: String
)

data class RefreshTokenRequest(
    @Json(name = "refresh_token")
    val refreshToken: String
)

// ========== RESPONSE DTOs ==========

data class LoginResponse(
    @Json(name = "user")
    val user: UserDto,

    @Json(name = "token")
    val token: String,

    @Json(name = "refreshToken")
    val refreshToken: String,

    @Json(name = "expiresIn")
    val expiresIn: String
)

data class UserDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "username")
    val username: String,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "email")
    val email: String? = null, // Nullable for partial user data in some responses

    @Json(name = "role")
    val role: String? = null, // SUPER_ADMIN, ADMIN, USER

    @Json(name = "is_active")
    val isActive: Boolean = true, // Default to true if not provided by backend

    @Json(name = "email_verified")
    val emailVerified: Boolean = false, // Default to false if not provided by backend

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null,

    @Json(name = "mrfc_access")
    val mrfcAccess: List<UserMrfcAccessDto>? = null // MRFC access list (optional)
)

/**
 * User MRFC Access DTO - represents which MRFCs a user can access
 */
data class UserMrfcAccessDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "user_id")
    val userId: Long,

    @Json(name = "mrfc_id")
    val mrfcId: Long,

    @Json(name = "granted_by")
    val grantedBy: Long? = null,

    @Json(name = "granted_at")
    val grantedAt: String? = null,

    @Json(name = "is_active")
    val isActive: Boolean = true,

    @Json(name = "mrfc")
    val mrfc: SimpleMrfcDto? = null // Nested MRFC details
)

/**
 * Simple MRFC DTO - minimal MRFC data for nested responses
 */
data class SimpleMrfcDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "municipality")
    val municipality: String? = null
)
