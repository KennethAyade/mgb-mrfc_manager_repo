package com.mgb.mrfcmanager

import android.app.Application
import com.mgb.mrfcmanager.data.local.database.MRFCDatabase
import com.mgb.mrfcmanager.sync.SyncWorker
import com.mgb.mrfcmanager.utils.NetworkConnectivityManager
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

        /**
         * Get singleton Database instance
         */
        fun getDatabase(): MRFCDatabase {
            return instance.database
        }

        /**
         * Get singleton NetworkConnectivityManager instance
         */
        fun getNetworkManager(): NetworkConnectivityManager {
            return instance.networkManager
        }
    }

    // Singleton TokenManager - only one instance for entire app
    private lateinit var tokenManager: TokenManager

    // Singleton Database instance (Feature 9: Offline support)
    private lateinit var database: MRFCDatabase

    // Singleton NetworkConnectivityManager (Feature 9: Offline support)
    private lateinit var networkManager: NetworkConnectivityManager

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize TokenManager once
        tokenManager = TokenManager(applicationContext)

        // Initialize Room database (Feature 9: Offline support)
        database = MRFCDatabase.getInstance(applicationContext)

        // Initialize NetworkConnectivityManager (Feature 9: Offline support)
        networkManager = NetworkConnectivityManager.getInstance(applicationContext)

        // Schedule periodic sync (Feature 9: Offline support)
        SyncWorker.schedulePeriodicSync(applicationContext)
    }
}
