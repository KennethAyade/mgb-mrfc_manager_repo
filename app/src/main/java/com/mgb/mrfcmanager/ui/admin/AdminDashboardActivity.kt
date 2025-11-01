package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.ui.auth.LoginActivity
import com.mgb.mrfcmanager.ui.meeting.QuarterSelectionActivity as MeetingQuarterSelection
import kotlinx.coroutines.launch

/**
 * Modern Admin Dashboard with Navigation Drawer
 * Professional design with burger menu and organized sections
 */
class AdminDashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AdminDashboard", "AdminDashboardActivity onCreate called")
        setContentView(R.layout.activity_admin_dashboard)

        setupToolbar()
        setupNavigationDrawer()
        setupDashboardCards()
        setupRoleBasedUI()

        Log.d("AdminDashboard", "AdminDashboardActivity setup completed")
    }

    /**
     * Configure UI based on user role
     * SUPER_ADMIN sees all options including User Management
     * ADMIN sees standard admin options
     */
    private fun setupRoleBasedUI() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        val username = tokenManager.getUsername()
        val fullName = tokenManager.getFullName()

        Log.d("AdminDashboard", "Setting up UI for role: $userRole, username: $username")

        // Update navigation header with user info
        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserRole = headerView.findViewById<TextView>(R.id.tvUserRole)

        tvUserName.text = fullName ?: username ?: "Admin User"

        // Display user-friendly role name
        tvUserRole.text = when (userRole) {
            "SUPER_ADMIN" -> "Super Administrator"
            "ADMIN" -> "Administrator"
            "USER" -> "User"
            else -> "User"
        }

        // Hide User Management menu for USER role only
        // SUPER_ADMIN and ADMIN both have access to User Management
        if (userRole == "USER") {
            val menu = navigationView.menu
            val userManagementItem = menu.findItem(R.id.nav_user_management)
            userManagementItem?.isVisible = false
            Log.d("AdminDashboard", "User Management hidden for USER role")
        } else {
            Log.d("AdminDashboard", "User Management visible for $userRole")
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupDashboardCards() {
        // Quick Action Cards
        findViewById<MaterialCardView>(R.id.cardSelectMRFC).setOnClickListener {
            startActivity(Intent(this, MRFCListActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardAttendance).setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardFileUpload).setOnClickListener {
            startActivity(Intent(this, FileUploadActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardCompliance).setOnClickListener {
            startActivity(Intent(this, ComplianceDashboardActivity::class.java))
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                // Already on dashboard
            }
            R.id.nav_mrfc -> {
                startActivity(Intent(this, MRFCListActivity::class.java))
            }
            R.id.nav_proponents -> {
                startActivity(Intent(this, MRFCListActivity::class.java))
            }
            R.id.nav_meetings -> {
                // Navigate directly to quarter selection for general meetings
                startActivity(Intent(this, MeetingQuarterSelection::class.java))
            }
            R.id.nav_documents -> {
                startActivity(Intent(this, FileUploadActivity::class.java))
            }
            R.id.nav_file_upload -> {
                startActivity(Intent(this, FileUploadActivity::class.java))
            }
            R.id.nav_compliance -> {
                startActivity(Intent(this, ComplianceDashboardActivity::class.java))
            }
            R.id.nav_reports -> {
                startActivity(Intent(this, ComplianceDashboardActivity::class.java))
            }
            R.id.nav_notifications -> {
                startActivity(Intent(this, NotificationActivity::class.java))
            }
            R.id.nav_user_management -> {
                startActivity(Intent(this, UserManagementActivity::class.java))
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                logout()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        // Use singleton TokenManager from Application class
        val tokenManager = MRFCManagerApp.getTokenManager()
        lifecycleScope.launch {
            tokenManager.clearTokens()

            // Navigate to login
            val intent = Intent(this@AdminDashboardActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
