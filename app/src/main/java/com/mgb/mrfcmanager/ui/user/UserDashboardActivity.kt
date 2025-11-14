package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.ui.auth.LoginActivity
import kotlinx.coroutines.launch

/**
 * User Dashboard - Main landing page for regular users
 * BUG FIX 6: Added hamburger menu for logout
 */
class UserDashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        setupToolbar()
        setupNavigationDrawer()
        setupDashboardCards()
        setupBackPressedHandler()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        // BUG FIX: Set actual user name and role in nav header
        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<android.widget.TextView>(R.id.tvUserName)
        val tvUserRole = headerView.findViewById<android.widget.TextView>(R.id.tvUserRole)
        
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userName = tokenManager.getFullName() ?: "User"
        val userRole = tokenManager.getUserRole()
        
        tvUserName?.text = userName
        tvUserRole?.text = when (userRole) {
            "USER" -> "Regular User"
            "ADMIN" -> "Administrator"
            "SUPER_ADMIN" -> "Super Administrator"
            else -> "User"
        }
        
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupDashboardCards() {
        // BUG FIX 1: View MRFC - Route to MRFCListActivity (read-only mode)
        findViewById<MaterialCardView>(R.id.cardViewMRFC).setOnClickListener {
            val intent = Intent(this, com.mgb.mrfcmanager.ui.admin.MRFCListActivity::class.java)
            startActivity(intent)
        }

        // BUG FIX 2: My Notes - Route to MRFC selection first (Notes requires MRFC_ID)
        findViewById<MaterialCardView>(R.id.cardViewNotes).setOnClickListener {
            startActivity(Intent(this, MRFCSelectionActivity::class.java).apply {
                putExtra("DESTINATION", "NOTES")
            })
        }

        // BUG FIX 3: View Documents - Route to MRFC selection first (Documents requires MRFC_ID)
        findViewById<MaterialCardView>(R.id.cardViewDocuments).setOnClickListener {
            startActivity(Intent(this, MRFCSelectionActivity::class.java).apply {
                putExtra("DESTINATION", "DOCUMENTS")
            })
        }

        // Meeting Management access for users
        findViewById<MaterialCardView>(R.id.cardViewMeetingManagement).setOnClickListener {
            val intent = Intent(this, com.mgb.mrfcmanager.ui.meeting.QuarterSelectionActivity::class.java)
            intent.putExtra("MRFC_ID", 0L) // 0 = general meetings
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already on dashboard
            }
            R.id.nav_mrfc_management -> {
                // View MRFCs
                val intent = Intent(this, com.mgb.mrfcmanager.ui.admin.MRFCListActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_meeting_management -> {
                // Meeting Management
                val intent = Intent(this, com.mgb.mrfcmanager.ui.meeting.QuarterSelectionActivity::class.java)
                intent.putExtra("MRFC_ID", 0L)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        lifecycleScope.launch {
            tokenManager.clearTokens()
            
            // Navigate to login
            val intent = Intent(this@UserDashboardActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }
}
