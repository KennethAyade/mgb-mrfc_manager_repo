package com.mgb.mrfcmanager.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.ui.admin.AdminDashboardActivity
import com.mgb.mrfcmanager.ui.user.UserDashboardActivity

/**
 * Login Screen - Handles user authentication
 * TODO: BACKEND - Replace hardcoded login with actual authentication
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val username = etUsername.text.toString()
        val password = etPassword.text.toString()

        // Simple hardcoded authentication
        // TODO: BACKEND - Replace with actual authentication service
        when {
            username == "admin" && password == "admin123" -> {
                navigateToAdminDashboard()
            }
            username == "user1" && password == "user123" -> {
                navigateToUserDashboard()
            }
            else -> {
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToAdminDashboard() {
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }

    private fun navigateToUserDashboard() {
        startActivity(Intent(this, UserDashboardActivity::class.java))
        finish()
    }
}
