package com.mgb.mrfcmanager

import android.app.Application
import com.mgb.mrfcmanager.utils.TokenManager

/**
 * Application class for global state management
 * Provides singleton instances to prevent multiple DataStore instances
 */
class MRFCManagerApp : Application() {

    companion object {
        private lateinit var instance: MRFCManagerApp

        /**
         * Get the application instance
         */
        fun getInstance(): MRFCManagerApp {
            return instance
        }

        /**
         * Get singleton TokenManager instance
         * IMPORTANT: Always use this instead of creating new TokenManager instances
         */
        fun getTokenManager(): TokenManager {
            return instance.tokenManager
        }
    }

    // Singleton TokenManager - only one instance for entire app
    private lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize TokenManager once
        tokenManager = TokenManager(applicationContext)
    }
}
