package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.UserApiService
import com.mgb.mrfcmanager.data.remote.dto.CreateUserRequest
import com.mgb.mrfcmanager.data.repository.UserRepository
import com.mgb.mrfcmanager.viewmodel.CreateUserState
import com.mgb.mrfcmanager.viewmodel.UserViewModel
import com.mgb.mrfcmanager.viewmodel.UserViewModelFactory

class CreateUserActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnCreateUser: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: UserViewModel
    private var currentUserRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        initViews()
        setupToolbar()
        setupViewModel()
        setupRoleSpinner()
        observeCreateUser()

        btnCreateUser.setOnClickListener {
            handleCreateUser()
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        spinnerRole = findViewById(R.id.spinnerRole)
        btnCreateUser = findViewById(R.id.btnCreateUser)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create User"
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        currentUserRole = tokenManager.getUserRole()

        val retrofit = RetrofitClient.getInstance(tokenManager)
        val userApiService = retrofit.create(UserApiService::class.java)
        val userRepository = UserRepository(userApiService)

        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun setupRoleSpinner() {
        // SUPER_ADMIN can create ADMIN and USER
        // ADMIN can only create USER
        val roles = if (currentUserRole == "SUPER_ADMIN") {
            arrayOf("USER", "ADMIN")
        } else {
            arrayOf("USER")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter
    }

    private fun observeCreateUser() {
        viewModel.createUserState.observe(this) { state ->
            when (state) {
                is CreateUserState.Idle -> {
                    showLoading(false)
                }
                is CreateUserState.Loading -> {
                    showLoading(true)
                }
                is CreateUserState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Return to UserManagementActivity
                }
                is CreateUserState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleCreateUser() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val role = spinnerRole.selectedItem.toString()

        // Validation
        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user
        val request = CreateUserRequest(
            username = username,
            password = password,
            fullName = fullName,
            email = email,
            role = role
        )

        viewModel.createUser(request)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnCreateUser.isEnabled = !isLoading
        etUsername.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        etFullName.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        spinnerRole.isEnabled = !isLoading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
