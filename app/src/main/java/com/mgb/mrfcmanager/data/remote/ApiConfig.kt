package com.mgb.mrfcmanager.data.remote

import android.os.Build

object ApiConfig {
    // Local development backend URL for emulator
    private const val LOCAL_URL = "http://10.0.2.2:3000/api/v1/"

    // Production backend URL for physical devices
    private const val PRODUCTION_URL = "https://mgb-mrfc-manager-repo.onrender.com/api/v1/"

    /**
     * Dynamically determine the base URL:
     * - Emulator: Use localhost backend (10.0.2.2)
     * - Physical device: Use deployed Render backend
     */
    val BASE_URL: String
        get() = if (isEmulator()) LOCAL_URL else PRODUCTION_URL

    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    /**
     * Detect if running on emulator by checking device characteristics
     */
    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}
