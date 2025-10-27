package com.mgb.mrfcmanager.data.remote

import com.mgb.mrfcmanager.data.remote.interceptor.AuthInterceptor
import com.mgb.mrfcmanager.utils.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for API communication
 * Configured with authentication, logging, and JSON parsing
 */
object RetrofitClient {

    @Volatile
    private var retrofit: Retrofit? = null

    /**
     * Get or create Retrofit instance
     */
    fun getInstance(tokenManager: TokenManager): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(tokenManager).also { retrofit = it }
        }
    }

    /**
     * Build Retrofit instance with all configurations
     */
    private fun buildRetrofit(tokenManager: TokenManager): Retrofit {
        // Moshi for JSON parsing with Kotlin support
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        // Logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttp client with interceptors and timeouts
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()

        // Retrofit instance
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Reset the instance (useful for testing or changing configurations)
     */
    fun reset() {
        synchronized(this) {
            retrofit = null
        }
    }
}
