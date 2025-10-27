package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * User list response with pagination
 */
data class UserListResponse(
    @Json(name = "users")
    val users: List<UserDto>,

    @Json(name = "pagination")
    val pagination: UserPaginationDto
)

/**
 * Pagination metadata (User-specific format)
 */
data class UserPaginationDto(
    @Json(name = "page")
    val page: Int,

    @Json(name = "limit")
    val limit: Int,

    @Json(name = "total")
    val total: Int,

    @Json(name = "totalPages")
    val totalPages: Int,

    @Json(name = "hasNext")
    val hasNext: Boolean,

    @Json(name = "hasPrev")
    val hasPrev: Boolean
)

/**
 * Create user request
 */
data class CreateUserRequest(
    @Json(name = "username")
    val username: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "role")
    val role: String = "USER" // USER, ADMIN
)

/**
 * Update user request
 */
data class UpdateUserRequest(
    @Json(name = "full_name")
    val fullName: String? = null,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "role")
    val role: String? = null,

    @Json(name = "is_active")
    val isActive: Boolean? = null
)

/**
 * Grant MRFC access request
 */
data class GrantMrfcAccessRequest(
    @Json(name = "mrfc_ids")
    val mrfcIds: List<Long>
)
