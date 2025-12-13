package com.mgb.mrfcmanager.ui.meeting.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaItemApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.repository.AgendaItemRepository
import com.mgb.mrfcmanager.viewmodel.AgendaItemViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaItemViewModelFactory
import com.mgb.mrfcmanager.viewmodel.ItemsListState
import kotlinx.coroutines.launch

/**
 * Proposals Fragment
 * Shows proposed agenda items awaiting approval
 * - Regular users: See only their own proposals
 * - Admins: See all proposals with approve/deny buttons
 */
class ProposalsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: ProposalsFragmentAdapter
    private lateinit var viewModel: AgendaItemViewModel

    private var agendaId: Long = 0L
    private var isAdmin: Boolean = false

    companion object {
        private const val ARG_AGENDA_ID = "agenda_id"

        fun newInstance(agendaId: Long): ProposalsFragment {
            return ProposalsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_AGENDA_ID, agendaId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            agendaId = it.getLong(ARG_AGENDA_ID)
        }

        // Check user role
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        isAdmin = userRole == "ADMIN" || userRole == "SUPER_ADMIN"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_proposals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupViewModel()
        setupRecyclerView()
        loadProposals()
    }

    override fun onResume() {
        super.onResume()
        // FIX: Don't auto-reload on every resume - this causes HTTP 429 errors
        // Data is already loaded in onViewCreated()
        // Users can manually refresh if needed
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.rvProposals)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
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
        adapter = ProposalsFragmentAdapter(
            isAdmin = isAdmin,
            onApproveClick = { item -> showApproveDialog(item) },
            onDenyClick = { item -> showDenyDialog(item) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadProposals() {
        showLoading(true)
        viewModel.loadItemsByAgenda(agendaId)
        
        viewModel.itemsListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ItemsListState.Loading -> showLoading(true)
                is ItemsListState.Success -> {
                    showLoading(false)
                    // Filter to show only PROPOSED and DENIED items
                    val proposals = if (isAdmin) {
                        // Admins see all PROPOSED items from all users
                        state.data.filter { it.status == "PROPOSED" || it.status == "DENIED" }
                    } else {
                        // Regular users see their own PROPOSED and DENIED items
                        val userId = MRFCManagerApp.getTokenManager().getUserId()
                        state.data.filter { 
                            (it.status == "PROPOSED" || it.status == "DENIED") && it.addedBy == userId
                        }
                    }
                    displayProposals(proposals)
                }
                is ItemsListState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
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
        tvEmptyState.text = if (isAdmin) {
            "No pending proposals.\n\nAll agenda items have been reviewed!"
        } else {
            "No proposals yet.\n\nAdd agenda items from the Agenda tab.\nYour proposals will appear here for review."
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showApproveDialog(item: AgendaItemDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Approve Proposal")
            .setMessage("Approve \"${item.title}\"?\n\nProposed by: ${item.addedByName}\n\nThis item will be added to the official agenda.")
            .setPositiveButton("Approve") { _, _ ->
                approveItem(item.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDenyDialog(item: AgendaItemDto) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_deny_proposal, null)
        val etRemarks = dialogView.findViewById<TextInputEditText>(R.id.etDenialRemarks)

        val dialog = AlertDialog.Builder(requireContext())
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

            denyItem(item.id, remarks)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun approveItem(itemId: Long) {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = viewModel.approveItem(itemId)) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Agenda item approved!", Toast.LENGTH_SHORT).show()
                    loadProposals() // Refresh list
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun denyItem(itemId: Long, remarks: String) {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = viewModel.denyItem(itemId, remarks)) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Proposal denied", Toast.LENGTH_SHORT).show()
                    loadProposals() // Refresh list
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}

/**
 * Adapter for proposals with role-based UI
 */
class ProposalsFragmentAdapter(
    private val isAdmin: Boolean,
    private val onApproveClick: (AgendaItemDto) -> Unit,
    private val onDenyClick: (AgendaItemDto) -> Unit
) : RecyclerView.Adapter<ProposalsFragmentAdapter.ViewHolder>() {

    private val proposals = mutableListOf<AgendaItemDto>()

    fun submitList(newList: List<AgendaItemDto>) {
        proposals.clear()
        proposals.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proposal_in_meeting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(proposals[position], isAdmin, onApproveClick, onDenyClick)
    }

    override fun getItemCount() = proposals.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvProposedBy: TextView = itemView.findViewById(R.id.tvProposedBy)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val cardStatus: MaterialCardView = itemView.findViewById(R.id.cardStatus)
        private val layoutDenialRemarks: View = itemView.findViewById(R.id.layoutDenialRemarks)
        private val tvDenialRemarks: TextView = itemView.findViewById(R.id.tvDenialRemarks)
        private val layoutAdminActions: View = itemView.findViewById(R.id.layoutAdminActions)
        private val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        private val btnDeny: MaterialButton = itemView.findViewById(R.id.btnDeny)

        fun bind(
            item: AgendaItemDto,
            isAdmin: Boolean,
            onApproveClick: (AgendaItemDto) -> Unit,
            onDenyClick: (AgendaItemDto) -> Unit
        ) {
            tvTitle.text = item.title
            tvDescription.text = item.description ?: "No description"
            tvProposedBy.text = "Proposed by: ${item.addedByName}"

            // Set status badge and admin actions
            when (item.status) {
                "PROPOSED" -> {
                    tvStatus.text = "Pending"
                    cardStatus.setCardBackgroundColor(itemView.context.getColor(R.color.status_warning))
                    layoutDenialRemarks.visibility = View.GONE
                    
                    // Show approve/deny buttons for admins
                    if (isAdmin) {
                        layoutAdminActions.visibility = View.VISIBLE
                        btnApprove.setOnClickListener { onApproveClick(item) }
                        btnDeny.setOnClickListener { onDenyClick(item) }
                    } else {
                        layoutAdminActions.visibility = View.GONE
                    }
                }
                "DENIED" -> {
                    tvStatus.text = "Denied"
                    cardStatus.setCardBackgroundColor(itemView.context.getColor(R.color.status_error))
                    layoutAdminActions.visibility = View.GONE
                    
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
                    layoutAdminActions.visibility = View.GONE
                }
            }
        }
    }
}

