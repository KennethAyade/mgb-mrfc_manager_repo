package com.mgb.mrfcmanager.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.data.remote.ApiConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

/**
 * MeetingRealtimeViewModel
 * Owns a single SSE connection for a meeting (agendaId) while MeetingDetailActivity is in foreground.
 * Emits MeetingEvent objects so fragments can refresh their lists.
 */
class MeetingRealtimeViewModel : ViewModel() {

    data class MeetingEvent(
        val type: String,
        val agendaId: Long,
        val itemId: Long? = null,
        val payload: Map<String, Any?>? = null,
        val ts: String? = null
    )

    // Nullable to satisfy Android lint (LiveData is Java; values may be null at runtime)
    private val _events = MutableLiveData<MeetingEvent?>()
    val events: LiveData<MeetingEvent?> = _events

    private var currentAgendaId: Long? = null
    private var eventSource: EventSource? = null

    private var reconnectJob: Job? = null
    private var stopped = true

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val eventAdapter = moshi.adapter(MeetingEvent::class.java)

    // OkHttp client for SSE: disable read timeout for long-lived streams
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun start(agendaId: Long) {
        if (currentAgendaId == agendaId && eventSource != null && !stopped) return

        stop()
        stopped = false
        currentAgendaId = agendaId

        connectWithBackoff(attempt = 0)
    }

    fun stop() {
        stopped = true
        reconnectJob?.cancel()
        reconnectJob = null
        eventSource?.cancel()
        eventSource = null
        currentAgendaId = null
    }

    private fun connectWithBackoff(attempt: Int) {
        val agendaId = currentAgendaId ?: return
        val token = MRFCManagerApp.getTokenManager().getAccessToken()

        if (token.isNullOrBlank()) {
            Log.w(TAG, "No token available for SSE; not connecting")
            return
        }

        val url = ApiConfig.BASE_URL + "agenda-items/meeting/$agendaId/events"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "text/event-stream")
            .build()

        val factory = EventSources.createFactory(okHttpClient)

        Log.d(TAG, "Connecting SSE (attempt=$attempt): $url")

        eventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d(TAG, "SSE opened (HTTP ${response.code})")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                // Backend sends JSON with { type, agendaId, itemId, payload, ts }
                try {
                    val parsed = eventAdapter.fromJson(data)
                    if (parsed != null) {
                        // Safety: ignore events for other meetings
                        if (parsed.agendaId == agendaId) {
                            _events.postValue(parsed)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse SSE event: ${e.message}")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                if (stopped) return

                val code = response?.code
                Log.w(TAG, "SSE failure (code=$code): ${t?.message}")

                // If unauthorized, don't loop forever.
                if (code == 401 || code == 403) {
                    Log.w(TAG, "SSE unauthorized; stopping reconnect")
                    stop()
                    return
                }

                scheduleReconnect(attempt + 1)
            }

            override fun onClosed(eventSource: EventSource) {
                if (stopped) return
                Log.d(TAG, "SSE closed; scheduling reconnect")
                scheduleReconnect(attempt + 1)
            }
        })
    }

    private fun scheduleReconnect(nextAttempt: Int) {
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            val delayMs = backoffDelayMs(nextAttempt)
            Log.d(TAG, "Reconnecting SSE in ${delayMs}ms (attempt=$nextAttempt)")
            delay(delayMs)
            if (!stopped) {
                connectWithBackoff(nextAttempt)
            }
        }
    }

    private fun backoffDelayMs(attempt: Int): Long {
        // 1s, 2s, 5s, 10s, 20s, 30s max
        return when {
            attempt <= 0 -> 1_000L
            attempt == 1 -> 1_000L
            attempt == 2 -> 2_000L
            attempt == 3 -> 5_000L
            attempt == 4 -> 10_000L
            attempt == 5 -> 20_000L
            else -> 30_000L
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }

    companion object {
        private const val TAG = "MeetingRealtimeVM"
    }
}
