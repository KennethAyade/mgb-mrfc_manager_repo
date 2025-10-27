package com.mgb.mrfcmanager.data.remote

object ApiConfig {
    // TODO: Replace with your deployed backend URL
    // For Android Emulator use: 10.0.2.2
    // For real device on same network: 192.168.1.X
    // For production: your deployed API URL
    const val BASE_URL = "http://10.0.2.2:3000/api/v1/"

    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
