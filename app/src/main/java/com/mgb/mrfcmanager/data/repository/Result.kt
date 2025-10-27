package com.mgb.mrfcmanager.data.repository

/**
 * Sealed class to represent the result of an API call
 * Used for consistent error handling across the app
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
