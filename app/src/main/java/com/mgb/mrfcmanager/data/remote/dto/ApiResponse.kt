package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Standard API response wrapper
 * Matches backend response format
 */
data class ApiResponse<T>(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "data")
    val data: T? = null,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "error")
    val error: ApiError? = null
)

data class ApiError(
    @Json(name = "code")
    val code: String,

    @Json(name = "message")
    val message: String,

    @Json(name = "details")
    val details: Any? = null
)
