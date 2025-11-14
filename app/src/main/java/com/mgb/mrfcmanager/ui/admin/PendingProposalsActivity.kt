package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaDto
import com.mgb.mrfcmanager.data.repository.AgendaRepository
import com.mgb.mrfcmanager.ui.base.BaseActivity
import com.mgb.mrfcmanager.viewmodel.AgendaViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Pending Proposals Activity
 * Shows list of agenda proposals awaiting admin approval
 * Admins can approve or deny with remarks
 */
class PendingProposalsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: ProposalsAdapter
    private lateinit var viewModel: AgendaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_proposals)

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        loadProposals()
        
        // Setup floating home button
        setupHomeFab()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pending Agenda Proposals"
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
        val apiService = retrofit.create(AgendaApiService::class.java)
        val repository = AgendaRepository(apiService)
        val factory = AgendaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AgendaViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = ProposalsAdapter(
            onApproveClick = { proposal -> showApproveDialog(proposal) },
            onDenyClick = { proposal -> showDenyDialog(proposal) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadProposals() {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = viewModel.getPendingProposals()) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    showLoading(false)
                    displayProposals(result.data)
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this@PendingProposalsActivity, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                    showEmptyState()
                }
                else -> {}
            }
        }
    }

    private fun displayProposals(proposals: List<AgendaDto>) {
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
        tvEmptyState.text = "No pending proposals.\n\nAll agenda proposals have been reviewed!"
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else recyclerView.visibility
    }

    private fun showApproveDialog(proposal: AgendaDto) {
        AlertDialog.Builder(this)
            .setTitle("Approve Agenda Proposal")
            .setMessage("Approve this agenda proposal?\n\nMRFC: ${proposal.mrfcName ?: "General Meeting"}\nDate: ${proposal.meetingDate}\n\nThis will publish the agenda for the meeting.")
            .setPositiveButton("Approve") { _, _ ->
                approveProposal(proposal.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDenyDialog(proposal: AgendaDto) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_deny_proposal, null)
        val etRemarks = dialogView.findViewById<TextInputEditText>(R.id.etDenialRemarks)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnDeny).setOnClickListener {
            val remarks = etRemarks.text.toString().trim()
            
            if (remarks.isEmpty()) {
                etRemarks.error = "Please provide a reason for denial"
                return@setOnClickListener
            }

            denyProposal(proposal.id, remarks)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun approveProposal(agendaId: Long) {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = viewModel.approveProposal(agendaId)) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    showLoading(false)
                    Toast.makeText(this@PendingProposalsActivity, "Agenda approved successfully!", Toast.LENGTH_SHORT).show()
                    loadProposals() // Refresh list
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this@PendingProposalsActivity, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun denyProposal(agendaId: Long, remarks: String) {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = viewModel.denyProposal(agendaId, remarks)) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    showLoading(false)
                    Toast.makeText(this@PendingProposalsActivity, "Proposal denied", Toast.LENGTH_SHORT).show()
                    loadProposals() // Refresh list
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showLoading(false)
                    Toast.makeText(this@PendingProposalsActivity, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}

/**
 * Adapter for agenda proposals with approve/deny buttons
 */
class ProposalsAdapter(
    private val onApproveClick: (AgendaDto) -> Unit,
    private val onDenyClick: (AgendaDto) -> Unit
) : RecyclerView.Adapter<ProposalsAdapter.ProposalViewHolder>() {

    private val proposals = mutableListOf<AgendaDto>()

    fun submitList(newList: List<AgendaDto>) {
        proposals.clear()
        proposals.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProposalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda_proposal, parent, false)
        return ProposalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProposalViewHolder, position: Int) {
        holder.bind(proposals[position], onApproveClick, onDenyClick)
    }

    override fun getItemCount() = proposals.size

    class ProposalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMrfcName: TextView = itemView.findViewById(R.id.tvMrfcName)
        private val tvMeetingDate: TextView = itemView.findViewById(R.id.tvMeetingDate)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvProposedBy: TextView = itemView.findViewById(R.id.tvProposedBy)
        private val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        private val btnDeny: MaterialButton = itemView.findViewById(R.id.btnDeny)

        fun bind(
            proposal: AgendaDto,
            onApproveClick: (AgendaDto) -> Unit,
            onDenyClick: (AgendaDto) -> Unit
        ) {
            tvMrfcName.text = proposal.mrfcName ?: "General Meeting"
            
            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvMeetingDate.text = proposal.meetingDate
            
            tvLocation.text = proposal.location ?: "No location specified"
            tvProposedBy.text = "Proposed by: ${proposal.proposedBy ?: "Unknown"}"

            btnApprove.setOnClickListener { onApproveClick(proposal) }
            btnDeny.setOnClickListener { onDenyClick(proposal) }
        }
    }
}

