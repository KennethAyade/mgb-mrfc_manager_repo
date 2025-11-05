package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.R

/**
 * User Dashboard - Main landing page for regular users
 */
class UserDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        setupDashboardCards()
    }

    private fun setupDashboardCards() {
        findViewById<MaterialCardView>(R.id.cardViewMRFC).setOnClickListener {
            startActivity(Intent(this, MRFCSelectionActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardViewNotes).setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            intent.putExtra("MRFC_ID", 0L)
            intent.putExtra("MRFC_NAME", "All MRFCs")
            intent.putExtra("QUARTER", "")
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardViewDocuments).setOnClickListener {
            val intent = Intent(this, DocumentListActivity::class.java)
            intent.putExtra("MRFC_ID", 0L)
            intent.putExtra("MRFC_NAME", "All MRFCs")
            startActivity(intent)
        }

        // NEW: Meeting Management access for users
        findViewById<MaterialCardView>(R.id.cardViewMeetingManagement).setOnClickListener {
            val intent = Intent(this, com.mgb.mrfcmanager.ui.meeting.QuarterSelectionActivity::class.java)
            intent.putExtra("MRFC_ID", 0L) // 0 = general meetings
            startActivity(intent)
        }
    }
}
