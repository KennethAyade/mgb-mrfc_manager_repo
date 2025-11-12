package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.mgb.mrfcmanager.ui.base.BaseActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.MrfcApiService
import com.mgb.mrfcmanager.data.remote.dto.MrfcDto
import com.mgb.mrfcmanager.data.repository.MrfcRepository
import com.mgb.mrfcmanager.viewmodel.MrfcListState
import com.mgb.mrfcmanager.viewmodel.MrfcViewModel
import com.mgb.mrfcmanager.viewmodel.MrfcViewModelFactory

/**
 * MRFC List Activity - Displays list of all MRFCs
 * Integrated with backend API
 */
class MRFCListActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: MRFCAdapter
    private lateinit var viewModel: MrfcViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mrfc_list)

        setupToolbar()
        initViews()
        setupViewModel()
        setupRecyclerView()
        observeMrfcList()
        setupFAB()
        setupSwipeRefresh()
        setupHomeFab() // Enable home button

        // Load data
        viewModel.loadAllMrfcs()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvMRFCList)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val mrfcApiService = retrofit.create(MrfcApiService::class.java)
        val repository = MrfcRepository(mrfcApiService)

        val factory = MrfcViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MrfcViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // Use GridLayoutManager with column count based on screen width
        // 1 column for phones, 2 columns for tablets
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnCount = if (screenWidthDp >= 600) 2 else 1
        recyclerView.layoutManager = GridLayoutManager(this, columnCount)

        adapter = MRFCAdapter(emptyList()) { mrfc ->
            onMRFCClicked(mrfc)
        }
        recyclerView.adapter = adapter
    }

    private fun observeMrfcList() {
        viewModel.mrfcListState.observe(this) { state ->
            when (state) {
                is MrfcListState.Idle -> {
                    showLoading(false)
                }
                is MrfcListState.Loading -> {
                    showLoading(true)
                    hideEmptyState()
                }
                is MrfcListState.Success -> {
                    showLoading(false)
                    adapter.updateData(state.data)

                    // Show empty state if no data
                    if (state.data.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                    }
                }
                is MrfcListState.Error -> {
                    showLoading(false)
                    hideEmptyState()
                    // Only show error dialog for actual errors, not empty lists
                    showError(state.message, "Error Loading MRFCs")
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.setOnRefreshListener {
        //     viewModel.refresh()
        //     swipeRefresh.isRefreshing = false
        // }
    }

    private fun setupFAB() {
        findViewById<FloatingActionButton>(R.id.fabAddMRFC).setOnClickListener {
            startActivityForResult(
                Intent(this, CreateMRFCActivity::class.java),
                CreateMRFCActivity.REQUEST_CODE_CREATE_MRFC
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CreateMRFCActivity.REQUEST_CODE_CREATE_MRFC && resultCode == RESULT_OK) {
            // Refresh the list after creating a new MRFC
            viewModel.loadAllMrfcs()
            showSuccess("MRFC created successfully")
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        if (isLoading) {
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun showEmptyState() {
        tvEmptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        tvEmptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun onMRFCClicked(mrfc: MrfcDto) {
        // Navigate to MRFC Detail
        val intent = Intent(this, MRFCDetailActivity::class.java)
        intent.putExtra("MRFC_ID", mrfc.id)
        intent.putExtra("MRFC_NUMBER", mrfc.mrfcNumber)
        startActivity(intent)
    }
}

/**
 * Adapter for MRFC List
 * Updated to use MrfcDto from backend
 */
class MRFCAdapter(
    private var mrfcList: List<MrfcDto>,
    private val onItemClick: (MrfcDto) -> Unit
) : RecyclerView.Adapter<MRFCAdapter.ViewHolder>() {

    fun updateData(newList: List<MrfcDto>) {
        mrfcList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mrfc, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mrfc = mrfcList[position]
        holder.bind(mrfc, onItemClick)
    }

    override fun getItemCount() = mrfcList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMrfcCode: TextView = itemView.findViewById(R.id.tvMrfcCode)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        private val tvMrfcName: TextView = itemView.findViewById(R.id.tvMrfcName)
        private val tvMunicipality: TextView = itemView.findViewById(R.id.tvMunicipality)
        private val tvProvince: TextView = itemView.findViewById(R.id.tvProvince)
        private val tvRegion: TextView = itemView.findViewById(R.id.tvRegion)
        private val tvContactPerson: TextView = itemView.findViewById(R.id.tvContactPerson)
        private val tvContactEmail: TextView = itemView.findViewById(R.id.tvContactEmail)

        fun bind(mrfc: MrfcDto, onItemClick: (MrfcDto) -> Unit) {
            // Display MRFC code (e.g., "MRFC-6015")
            tvMrfcCode.text = if (mrfc.mrfcNumber.isNullOrEmpty()) {
                "MRFC-${mrfc.id}"
            } else {
                mrfc.mrfcNumber
            }

            // Display status badge
            val statusText = if (mrfc.isActive) "Active" else "Inactive"
            tvStatusBadge.text = statusText
            tvStatusBadge.setTextColor(
                if (mrfc.isActive) {
                    itemView.context.getColor(android.R.color.holo_green_dark)
                } else {
                    itemView.context.getColor(android.R.color.darker_gray)
                }
            )

            // Display MRFC name
            tvMrfcName.text = mrfc.name

            // Display Municipality (non-nullable, but can be empty)
            tvMunicipality.text = mrfc.municipality.ifEmpty { "Not specified" }

            // Display Province (nullable)
            tvProvince.text = mrfc.province?.ifEmpty { "Not specified" } ?: "Not specified"

            // Display Region (nullable)
            tvRegion.text = mrfc.region?.ifEmpty { "Not specified" } ?: "Not specified"

            // Display contact person (nullable)
            tvContactPerson.text = mrfc.contactPerson?.ifEmpty { "Not specified" } ?: "Not specified"

            // Display contact email (nullable, field is called 'email')
            if (mrfc.email.isNullOrEmpty()) {
                tvContactEmail.visibility = View.GONE
            } else {
                tvContactEmail.visibility = View.VISIBLE
                tvContactEmail.text = mrfc.email
            }

            // Set click listener
            itemView.setOnClickListener { onItemClick(mrfc) }
        }
    }
}
