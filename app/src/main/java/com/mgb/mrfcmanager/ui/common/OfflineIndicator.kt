package com.mgb.mrfcmanager.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mgb.mrfcmanager.R

/**
 * Custom view to show offline mode indicator
 * Displays at top of screen when app is offline with pending sync info
 */
class OfflineIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val tvMessage: TextView

    init {
        orientation = HORIZONTAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.status_warning))
        setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())

        // Create text view
        tvMessage = TextView(context).apply {
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            textSize = 12f
            text = "Offline Mode - Changes will sync when online"
        }

        addView(tvMessage, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // Initially hidden
        visibility = GONE
    }

    /**
     * Show offline indicator with optional pending sync count
     */
    fun show(pendingSyncCount: Int = 0) {
        visibility = VISIBLE
        tvMessage.text = if (pendingSyncCount > 0) {
            "Offline Mode - $pendingSyncCount changes pending sync"
        } else {
            "Offline Mode - Changes will sync when online"
        }
    }

    /**
     * Hide offline indicator
     */
    fun hide() {
        visibility = GONE
    }

    /**
     * Update pending sync count
     */
    fun updatePendingSyncCount(count: Int) {
        if (visibility == VISIBLE) {
            tvMessage.text = if (count > 0) {
                "Offline Mode - $count changes pending sync"
            } else {
                "Offline Mode - Changes will sync when online"
            }
        }
    }

    /**
     * Show syncing indicator
     */
    fun showSyncing() {
        visibility = VISIBLE
        setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
        tvMessage.text = "Syncing..."
    }

    /**
     * Show sync complete message briefly
     */
    fun showSyncComplete() {
        visibility = VISIBLE
        setBackgroundColor(ContextCompat.getColor(context, R.color.status_success))
        tvMessage.text = "Sync complete"

        // Hide after 2 seconds
        postDelayed({
            visibility = GONE
            setBackgroundColor(ContextCompat.getColor(context, R.color.status_warning))
        }, 2000)
    }

    /**
     * Show sync error message
     */
    fun showSyncError(message: String = "Sync failed") {
        visibility = VISIBLE
        setBackgroundColor(ContextCompat.getColor(context, R.color.status_error))
        tvMessage.text = message

        // Reset to warning color after 3 seconds
        postDelayed({
            setBackgroundColor(ContextCompat.getColor(context, R.color.status_warning))
        }, 3000)
    }

    private fun Int.dpToPx(): Int = (this * context.resources.displayMetrics.density).toInt()
}
