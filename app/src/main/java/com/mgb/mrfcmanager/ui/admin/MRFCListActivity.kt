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
class MRFCListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
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
                    hideError()
                }
                is MrfcListState.Success -> {
                    showLoading(false)
                    hideError()
                    adapter.updateData(state.data)

                    if (state.data.isEmpty()) {
                        showError("No MRFCs found")
                    }
                }
                is MrfcListState.Error -> {
                    showLoading(false)
                    showError(state.message)
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
            Toast.makeText(this, "Add MRFC - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        // TODO: Add tvError to layout XML
        // tvError.visibility = View.VISIBLE
        // tvError.text = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hideError() {
        // TODO: Add tvError to layout XML
        // tvError.visibility = View.GONE
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
        return ViewHolder(view as MaterialButton)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mrfc = mrfcList[position]
        holder.bind(mrfc, onItemClick)
    }

    override fun getItemCount() = mrfcList.size

    class ViewHolder(private val button: MaterialButton) : RecyclerView.ViewHolder(button) {
        fun bind(mrfc: MrfcDto, onItemClick: (MrfcDto) -> Unit) {
            // Display MRFC number and location
            button.text = "${mrfc.mrfcNumber}\n${mrfc.location}"
            button.setOnClickListener { onItemClick(mrfc) }
        }
    }
}
