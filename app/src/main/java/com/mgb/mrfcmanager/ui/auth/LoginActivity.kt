package com.mgb.mrfcmanager.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AuthApiService
import com.mgb.mrfcmanager.data.repository.AuthRepository
import com.mgb.mrfcmanager.ui.admin.AdminDashboardActivity
import com.mgb.mrfcmanager.viewmodel.LoginState
import com.mgb.mrfcmanager.viewmodel.LoginViewModel
import com.mgb.mrfcmanager.viewmodel.LoginViewModelFactory

/**
 * Login Screen - Handles user authentication with backend API
 * Integrated with backend authentication system
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupViewModel()
        observeLoginState()

        btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // Try to find progressBar, create if not exists in layout
        progressBar = try {
            findViewById(R.id.progressBar)
        } catch (e: Exception) {
            // If progressBar doesn't exist in layout, we'll handle loading via button state
            ProgressBar(this).apply { visibility = View.GONE }
        }
    }

    private fun setupViewModel() {
        // Use singleton TokenManager from Application class to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val authApiService = retrofit.create(AuthApiService::class.java)
        val authRepository = AuthRepository(authApiService, tokenManager)

        // Create ViewModel
        val factory = LoginViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        Log.d("LoginActivity", "ViewModel setup completed")
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            Log.d("LoginActivity", "Login state changed: ${state.javaClass.simpleName}")
            when (state) {
                is LoginState.Idle -> {
                    Log.d("LoginActivity", "State: Idle")
                    showLoading(false)
                }
                is LoginState.Loading -> {
                    Log.d("LoginActivity", "State: Loading")
                    showLoading(true)
                }
                is LoginState.Success -> {
                    Log.d("LoginActivity", "State: Success - User: ${state.data.user.username}, Role: ${state.data.user.role}")
                    showLoading(false)
                    val role = state.data.user.role ?: "USER" // Default to USER if role is null
                    navigateToDashboard(role)
                }
                is LoginState.Error -> {
                    Log.e("LoginActivity", "State: Error - ${state.message}")
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun handleLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()
        viewModel.login(username, password)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        etUsername.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading

        // Update button text
        btnLogin.text = if (isLoading) "Logging in..." else "Login"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToDashboard(role: String) {
        Log.d("LoginActivity", "Navigating to dashboard for role: $role")
        // All users (USER, ADMIN, SUPER_ADMIN) now get AdminDashboardActivity
        // which has sidebar navigation for MRFC and Meeting Management
        val intent = Intent(this, AdminDashboardActivity::class.java)
        Log.d("LoginActivity", "Starting AdminDashboardActivity for role: $role")
        startActivity(intent)
        finish()
        Log.d("LoginActivity", "Navigation completed")
    }
}
