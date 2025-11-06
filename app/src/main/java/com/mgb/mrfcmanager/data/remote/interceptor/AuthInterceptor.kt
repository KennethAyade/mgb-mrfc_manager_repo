package com.mgb.mrfcmanager.data.remote.interceptor

import android.content.Intent
import android.util.Log
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.ui.auth.LoginActivity
import com.mgb.mrfcmanager.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add JWT token to all API requests
 * Automatically adds Authorization header to authenticated endpoints
 * Handles token expiration by clearing tokens and redirecting to login
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip adding token for public endpoints
        val url = originalRequest.url.toString()
        if (shouldSkipAuth(url)) {
            Log.d("AuthInterceptor", "Skipping auth for: $url")
            return chain.proceed(originalRequest)
        }

        // Add JWT token to header
        val token = tokenManager.getAccessToken()
        val request = if (token != null) {
            Log.d("AuthInterceptor", "Adding auth token to request: ${url.substringAfter("/api")}")
            Log.d("AuthInterceptor", "Token length: ${token.length}, starts with: ${token.take(20)}...")
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.e("AuthInterceptor", "⚠️ NO TOKEN FOUND for: ${url.substringAfter("/api")}")
            Log.e("AuthInterceptor", "⚠️ User is logged in: ${tokenManager.isLoggedIn()}")
            Log.e("AuthInterceptor", "⚠️ User role: ${tokenManager.getUserRole()}")
            originalRequest
        }

        // Proceed with the request
        val response = chain.proceed(request)

        // Check for 401 Unauthorized (expired or invalid token)
        if (response.code == 401) {
            Log.w("AuthInterceptor", "⚠️ 401 Unauthorized - Token expired or invalid")
            handleTokenExpiration()
        }

        return response
    }

    /**
     * Handle token expiration by clearing tokens and redirecting to login
     */
    private fun handleTokenExpiration() {
        try {
            Log.w("AuthInterceptor", "Clearing expired tokens...")
            
            // Clear tokens
            runBlocking {
                tokenManager.clearTokens()
            }

            // Redirect to login screen
            val context = MRFCManagerApp.getInstance()
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("SESSION_EXPIRED", true)
            }
            context.startActivity(intent)
            
            Log.w("AuthInterceptor", "Redirected to login due to expired token")
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error handling token expiration", e)
        }
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
