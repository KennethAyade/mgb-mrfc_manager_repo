package com.mgb.mrfcmanager.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Network Monitor Utility
 *
 * Monitors network connectivity status and provides:
 * - Immediate connectivity check
 * - Flow-based status updates for reactive UI
 *
 * Usage:
 * - Call isConnected() for one-time check
 * - Collect networkStatus flow for real-time updates
 */
class NetworkMonitor(private val context: Context) {

    companion object {
        private const val TAG = "NetworkMonitor"

        @Volatile
        private var INSTANCE: NetworkMonitor? = null

        fun getInstance(context: Context): NetworkMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkMonitor(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Check if network is currently available
     */
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Flow that emits network connectivity status changes
     *
     * Usage:
     * ```
     * networkMonitor.networkStatus.collect { isConnected ->
     *     if (isConnected) hideOfflineIndicator() else showOfflineIndicator()
     * }
     * ```
     */
    val networkStatus: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                trySend(true)
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                trySend(false)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                Log.d(TAG, "Capabilities changed: internet=$hasInternet, validated=$validated")
                trySend(hasInternet && validated)
            }

            override fun onUnavailable() {
                Log.d(TAG, "Network unavailable")
                trySend(false)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        // Emit initial state
        trySend(isConnected())

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}
