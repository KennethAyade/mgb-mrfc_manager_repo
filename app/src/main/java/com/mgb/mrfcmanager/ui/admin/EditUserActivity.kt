package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.UserApiService
import com.mgb.mrfcmanager.data.remote.dto.UpdateUserRequest
import com.mgb.mrfcmanager.data.remote.dto.UserDto
import com.mgb.mrfcmanager.data.repository.UserRepository
import com.mgb.mrfcmanager.viewmodel.UpdateUserState
import com.mgb.mrfcmanager.viewmodel.UserViewModel
import com.mgb.mrfcmanager.viewmodel.UserViewModelFactory

/**
 * Edit User Activity - Update existing user details
 * Allows editing: full_name, email, role, is_active
 * Requires ADMIN or SUPER_ADMIN role
 */
class EditUserActivity : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var spinnerRole: Spinner
    private lateinit var switchActive: SwitchMaterial
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: UserViewModel
    private var userId: Long = -1
    private var currentUser: UserDto? = null
    private var currentUserRole: String = "USER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRoleSpinner()
        loadUserData()
        observeUpdateState()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit User"
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        spinnerRole = findViewById(R.id.spinnerRole)
        switchActive = findViewById(R.id.switchActive)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val userApiService = retrofit.create(UserApiService::class.java)
        val repository = UserRepository(userApiService)
        val factory = UserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        // Get current user role from intent
        currentUserRole = intent.getStringExtra("CURRENT_USER_ROLE") ?: "USER"
    }

    private fun setupRoleSpinner() {
        // SUPER_ADMIN can assign any role except SUPER_ADMIN
        // ADMIN can only assign USER role
        val roles = if (currentUserRole == "SUPER_ADMIN") {
            arrayOf("USER", "ADMIN")
        } else {
            arrayOf("USER")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter
    }

    private fun loadUserData() {
        userId = intent.getLongExtra("USER_ID", -1)
        val username = intent.getStringExtra("USERNAME") ?: ""
        val fullName = intent.getStringExtra("FULL_NAME") ?: ""
        val email = intent.getStringExtra("EMAIL") ?: ""
        val role = intent.getStringExtra("ROLE") ?: "USER"
        val isActive = intent.getBooleanExtra("IS_ACTIVE", true)

        if (userId == -1L) {
            Toast.makeText(this, "Error: Invalid user ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set form values
        etFullName.setText(fullName)
        etEmail.setText(email)
        switchActive.isChecked = isActive

        // Set role spinner selection
        val rolePosition = when (role) {
            "ADMIN" -> 1
            else -> 0  // USER
        }
        spinnerRole.setSelection(rolePosition)

        // Disable role editing if current user is not SUPER_ADMIN or editing SUPER_ADMIN
        if (currentUserRole != "SUPER_ADMIN" || role == "SUPER_ADMIN") {
            spinnerRole.isEnabled = false
        }
    }

    private fun observeUpdateState() {
        viewModel.updateUserState.observe(this) { state ->
            when (state) {
                is UpdateUserState.Idle -> {
                    showLoading(false)
                }
                is UpdateUserState.Loading -> {
                    showLoading(true)
                }
                is UpdateUserState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is UpdateUserState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Update failed: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveUserChanges()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveUserChanges() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val role = when (spinnerRole.selectedItemPosition) {
            1 -> "ADMIN"
            else -> "USER"
        }
        val isActive = switchActive.isChecked

        // Validation
        if (fullName.isEmpty()) {
            etFullName.error = "Full name is required"
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            return
        }

        // Create update request
        val request = UpdateUserRequest(
            fullName = fullName,
            email = email,
            role = role,
            isActive = isActive
        )

        // Call ViewModel to update user
        viewModel.updateUser(userId, request)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !isLoading
        btnCancel.isEnabled = !isLoading
        etFullName.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        spinnerRole.isEnabled = !isLoading
        switchActive.isEnabled = !isLoading
    }
}
