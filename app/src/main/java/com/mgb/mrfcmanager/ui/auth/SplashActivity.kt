package com.mgb.mrfcmanager.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AuthApiService
import com.mgb.mrfcmanager.data.repository.AuthRepository
import com.mgb.mrfcmanager.ui.admin.AdminDashboardActivity
import com.mgb.mrfcmanager.ui.user.UserDashboardActivity

/**
 * Splash Screen - Shows app branding and checks authentication status
 * Navigates to appropriate screen based on login state
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.d("SplashActivity", "SplashActivity started")

        // Use singleton TokenManager from Application class
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val authApiService = retrofit.create(AuthApiService::class.java)
        authRepository = AuthRepository(authApiService, tokenManager)

        // Check auth status after 2 seconds (splash duration)
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthStatus()
        }, 2000)
    }

    private fun checkAuthStatus() {
        val isLoggedIn = authRepository.isLoggedIn()
        Log.d("SplashActivity", "Checking auth status: isLoggedIn=$isLoggedIn")

        if (isLoggedIn) {
            // User is logged in, route to appropriate dashboard based on role
            val role = authRepository.getUserRole()
            Log.d("SplashActivity", "User is logged in with role: $role")
            val intent = when (role) {
                "USER" -> Intent(this, UserDashboardActivity::class.java)
                "ADMIN", "SUPER_ADMIN" -> Intent(this, AdminDashboardActivity::class.java)
                else -> Intent(this, UserDashboardActivity::class.java)
            }
            startActivity(intent)
        } else {
            // Not logged in, go to login screen
            Log.d("SplashActivity", "User not logged in, navigating to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
