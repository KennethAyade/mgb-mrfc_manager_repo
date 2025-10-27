package com.mgb.mrfcmanager.utils

import android.content.Context
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
 * Manages JWT tokens using DataStore
 * Thread-safe and secure token storage
 */
class TokenManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val FULL_NAME_KEY = stringPreferencesKey("full_name")
    }

    /**
     * Save authentication tokens and user info
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: Long,
        role: String,
        username: String = "",
        fullName: String = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[USER_ID_KEY] = userId
            prefs[USER_ROLE_KEY] = role
            prefs[USERNAME_KEY] = username
            prefs[FULL_NAME_KEY] = fullName
        }
    }

    /**
     * Get the access token (synchronously)
     */
    fun getAccessToken(): String? = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN_KEY]
        }.first()
    }

    /**
     * Get the refresh token (synchronously)
     */
    fun getRefreshToken(): String? = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }.first()
    }

    /**
     * Get the user role
     */
    fun getUserRole(): String? = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[USER_ROLE_KEY]
        }.first()
    }

    /**
     * Get the user ID
     */
    fun getUserId(): Long? = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }.first()
    }

    /**
     * Get the username
     */
    fun getUsername(): String? = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[USERNAME_KEY]
        }.first()
    }

    /**
     * Get the full name
     */
    fun getFullName(): String? = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[FULL_NAME_KEY]
        }.first()
    }

    /**
     * Clear all tokens and user data (logout)
     */
    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    /**
     * Check if user is admin
     */
    fun isAdmin(): Boolean {
        val role = getUserRole()
        return role == "SUPER_ADMIN" || role == "ADMIN"
    }
}
