package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
import com.mgb.mrfcmanager.viewmodel.UserListState
import com.mgb.mrfcmanager.viewmodel.UserViewModel
import com.mgb.mrfcmanager.viewmodel.UserViewModelFactory

class UserManagementActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabCreateUser: FloatingActionButton
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnSearch: MaterialButton

    private lateinit var viewModel: UserViewModel
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        initViews()
        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        observeUsers()

        loadUsers()

        fabCreateUser.setOnClickListener {
            startActivity(Intent(this, CreateUserActivity::class.java))
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

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val userApiService = retrofit.create(UserApiService::class.java)
        val userRepository = UserRepository(userApiService)

        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user ->
            onUserClick(user)
        }

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

    private fun loadUsers(search: String? = null) {
        viewModel.loadUsers(search = search)
    }

    private fun onUserClick(user: UserDto) {
        Toast.makeText(this, "User: ${user.username} (${user.role})", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to UserDetailActivity
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
