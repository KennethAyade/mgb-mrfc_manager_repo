package com.mgb.mrfcmanager.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.MRFC
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.MrfcApiService
import com.mgb.mrfcmanager.data.remote.dto.MrfcDto
import com.mgb.mrfcmanager.data.repository.MrfcRepository
import com.mgb.mrfcmanager.viewmodel.MrfcListState
import com.mgb.mrfcmanager.viewmodel.MrfcViewModel
import com.mgb.mrfcmanager.viewmodel.MrfcViewModelFactory

class MRFCSelectionActivity : AppCompatActivity() {

    private lateinit var rvMRFCList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var mrfcAdapter: MRFCUserAdapter
    private lateinit var viewModel: MrfcViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrfc_selection)

        initializeViews()
        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        loadMRFCs()
    }

    private fun initializeViews() {
        rvMRFCList = findViewById(R.id.rvMRFCList)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Select MRFC"
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val mrfcApiService = retrofit.create(MrfcApiService::class.java)
        val mrfcRepository = MrfcRepository(mrfcApiService)
        val factory = MrfcViewModelFactory(mrfcRepository)
        viewModel = ViewModelProvider(this, factory)[MrfcViewModel::class.java]
    }

    private fun setupRecyclerView() {
        rvMRFCList.layoutManager = LinearLayoutManager(this)
        mrfcAdapter = MRFCUserAdapter(emptyList()) { mrfc ->
            onMRFCSelected(mrfc)
        }
        rvMRFCList.adapter = mrfcAdapter
    }

    private fun observeViewModel() {
        viewModel.mrfcListState.observe(this) { state ->
            when (state) {
                is MrfcListState.Loading -> showLoading(true)
                is MrfcListState.Success -> {
                    showLoading(false)
                    displayMRFCs(state.data)
                }
                is MrfcListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is MrfcListState.Idle -> showLoading(false)
            }
        }
    }

    private fun loadMRFCs() {
        // Backend automatically filters by user's mrfcAccess array
        viewModel.loadAllMrfcs(activeOnly = true)
    }

    private fun displayMRFCs(mrfcs: List<MrfcDto>) {
        if (mrfcs.isEmpty()) {
            rvMRFCList.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            // BUG FIX 5: More helpful message explaining MRFC access
            tvEmptyState.text = "No MRFCs Assigned\n\n" +
                    "You don't have access to any MRFCs yet.\n\n" +
                    "To get access:\n" +
                    "1. Contact your Super Admin\n" +
                    "2. Ask them to assign MRFCs to your account\n" +
                    "3. They can do this in User Management > Edit User\n\n" +
                    "Once assigned, you'll be able to view and manage those MRFCs."
        } else {
            rvMRFCList.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
                // Convert MrfcDto to MRFC model for adapter
                val mrfcList = mrfcs.map { dto ->
                    MRFC(
                        id = dto.id,
                        name = dto.name,
                        municipality = dto.municipality,
                        contactPerson = dto.chairpersonName ?: "",
                        contactNumber = dto.contactNumber ?: ""
                    )
                }
            mrfcAdapter.updateData(mrfcList)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        rvMRFCList.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
        tvEmptyState.visibility = View.VISIBLE
        tvEmptyState.text = "Failed to load MRFCs.\n$message"
    }

    private fun onMRFCSelected(mrfc: MRFC) {
        // BUG FIX: Check DESTINATION parameter to route to correct activity
        val destination = intent.getStringExtra("DESTINATION")
        
        val intent = when (destination) {
            "NOTES" -> {
                // Route to Notes Activity
                Intent(this, NotesActivity::class.java).apply {
                    putExtra("MRFC_ID", mrfc.id)
                    putExtra("MRFC_NAME", mrfc.name)
                    putExtra("QUARTER", "")
                }
            }
            "DOCUMENTS" -> {
                // Route to Documents Activity
                Intent(this, DocumentListActivity::class.java).apply {
                    putExtra("MRFC_ID", mrfc.id)
                    putExtra("MRFC_NAME", mrfc.name)
                }
            }
            else -> {
                // Default: Route to Proponent View Activity
                Intent(this, ProponentViewActivity::class.java).apply {
                    putExtra("MRFC_ID", mrfc.id)
                    putExtra("MRFC_NAME", mrfc.name)
                }
            }
        }
        
        startActivity(intent)
        finish() // Close selection screen after selection
    }

    // Adapter for MRFC list (User portal - read-only)
    class MRFCUserAdapter(
        private var mrfcList: List<MRFC>,
        private val onItemClick: (MRFC) -> Unit
    ) : RecyclerView.Adapter<MRFCUserAdapter.ViewHolder>() {

        fun updateData(newList: List<MRFC>) {
            mrfcList = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mrfc_user, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(mrfcList[position], onItemClick)
        }

        override fun getItemCount() = mrfcList.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMRFCName: TextView = itemView.findViewById(R.id.tvMRFCName)
            private val tvMunicipality: TextView = itemView.findViewById(R.id.tvMunicipality)

            fun bind(mrfc: MRFC, onItemClick: (MRFC) -> Unit) {
                tvMRFCName.text = mrfc.name
                tvMunicipality.text = mrfc.municipality

                itemView.setOnClickListener {
                    onItemClick(mrfc)
                }
            }
        }
    }
}
