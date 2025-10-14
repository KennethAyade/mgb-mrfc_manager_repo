package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mgb.mrfcmanager.R

/**
 * User Dashboard - Main landing page for regular users
 * TODO: Implement user-specific features (view agenda, documents, notes)
 */
class UserDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)
    }
}
