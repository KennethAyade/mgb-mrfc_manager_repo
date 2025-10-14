package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.ui.auth.LoginActivity

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
        setContentView(R.layout.activity_admin_dashboard)

        setupToolbar()
        setupNavigationDrawer()
        setupDashboardCards()
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
            Toast.makeText(this, "Attendance - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.cardFileUpload).setOnClickListener {
            Toast.makeText(this, "File Upload - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.cardCompliance).setOnClickListener {
            Toast.makeText(this, "Compliance Dashboard - Coming Soon", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Proponents - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_agenda -> {
                Toast.makeText(this, "Agenda - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_attendance -> {
                Toast.makeText(this, "Attendance - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_minutes -> {
                Toast.makeText(this, "Minutes - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_documents -> {
                Toast.makeText(this, "Documents - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_file_upload -> {
                Toast.makeText(this, "File Upload - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_compliance -> {
                Toast.makeText(this, "Compliance Dashboard - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_reports -> {
                Toast.makeText(this, "Reports - Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_notifications -> {
                Toast.makeText(this, "Notifications - Coming Soon", Toast.LENGTH_SHORT).show()
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
        // TODO: BACKEND - Clear session/token
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
