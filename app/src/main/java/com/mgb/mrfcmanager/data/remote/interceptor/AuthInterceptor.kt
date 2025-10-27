package com.mgb.mrfcmanager.data.remote.interceptor

import com.mgb.mrfcmanager.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add JWT token to all API requests
 * Automatically adds Authorization header to authenticated endpoints
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip adding token for public endpoints
        val url = originalRequest.url.toString()
        if (shouldSkipAuth(url)) {
            return chain.proceed(originalRequest)
        }

        // Add JWT token to header
        val token = tokenManager.getAccessToken()
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }

    /**
     * Check if the URL should skip authentication
     */
    private fun shouldSkipAuth(url: String): Boolean {
        return url.contains("/auth/login") ||
                url.contains("/auth/register") ||
                url.contains("/health")
    }
}
