package com.mgb.mrfcmanager.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random

/**
 * Retry Interceptor with Exponential Backoff
 *
 * Handles transient failures (429, 500, 502, 503, 504) by:
 * - Retrying up to MAX_RETRIES times
 * - Using exponential backoff with jitter
 * - Logging retry attempts for debugging
 *
 * This prevents request storms and improves reliability.
 */
class RetryInterceptor(
    private val maxRetries: Int = MAX_RETRIES
) : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
        private const val MAX_RETRIES = 3
        private const val INITIAL_DELAY_MS = 1000L // 1 second
        private const val MAX_DELAY_MS = 8000L // 8 seconds cap
        private const val JITTER_FACTOR = 0.3 // 30% randomization

        // HTTP status codes that warrant a retry
        private val RETRYABLE_CODES = setOf(
            429, // Too Many Requests
            500, // Internal Server Error
            502, // Bad Gateway
            503, // Service Unavailable
            504  // Gateway Timeout
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var lastException: IOException? = null
        var retryCount = 0

        while (retryCount <= maxRetries) {
            try {
                // Close previous response if exists
                response?.close()

                // Execute the request
                response = chain.proceed(request)

                // Check if we should retry
                if (shouldRetry(response.code) && retryCount < maxRetries) {
                    val delayMs = calculateDelay(retryCount)
                    Log.w(TAG, "Request failed with ${response.code}. Retry ${retryCount + 1}/$maxRetries after ${delayMs}ms: ${request.url}")

                    // Wait before retrying
                    Thread.sleep(delayMs)
                    retryCount++
                    continue
                }

                // Success or non-retryable error
                return response

            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "Request failed with IOException. Retry ${retryCount + 1}/$maxRetries: ${request.url}", e)

                if (retryCount < maxRetries) {
                    val delayMs = calculateDelay(retryCount)
                    try {
                        Thread.sleep(delayMs)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("Interrupted during retry delay", ie)
                    }
                    retryCount++
                } else {
                    throw e
                }
            }
        }

        // Should not reach here, but throw the last exception if we do
        throw lastException ?: IOException("Request failed after $maxRetries retries")
    }

    /**
     * Determine if the response code warrants a retry
     */
    private fun shouldRetry(code: Int): Boolean {
        return code in RETRYABLE_CODES
    }

    /**
     * Calculate delay with exponential backoff and jitter
     *
     * Formula: delay = min(initialDelay * 2^retryCount, maxDelay) * (1 + random jitter)
     */
    private fun calculateDelay(retryCount: Int): Long {
        // Exponential backoff: 1s, 2s, 4s, ...
        val exponentialDelay = INITIAL_DELAY_MS * (1 shl retryCount)

        // Cap at maximum delay
        val cappedDelay = min(exponentialDelay, MAX_DELAY_MS)

        // Add jitter to prevent thundering herd
        val jitter = (Random.nextDouble() * JITTER_FACTOR * 2 - JITTER_FACTOR) // -30% to +30%
        val delayWithJitter = (cappedDelay * (1 + jitter)).toLong()

        return delayWithJitter.coerceAtLeast(100L) // Minimum 100ms
    }
}
