package com.mgb.mrfcmanager.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Manages JWT tokens using DataStore with in-memory caching
 * Thread-safe and secure token storage
 */
class TokenManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

    // In-memory cache for fast synchronous access (fixes interceptor issues)
    @Volatile
    private var cachedAccessToken: String? = null
    @Volatile
    private var cachedRefreshToken: String? = null
    @Volatile
    private var cachedUserId: Long? = null
    @Volatile
    private var cachedUserRole: String? = null
    @Volatile
    private var cachedUsername: String? = null
    @Volatile
    private var cachedFullName: String? = null

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val FULL_NAME_KEY = stringPreferencesKey("full_name")
    }

    init {
        // Load cached values from DataStore on init
        try {
            runBlocking {
                val prefs = context.dataStore.data.first()
                cachedAccessToken = prefs[ACCESS_TOKEN_KEY]
                cachedRefreshToken = prefs[REFRESH_TOKEN_KEY]
                cachedUserId = prefs[USER_ID_KEY]
                cachedUserRole = prefs[USER_ROLE_KEY]
                cachedUsername = prefs[USERNAME_KEY]
                cachedFullName = prefs[FULL_NAME_KEY]
                Log.d("TokenManager", "Loaded cached tokens - hasToken: ${cachedAccessToken != null}, role: $cachedUserRole")
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Error loading cached tokens", e)
        }
    }

    /**
     * Save authentication tokens and user info
     * Updates both DataStore and in-memory cache
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: Long,
        role: String,
        username: String = "",
        fullName: String = ""
    ) {
        // Update cache immediately (for instant access)
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
        cachedUserId = userId
        cachedUserRole = role
        cachedUsername = username
        cachedFullName = fullName
        Log.d("TokenManager", "Cached tokens - role: $role, username: $username")

        // Persist to DataStore
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USER_ID_KEY] = userId
            prefs[USER_ROLE_KEY] = role
            prefs[USERNAME_KEY] = username
            prefs[FULL_NAME_KEY] = fullName
        }
        Log.d("TokenManager", "Persisted tokens to DataStore")
    }

    /**
     * Get the access token (synchronously from cache)
     */
    fun getAccessToken(): String? {
        return cachedAccessToken
    }

    /**
     * Get the refresh token (synchronously from cache)
     */
    fun getRefreshToken(): String? {
        return cachedRefreshToken
    }

    /**
     * Get the user role
     */
    fun getUserRole(): String? {
        return cachedUserRole
    }

    /**
     * Get the user ID
     */
    fun getUserId(): Long? {
        return cachedUserId
    }

    /**
     * Get the username
     */
    fun getUsername(): String? {
        return cachedUsername
    }

    /**
     * Get the full name
     */
    fun getFullName(): String? {
        return cachedFullName
    }

    /**
     * Clear all tokens and user data (logout)
     * Clears both cache and DataStore
     */
    suspend fun clearTokens() {
        // Clear cache first
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedUserId = null
        cachedUserRole = null
        cachedUsername = null
        cachedFullName = null
        Log.d("TokenManager", "Cleared cached tokens")

        // Clear DataStore
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        Log.d("TokenManager", "Cleared DataStore tokens")
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return cachedAccessToken != null
    }

    /**
     * Check if user is admin
     */
    fun isAdmin(): Boolean {
        val role = cachedUserRole
        return role == "SUPER_ADMIN" || role == "ADMIN"
    }
}
