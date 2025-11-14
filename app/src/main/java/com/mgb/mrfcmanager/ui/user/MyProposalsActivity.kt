package com.mgb.mrfcmanager.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaItemApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.repository.AgendaItemRepository
import com.mgb.mrfcmanager.ui.base.BaseActivity
import com.mgb.mrfcmanager.viewmodel.AgendaItemViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaItemViewModelFactory
import kotlinx.coroutines.launch

/**
 * My Proposals Activity
 * Shows regular user's proposed agenda items with status
 * PROPOSED - Awaiting approval (orange)
 * APPROVED - Accepted (green)
 * DENIED - Rejected with admin remarks (red)
 */
class MyProposalsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: MyProposalsAdapter
    private lateinit var viewModel: AgendaItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_proposals)

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        loadMyProposals()
        
        setupHomeFab()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Proposals"
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.rvProposals)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }

    private fun setupViewModel() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val apiService = retrofit.create(AgendaItemApiService::class.java)
        val repository = AgendaItemRepository(apiService)
        val factory = AgendaItemViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AgendaItemViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = MyProposalsAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadMyProposals() {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = viewModel.getMyProposals()) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    showLoading(false)
                    displayProposals(result.data)
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this@MyProposalsActivity, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                    showEmptyState()
                }
                else -> {}
            }
        }
    }

    private fun displayProposals(proposals: List<AgendaItemDto>) {
        if (proposals.isEmpty()) {
            showEmptyState()
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            adapter.submitList(proposals)
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
        tvEmptyState.text = "No proposals yet.\n\nPropose agenda items in meeting agendas to see them here!"
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else recyclerView.visibility
    }
}

/**
 * Adapter for user's proposed items with status indicators
 */
class MyProposalsAdapter : RecyclerView.Adapter<MyProposalsAdapter.ProposalViewHolder>() {

    private val proposals = mutableListOf<AgendaItemDto>()

    fun submitList(newList: List<AgendaItemDto>) {
        proposals.clear()
        proposals.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProposalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_proposal, parent, false)
        return ProposalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProposalViewHolder, position: Int) {
        holder.bind(proposals[position])
    }

    override fun getItemCount() = proposals.size

    class ProposalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val cardStatus: MaterialCardView = itemView.findViewById(R.id.cardStatus)
        private val layoutDenialRemarks: View = itemView.findViewById(R.id.layoutDenialRemarks)
        private val tvDenialRemarks: TextView = itemView.findViewById(R.id.tvDenialRemarks)

        fun bind(item: AgendaItemDto) {
            tvTitle.text = item.title
            tvDescription.text = item.description ?: "No description"

            // Set status badge
            when (item.status) {
                "PROPOSED" -> {
                    tvStatus.text = "Pending Review"
                    cardStatus.setCardBackgroundColor(itemView.context.getColor(R.color.status_warning))
                    layoutDenialRemarks.visibility = View.GONE
                }
                "APPROVED" -> {
                    tvStatus.text = "Approved"
                    cardStatus.setCardBackgroundColor(itemView.context.getColor(R.color.status_success))
                    layoutDenialRemarks.visibility = View.GONE
                }
                "DENIED" -> {
                    tvStatus.text = "Denied"
                    cardStatus.setCardBackgroundColor(itemView.context.getColor(R.color.status_error))
                    // Show denial remarks
                    if (!item.denialRemarks.isNullOrBlank()) {
                        layoutDenialRemarks.visibility = View.VISIBLE
                        tvDenialRemarks.text = item.denialRemarks
                    } else {
                        layoutDenialRemarks.visibility = View.GONE
                    }
                }
                else -> {
                    tvStatus.text = item.status
                    cardStatus.setCardBackgroundColor(itemView.context.getColor(R.color.text_hint))
                    layoutDenialRemarks.visibility = View.GONE
                }
            }
        }
    }
}

