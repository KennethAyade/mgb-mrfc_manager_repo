package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.MrfcApiService
import com.mgb.mrfcmanager.data.remote.api.UserApiService
import com.mgb.mrfcmanager.data.remote.dto.MrfcDto
import com.mgb.mrfcmanager.data.remote.dto.UpdateUserRequest
import com.mgb.mrfcmanager.data.remote.dto.UserDto
import com.mgb.mrfcmanager.data.repository.MrfcRepository
import com.mgb.mrfcmanager.data.repository.UserRepository
import com.mgb.mrfcmanager.viewmodel.*

/**
 * Edit User Activity - Update existing user details
 * Allows editing: full_name, email, role, is_active, MRFC access
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

    // MRFC Access UI elements
    private lateinit var cardMrfcAccess: MaterialCardView
    private lateinit var rvMrfcList: RecyclerView
    private lateinit var progressBarMrfcs: ProgressBar
    private lateinit var tvNoMrfcs: TextView
    private lateinit var mrfcAdapter: MrfcCheckboxAdapter

    private lateinit var userViewModel: UserViewModel
    private lateinit var mrfcViewModel: MrfcViewModel
    private var userId: Long = -1
    private var currentUser: UserDto? = null
    private var currentUserRole: String = "USER"
    private var allMrfcs: List<MrfcDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRoleSpinner()
        setupMrfcRecyclerView()
        loadUserData()
        loadAllMrfcs()
        loadUserMrfcAccess()
        observeStates()
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

        // MRFC Access views
        cardMrfcAccess = findViewById(R.id.cardMrfcAccess)
        rvMrfcList = findViewById(R.id.rvMrfcList)
        progressBarMrfcs = findViewById(R.id.progressBarMrfcs)
        tvNoMrfcs = findViewById(R.id.tvNoMrfcs)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        
        // User ViewModel
        val userApiService = retrofit.create(UserApiService::class.java)
        val userRepository = UserRepository(userApiService)
        val userFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]

        // MRFC ViewModel
        val mrfcApiService = retrofit.create(MrfcApiService::class.java)
        val mrfcRepository = MrfcRepository(mrfcApiService)
        val mrfcFactory = MrfcViewModelFactory(mrfcRepository)
        mrfcViewModel = ViewModelProvider(this, mrfcFactory)[MrfcViewModel::class.java]

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

    private fun setupMrfcRecyclerView() {
        mrfcAdapter = MrfcCheckboxAdapter()
        rvMrfcList.apply {
            adapter = mrfcAdapter
            layoutManager = LinearLayoutManager(this@EditUserActivity)
        }
    }

    private fun loadAllMrfcs() {
        mrfcViewModel.loadAllMrfcs()
    }

    private fun loadUserMrfcAccess() {
        // Load user details with MRFC access from backend
        userViewModel.loadUserById(userId)
    }

    private fun observeStates() {
        // Observe user update state
        userViewModel.updateUserState.observe(this) { state ->
            when (state) {
                is UpdateUserState.Idle -> {
                    showLoading(false)
                }
                is UpdateUserState.Loading -> {
                    showLoading(true)
                }
                is UpdateUserState.Success -> {
                    showLoading(false)
                    // After user update, grant MRFC access
                    grantMrfcAccess()
                }
                is UpdateUserState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Update failed: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observe MRFC list state
        mrfcViewModel.mrfcListState.observe(this) { state ->
            when (state) {
                is MrfcListState.Loading -> {
                    showMrfcLoading(true)
                }
                is MrfcListState.Success -> {
                    showMrfcLoading(false)
                    allMrfcs = state.data
                    displayMrfcList(state.data)
                }
                is MrfcListState.Error -> {
                    showMrfcLoading(false)
                    tvNoMrfcs.visibility = View.VISIBLE
                    tvNoMrfcs.text = "Error loading MRFCs: ${state.message}"
                }
                else -> {}
            }
        }

        // Observe user detail state (to get current MRFC access)
        userViewModel.userDetailState.observe(this) { state ->
            when (state) {
                is UserDetailState.Success -> {
                    val user = state.user
                    val currentMrfcIds = user.mrfcAccess?.map { it.mrfcId } ?: emptyList()
                    mrfcAdapter.setSelectedMrfcs(currentMrfcIds)
                }
                is UserDetailState.Error -> {
                    Toast.makeText(this, "Error loading user MRFC access", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        // Observe MRFC access grant state
        userViewModel.grantMrfcAccessState.observe(this) { state ->
            when (state) {
                is GrantMrfcAccessState.Loading -> {
                    showLoading(true)
                }
                is GrantMrfcAccessState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "User and MRFC access updated successfully", Toast.LENGTH_SHORT).show()
                    userViewModel.resetGrantMrfcAccessState()
                    userViewModel.resetUpdateState()
                    setResult(RESULT_OK)
                    finish()
                }
                is GrantMrfcAccessState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "MRFC access update failed: ${state.message}", Toast.LENGTH_LONG).show()
                    userViewModel.resetGrantMrfcAccessState()
                }
                else -> {}
            }
        }
    }

    private fun displayMrfcList(mrfcs: List<MrfcDto>) {
        if (mrfcs.isEmpty()) {
            rvMrfcList.visibility = View.GONE
            tvNoMrfcs.visibility = View.VISIBLE
        } else {
            rvMrfcList.visibility = View.VISIBLE
            tvNoMrfcs.visibility = View.GONE
            mrfcAdapter.submitList(mrfcs)
        }
    }

    private fun showMrfcLoading(isLoading: Boolean) {
        progressBarMrfcs.visibility = if (isLoading) View.VISIBLE else View.GONE
        rvMrfcList.visibility = if (isLoading) View.GONE else rvMrfcList.visibility
        tvNoMrfcs.visibility = if (isLoading) View.GONE else tvNoMrfcs.visibility
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

        // Call ViewModel to update user (MRFC access will be granted after this succeeds)
        userViewModel.updateUser(userId, request)
    }

    private fun grantMrfcAccess() {
        // Get selected MRFC IDs from adapter
        val selectedMrfcIds = mrfcAdapter.getSelectedMrfcIds()
        
        // Grant MRFC access (can be empty list to revoke all access)
        userViewModel.grantMrfcAccess(userId, selectedMrfcIds)
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
