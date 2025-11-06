package com.mgb.mrfcmanager.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.ProponentApiService
import com.mgb.mrfcmanager.data.remote.dto.ProponentDto
import com.mgb.mrfcmanager.data.repository.ProponentRepository
import com.mgb.mrfcmanager.utils.TokenManager
import com.mgb.mrfcmanager.viewmodel.ProponentListState
import com.mgb.mrfcmanager.viewmodel.ProponentViewModel
import com.mgb.mrfcmanager.viewmodel.ProponentViewModelFactory
import java.text.NumberFormat
import java.util.Locale

/**
 * Proponent List Activity - Displays proponents for a specific MRFC
 * Integrated with backend API
 */
class ProponentListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProponentAdapter
    private lateinit var tvMRFCName: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var viewModel: ProponentViewModel

    private var mrfcId: Long = -1
    private var mrfcName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proponent_list)

        loadMRFCInfo()
        setupToolbar()
        initViews()
        setupViewModel()
        setupRecyclerView()
        observeProponentList()
        setupFAB()
        setupSwipeRefresh()

        // Load proponents for this MRFC
        if (mrfcId != -1L) {
            viewModel.loadProponentsByMrfc(mrfcId)
        } else {
            Toast.makeText(this, "Invalid MRFC ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadMRFCInfo() {
        mrfcId = intent.getLongExtra("MRFC_ID", -1)
        mrfcName = intent.getStringExtra("MRFC_NAME") ?: intent.getStringExtra("MRFC_NUMBER") ?: "MRFC"
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Proponents"
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        tvMRFCName = findViewById(R.id.tvMRFCName)
        tvMRFCName.text = mrfcName
        recyclerView = findViewById(R.id.rvProponentList)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val proponentApiService = retrofit.create(ProponentApiService::class.java)
        val repository = ProponentRepository(proponentApiService)

        val factory = ProponentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProponentViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // Use GridLayoutManager with column count based on screen width
        // 1 column for phones, 2 columns for tablets
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnCount = if (screenWidthDp >= 600) 2 else 1
        recyclerView.layoutManager = GridLayoutManager(this, columnCount)

        adapter = ProponentAdapter(emptyList()) { proponent ->
            onProponentClicked(proponent)
        }
        recyclerView.adapter = adapter
    }

    private fun observeProponentList() {
        viewModel.proponentListState.observe(this) { state ->
            when (state) {
                is ProponentListState.Idle -> {
                    showLoading(false)
                }
                is ProponentListState.Loading -> {
                    showLoading(true)
                    hideError()
                }
                is ProponentListState.Success -> {
                    showLoading(false)
                    hideError()
                    adapter.updateData(state.data)

                    if (state.data.isEmpty()) {
                        showError("No proponents found for this MRFC")
                    }
                }
                is ProponentListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.setOnRefreshListener {
        //     viewModel.refresh(mrfcId)
        //     swipeRefresh.isRefreshing = false
        // }
    }

    private fun setupFAB() {
        findViewById<FloatingActionButton>(R.id.fabAddProponent).setOnClickListener {
            val intent = Intent(this, ProponentFormActivity::class.java)
            intent.putExtra("MRFC_ID", mrfcId)
            startActivityForResult(intent, ProponentFormActivity.REQUEST_CODE_CREATE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ProponentFormActivity.REQUEST_CODE_CREATE,
                ProponentFormActivity.REQUEST_CODE_EDIT -> {
                    // Refresh list after create/edit
                    viewModel.loadProponentsByMrfc(mrfcId)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        // Show as Snackbar for better UX
        com.google.android.material.snackbar.Snackbar.make(
            findViewById(android.R.id.content),
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    private fun hideError() {
        // Snackbar auto-dismisses, no action needed
    }

    private fun onProponentClicked(proponent: ProponentDto) {
        val intent = Intent(this, ProponentDetailActivity::class.java)
        intent.putExtra("PROPONENT_ID", proponent.id)
        intent.putExtra("MRFC_ID", mrfcId)
        startActivityForResult(intent, ProponentFormActivity.REQUEST_CODE_EDIT)
    }
}

/**
 * Adapter for Proponent List
 * Updated to use ProponentDto from backend
 */
class ProponentAdapter(
    private var proponentList: List<ProponentDto>,
    private val onItemClick: (ProponentDto) -> Unit
) : RecyclerView.Adapter<ProponentAdapter.ViewHolder>() {

    fun updateData(newList: List<ProponentDto>) {
        proponentList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proponent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val proponent = proponentList[position]
        holder.bind(proponent, position + 1, onItemClick)
    }

    override fun getItemCount() = proponentList.size

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val cardProponent: MaterialCardView = itemView.findViewById(R.id.cardProponent)
        private val tvProponentNumber: TextView = itemView.findViewById(R.id.tvProponentNumber)
        private val tvProponentName: TextView = itemView.findViewById(R.id.tvProponentName)
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(proponent: ProponentDto, number: Int, onItemClick: (ProponentDto) -> Unit) {
            tvProponentNumber.text = number.toString()
            tvProponentName.text = proponent.name
            tvCompanyName.text = proponent.companyName

            // Display status
            tvStatus.text = proponent.status

            // Set status color based on backend status
            val statusColor = when (proponent.status.uppercase()) {
                "ACTIVE" -> R.color.status_compliant
                "INACTIVE" -> R.color.status_non_compliant
                "SUSPENDED" -> R.color.status_pending
                else -> R.color.status_pending
            }
            tvStatus.setTextColor(itemView.context.getColor(statusColor))

            cardProponent.setOnClickListener { onItemClick(proponent) }
        }
    }
}
