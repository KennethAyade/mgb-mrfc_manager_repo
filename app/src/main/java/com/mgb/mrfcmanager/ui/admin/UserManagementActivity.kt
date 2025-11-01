package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.UserApiService
import com.mgb.mrfcmanager.data.remote.dto.UserDto
import com.mgb.mrfcmanager.data.repository.UserRepository
import com.mgb.mrfcmanager.ui.adapter.UserAdapter
import com.mgb.mrfcmanager.viewmodel.DeleteUserState
import com.mgb.mrfcmanager.viewmodel.UserListState
import com.mgb.mrfcmanager.viewmodel.UserViewModel
import com.mgb.mrfcmanager.viewmodel.UserViewModelFactory
import kotlinx.coroutines.launch

class UserManagementActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabCreateUser: FloatingActionButton
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnSearch: MaterialButton

    private lateinit var viewModel: UserViewModel
    private lateinit var userAdapter: UserAdapter
    private var currentUserRole: String = "USER"

    companion object {
        private const val REQUEST_EDIT_USER = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        initViews()
        setupToolbar()
        getCurrentUserRole()
        setupViewModel()
        setupRecyclerView()
        observeUsers()
        observeDeleteUser()

        loadUsers()

        fabCreateUser.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java)
            intent.putExtra("CURRENT_USER_ROLE", currentUserRole)
            startActivity(intent)
        }

        btnSearch.setOnClickListener {
            val searchQuery = etSearch.text.toString().trim()
            loadUsers(search = searchQuery.ifEmpty { null })
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewUsers)
        progressBar = findViewById(R.id.progressBar)
        fabCreateUser = findViewById(R.id.fabCreateUser)
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "User Management"
    }

    private fun getCurrentUserRole() {
        // Get current user role from TokenManager
        lifecycleScope.launch {
            val tokenManager = MRFCManagerApp.getTokenManager()
            currentUserRole = tokenManager.getUserRole() ?: "USER"
        }
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val userApiService = retrofit.create(UserApiService::class.java)
        val userRepository = UserRepository(userApiService)

        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onUserClick = { user -> onUserClick(user) },
            onEditClick = { user -> onEditClick(user) },
            onDeleteClick = { user -> onDeleteClick(user) },
            onToggleStatusClick = { user -> onToggleStatusClick(user) }
        )

        userAdapter.setCurrentUserRole(currentUserRole)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserManagementActivity)
            adapter = userAdapter
        }
    }

    private fun observeUsers() {
        viewModel.usersState.observe(this) { state ->
            when (state) {
                is UserListState.Idle -> {
                    progressBar.visibility = View.GONE
                }
                is UserListState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is UserListState.Success -> {
                    progressBar.visibility = View.GONE
                    userAdapter.submitList(state.data.users)
                }
                is UserListState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeDeleteUser() {
        viewModel.deleteUserState.observe(this) { state ->
            when (state) {
                is DeleteUserState.Idle -> {
                    // Do nothing
                }
                is DeleteUserState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is DeleteUserState.Success -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show()
                    loadUsers() // Reload list
                }
                is DeleteUserState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Delete failed: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadUsers(search: String? = null) {
        viewModel.loadUsers(search = search)
    }

    private fun onUserClick(user: UserDto) {
        Toast.makeText(this, "User: ${user.username} (${user.role})", Toast.LENGTH_SHORT).show()
    }

    private fun onEditClick(user: UserDto) {
        val intent = Intent(this, EditUserActivity::class.java).apply {
            putExtra("USER_ID", user.id)
            putExtra("USERNAME", user.username)
            putExtra("FULL_NAME", user.fullName)
            putExtra("EMAIL", user.email)
            putExtra("ROLE", user.role)
            putExtra("IS_ACTIVE", user.isActive)
            putExtra("CURRENT_USER_ROLE", currentUserRole)
        }
        startActivityForResult(intent, REQUEST_EDIT_USER)
    }

    private fun onDeleteClick(user: UserDto) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete user '${user.fullName}' (@${user.username})?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteUser(user.id)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun onToggleStatusClick(user: UserDto) {
        val action = if (user.isActive) "deactivate" else "activate"
        val newStatus = if (user.isActive) "inactive" else "active"

        AlertDialog.Builder(this)
            .setTitle("${action.replaceFirstChar { it.uppercase() }} User")
            .setMessage("Are you sure you want to $action user '${user.fullName}' (@${user.username})?\n\nThe user will be $newStatus.")
            .setPositiveButton(action.replaceFirstChar { it.uppercase() }) { _, _ ->
                viewModel.toggleUserStatus(user.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_USER && resultCode == RESULT_OK) {
            // Reload users after successful edit
            loadUsers()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUsers() // Reload users when returning from CreateUserActivity
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
