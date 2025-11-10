package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
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
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaApiService
import com.mgb.mrfcmanager.data.remote.api.MrfcApiService
import com.mgb.mrfcmanager.data.remote.api.UserApiService
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
        setupBackPressedHandler()
        loadDashboardStatistics()

        Log.d("AdminDashboard", "AdminDashboardActivity setup completed")
    }

    /**
     * Configure UI based on user role
     * Updates navigation header with user info
     * Menu selection is handled in setupNavigationDrawer()
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
        val roleText = when (userRole) {
            "SUPER_ADMIN" -> "Super Administrator"
            "ADMIN" -> "Administrator"
            "USER" -> "User"
            else -> "User"
        }
        tvUserRole.text = roleText

        // Update welcome card
        findViewById<TextView>(R.id.tvWelcomeUser).text = roleText
    }

    /**
     * Load and display dashboard statistics from backend API
     */
    private fun loadDashboardStatistics() {
        // Set initial loading placeholders
        findViewById<TextView>(R.id.tvTotalMrfcs).text = "..."
        findViewById<TextView>(R.id.tvTotalUsers).text = "..."
        findViewById<TextView>(R.id.tvUpcomingMeetings).text = "..."
        findViewById<TextView>(R.id.tvPendingDocuments).text = "..."

        // Fetch actual counts from API
        lifecycleScope.launch {
            val tokenManager = MRFCManagerApp.getTokenManager()
            val retrofit = RetrofitClient.getInstance(tokenManager)
            
            // Load MRFC count
            launch {
                try {
                    val mrfcApi = retrofit.create(MrfcApiService::class.java)
                    val response = mrfcApi.getAllMrfcs(page = 1, limit = 1, isActive = true)
                    if (response.isSuccessful) {
                        val totalMrfcs = response.body()?.data?.pagination?.totalItems ?: 0
                        findViewById<TextView>(R.id.tvTotalMrfcs).text = totalMrfcs.toString()
                        Log.d("Dashboard", "Total MRFCs: $totalMrfcs")
                    } else {
                        findViewById<TextView>(R.id.tvTotalMrfcs).text = "0"
                    }
                } catch (e: Exception) {
                    Log.e("Dashboard", "Error loading MRFC count", e)
                    findViewById<TextView>(R.id.tvTotalMrfcs).text = "0"
                }
            }

            // Load User count
            launch {
                try {
                    val userApi = retrofit.create(UserApiService::class.java)
                    val response = userApi.getAllUsers(page = 1, limit = 1)
                    if (response.isSuccessful) {
                        val totalUsers = response.body()?.data?.pagination?.total ?: 0
                        findViewById<TextView>(R.id.tvTotalUsers).text = totalUsers.toString()
                        Log.d("Dashboard", "Total Users: $totalUsers")
                    } else {
                        findViewById<TextView>(R.id.tvTotalUsers).text = "0"
                    }
                } catch (e: Exception) {
                    Log.e("Dashboard", "Error loading user count", e)
                    findViewById<TextView>(R.id.tvTotalUsers).text = "0"
                }
            }

            // Load Meeting count (total agendas)
            launch {
                try {
                    val agendaApi = retrofit.create(AgendaApiService::class.java)
                    val response = agendaApi.getAllAgendas(page = 1, limit = 1)
                    if (response.isSuccessful) {
                        val totalAgendas = response.body()?.data?.pagination?.total ?: 0
                        findViewById<TextView>(R.id.tvUpcomingMeetings).text = totalAgendas.toString()
                        Log.d("Dashboard", "Total Meetings/Agendas: $totalAgendas")
                    } else {
                        findViewById<TextView>(R.id.tvUpcomingMeetings).text = "0"
                    }
                } catch (e: Exception) {
                    Log.e("Dashboard", "Error loading meeting count", e)
                    findViewById<TextView>(R.id.tvUpcomingMeetings).text = "0"
                }
            }

            // Documents count (placeholder - no document API yet)
            findViewById<TextView>(R.id.tvPendingDocuments).text = "0"
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Load menu based on user role
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()

        if (userRole == "USER") {
            // Load simplified menu for regular users
            navigationView.menu.clear()
            navigationView.inflateMenu(R.menu.nav_drawer_menu_user)
            Log.d("AdminDashboard", "Loaded simplified user menu")
        } else {
            // Load full admin menu for ADMIN and SUPER_ADMIN
            navigationView.menu.clear()
            navigationView.inflateMenu(R.menu.nav_drawer_menu)
            Log.d("AdminDashboard", "Loaded full admin menu")
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
        // Setup quick action card click listeners for admin/superadmin dashboard
        
        // MRFCs Card
        findViewById<MaterialCardView>(R.id.cardSelectMRFC).setOnClickListener {
            startActivity(Intent(this, MRFCListActivity::class.java))
        }

        // Users Card
        findViewById<MaterialCardView>(R.id.cardUsers).setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        // Meetings Card
        findViewById<MaterialCardView>(R.id.cardMeetings).setOnClickListener {
            startActivity(Intent(this, MeetingQuarterSelection::class.java))
        }

        // Documents Card
        findViewById<MaterialCardView>(R.id.cardDocuments).setOnClickListener {
            startActivity(Intent(this, FileUploadActivity::class.java))
        }

        // Compliance Card
        findViewById<MaterialCardView>(R.id.cardCompliance).setOnClickListener {
            // Navigate to MRFC List first - user needs to select an MRFC to view compliance
            Toast.makeText(this, "Please select an MRFC to view compliance", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MRFCListActivity::class.java))
        }

        // Reports Card
        findViewById<MaterialCardView>(R.id.cardReports).setOnClickListener {
            Toast.makeText(this, "Reports - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Dashboard
            R.id.nav_dashboard -> {
                // Already on dashboard
            }
            
            // Master Data Section
            R.id.nav_mrfc -> {
                startActivity(Intent(this, MRFCListActivity::class.java))
            }
            R.id.nav_proponents -> {
                Toast.makeText(this, "Proponents - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Proponents activity
            }
            R.id.nav_quarters -> {
                Toast.makeText(this, "Quarters Management - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Quarters activity
            }
            
            // Meetings Section
            R.id.nav_meetings -> {
                // Navigate to quarter selection for general meetings
                startActivity(Intent(this, MeetingQuarterSelection::class.java))
            }
            R.id.nav_agendas -> {
                // Navigate to all agendas view
                startActivity(Intent(this, MeetingQuarterSelection::class.java))
            }
            R.id.nav_attendance -> {
                startActivity(Intent(this, AttendanceActivity::class.java))
            }
            R.id.nav_minutes -> {
                Toast.makeText(this, "Minutes Management - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Minutes activity
            }
            R.id.nav_matters_arising -> {
                Toast.makeText(this, "Matters Arising - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Matters Arising activity
            }
            
            // Documents Section
            R.id.nav_documents -> {
                startActivity(Intent(this, FileUploadActivity::class.java))
            }
            R.id.nav_file_upload -> {
                startActivity(Intent(this, FileUploadActivity::class.java))
            }
            R.id.nav_document_review -> {
                startActivity(Intent(this, DocumentReviewActivity::class.java))
            }
            
            // Reports Section
            R.id.nav_compliance -> {
                // Navigate to MRFC List first - user needs to select an MRFC to view compliance
                Toast.makeText(this, "Please select an MRFC to view compliance", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MRFCListActivity::class.java))
            }
            R.id.nav_attendance_reports -> {
                Toast.makeText(this, "Attendance Reports - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Attendance Reports activity
            }
            R.id.nav_meeting_reports -> {
                Toast.makeText(this, "Meeting Reports - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Meeting Reports activity
            }
            R.id.nav_generate_reports -> {
                Toast.makeText(this, "Generate Reports - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Report Generator activity
            }
            
            // Users & Access Section
            R.id.nav_user_management -> {
                startActivity(Intent(this, UserManagementActivity::class.java))
            }
            R.id.nav_mrfc_access -> {
                Toast.makeText(this, "MRFC Access Control - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to MRFC Access Control activity
            }
            
            // Settings Section
            R.id.nav_settings -> {
                Toast.makeText(this, "System Settings - Coming Soon", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to Settings activity
            }
            R.id.nav_notifications -> {
                startActivity(Intent(this, NotificationActivity::class.java))
            }
            
            // Logout
            R.id.nav_logout -> {
                logout()
            }
            
            // Legacy menu items (for compatibility)
            R.id.nav_home -> {
                // Already on dashboard
            }
            R.id.nav_mrfc_management -> {
                // User menu: MRFC Management
                startActivity(Intent(this, MRFCListActivity::class.java))
            }
            R.id.nav_meeting_management -> {
                // User menu: Meeting Management
                startActivity(Intent(this, MeetingQuarterSelection::class.java))
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
