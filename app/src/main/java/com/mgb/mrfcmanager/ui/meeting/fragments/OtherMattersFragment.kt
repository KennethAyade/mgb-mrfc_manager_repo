package com.mgb.mrfcmanager.ui.meeting.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaItemApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.remote.dto.CreateAgendaItemRequest
import com.mgb.mrfcmanager.viewmodel.MeetingRealtimeViewModel
import kotlinx.coroutines.launch

/**
 * Other Matters Fragment
 * Shows agenda items marked as "Other Matters" - items added after the main agenda is finalized
 * These are approved items that were discussed outside the regular agenda
 *
 * ALL users can view and add other matters
 * Only ADMIN can mark existing items as "other matters"
 */
class OtherMattersFragment : Fragment() {

    private val realtimeViewModel: MeetingRealtimeViewModel by activityViewModels()
    private var lastRealtimeRefreshAt: Long = 0L

    private lateinit var rvOtherMatters: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var tvOfflineBanner: TextView? = null

    private lateinit var apiService: AgendaItemApiService
    private lateinit var adapter: OtherMattersAdapter

    private val otherMattersList = mutableListOf<AgendaItemDto>()
    private var agendaId: Long = 0L
    private var mrfcId: Long = 0L
    private var isAdmin: Boolean = false

    // Smart polling for highlight sync (10 second interval)
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            silentRefresh()
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }
    private var lastRefreshTime = 0L
    private var isPollingActive = false

    companion object {
        private const val ARG_AGENDA_ID = "agenda_id"
        private const val ARG_MRFC_ID = "mrfc_id"
        private const val REFRESH_INTERVAL_MS = 10_000L // 10 seconds
        private const val MIN_REFRESH_INTERVAL_MS = 5_000L // Minimum 5 seconds between refreshes

        fun newInstance(agendaId: Long, mrfcId: Long): OtherMattersFragment {
            return OtherMattersFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_AGENDA_ID, agendaId)
                    putLong(ARG_MRFC_ID, mrfcId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            agendaId = it.getLong(ARG_AGENDA_ID)
            mrfcId = it.getLong(ARG_MRFC_ID)
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
        return inflater.inflate(R.layout.fragment_other_matters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupApiService()
        setupRecyclerView()
        observeRealtimeEvents()
        loadOtherMatters()

        fabAddItem.setOnClickListener {
            showAddOtherMatterDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Start smart polling for highlight sync
        startPolling()
        updateOfflineIndicator()
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPolling()
    }

    private fun startPolling() {
        if (!isPollingActive) {
            isPollingActive = true
            // Run immediately (tab return should sync highlights right away)
            refreshHandler.post(refreshRunnable)
        }
    }

    private fun stopPolling() {
        isPollingActive = false
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun silentRefresh() {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < MIN_REFRESH_INTERVAL_MS) return

        val networkManager = MRFCManagerApp.getNetworkManager()
        if (!networkManager.isNetworkAvailable()) {
            updateOfflineIndicator()
            return
        }

        lastRefreshTime = now

        lifecycleScope.launch {
            try {
                val response = apiService.getOtherMatters(agendaId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data ?: emptyList()
                    if (hasChanges(items)) {
                        updateList(items)
                    }
                }
                hideOfflineIndicator()
            } catch (e: Exception) {
                updateOfflineIndicator()
            }
        }
    }

    private fun hasChanges(newItems: List<AgendaItemDto>): Boolean {
        if (newItems.size != otherMattersList.size) return true
        for (newItem in newItems) {
            val existingItem = otherMattersList.find { it.id == newItem.id }
            if (existingItem == null || existingItem.isHighlighted != newItem.isHighlighted) {
                return true
            }
        }
        return false
    }

    private fun updateOfflineIndicator() {
        val networkManager = MRFCManagerApp.getNetworkManager()
        if (!networkManager.isNetworkAvailable()) {
            tvOfflineBanner?.visibility = View.VISIBLE
        } else {
            tvOfflineBanner?.visibility = View.GONE
        }
    }

    private fun hideOfflineIndicator() {
        tvOfflineBanner?.visibility = View.GONE
    }

    private fun initializeViews(view: View) {
        rvOtherMatters = view.findViewById(R.id.rvOtherMatters)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        fabAddItem = view.findViewById(R.id.fabAddItem)
        progressBar = view.findViewById(R.id.progressBar)
        tvOfflineBanner = view.findViewById(R.id.tvOfflineBanner)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Setup SwipeRefresh for manual refresh (allows users to sync highlight updates)
        swipeRefreshLayout.setColorSchemeResources(R.color.tab_icon_other_matters)
        swipeRefreshLayout.setOnRefreshListener {
            loadOtherMatters()
        }
    }

    private fun setupApiService() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        apiService = retrofit.create(AgendaItemApiService::class.java)
    }

    private fun setupRecyclerView() {
        adapter = OtherMattersAdapter(
            items = otherMattersList,
            isAdmin = isAdmin,
            onItemClick = { item -> showItemDetailDialog(item) },
            onHighlightClick = { item -> toggleHighlight(item) }
        )
        rvOtherMatters.layoutManager = LinearLayoutManager(requireContext())
        rvOtherMatters.adapter = adapter
    }

    private fun observeRealtimeEvents() {
        realtimeViewModel.events.observe(viewLifecycleOwner) { evt ->
            evt ?: return@observe
            if (evt.agendaId != agendaId) return@observe

            val now = System.currentTimeMillis()
            if (now - lastRealtimeRefreshAt < 500L) return@observe
            lastRealtimeRefreshAt = now

            // Force refresh even if we recently refreshed via polling
            lastRefreshTime = 0L
            silentRefresh()
        }
    }

    private fun loadOtherMatters() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = apiService.getOtherMatters(agendaId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data ?: emptyList()
                    // ✅ FIX: Trust backend filtering - don't filter again!
                    // Backend already returns:
                    // - For USER: APPROVED items + user's own PROPOSED items
                    // - For ADMIN: ALL items (PROPOSED, APPROVED, DENIED)
                    updateList(items)
                } else {
                    showError("Failed to load other matters")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateList(items: List<AgendaItemDto>) {
        otherMattersList.clear()
        otherMattersList.addAll(items)
        adapter.notifyDataSetChanged()

        if (otherMattersList.isEmpty()) {
            rvOtherMatters.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "No other matters yet.\n\nUse the + button to add items discussed outside the regular agenda."
        } else {
            rvOtherMatters.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun showAddOtherMatterDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_other_matter, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Other Matter")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    addOtherMatter(title, description)
                } else {
                    Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addOtherMatter(title: String, description: String) {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        val isAdminUser = userRole == "ADMIN" || userRole == "SUPER_ADMIN"

        showLoading(true)
        lifecycleScope.launch {
            try {
                val request = CreateAgendaItemRequest(
                    agendaId = agendaId,
                    title = title,
                    description = description.ifEmpty { null },
                    orderIndex = otherMattersList.size,
                    isOtherMatter = true  // Mark as other matter
                )

                val response = apiService.createItem(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val message = if (isAdminUser) {
                        "Other matter added successfully!"
                    } else {
                        "Proposal submitted! It will appear after admin approval."
                    }

                    view?.let {
                        Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
                    }
                    loadOtherMatters()
                } else {
                    showError("Failed to add other matter")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showItemDetailDialog(item: AgendaItemDto) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_other_matter_detail, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set data
        dialogView.findViewById<TextView>(R.id.tvTitle).text = item.title
        dialogView.findViewById<TextView>(R.id.tvDescription).apply {
            text = item.description ?: "No description"
            visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        dialogView.findViewById<TextView>(R.id.tvAddedBy).text = "Added by: ${item.addedByName}"

        // Status badges
        val tvApprovalStatus = dialogView.findViewById<TextView>(R.id.tvApprovalStatus)
        val tvHighlightStatus = dialogView.findViewById<TextView>(R.id.tvHighlightStatus)
        val layoutDenialRemarks = dialogView.findViewById<View>(R.id.layoutDenialRemarks)
        val tvDenialRemarks = dialogView.findViewById<TextView>(R.id.tvDenialRemarks)

        // Approval status badge
        when (item.status) {
            "PROPOSED" -> {
                tvApprovalStatus.visibility = View.VISIBLE
                tvApprovalStatus.text = "Pending approval"
                tvApprovalStatus.setBackgroundResource(R.drawable.bg_rounded_orange)
                layoutDenialRemarks.visibility = View.GONE
            }
            "DENIED" -> {
                tvApprovalStatus.visibility = View.VISIBLE
                tvApprovalStatus.text = "Denied"
                tvApprovalStatus.setBackgroundResource(R.drawable.bg_rounded_red)
                if (!item.denialRemarks.isNullOrBlank()) {
                    layoutDenialRemarks.visibility = View.VISIBLE
                    tvDenialRemarks.text = item.denialRemarks
                } else {
                    layoutDenialRemarks.visibility = View.GONE
                }
            }
            "APPROVED" -> {
                tvApprovalStatus.visibility = View.GONE
                layoutDenialRemarks.visibility = View.GONE
            }
            else -> {
                tvApprovalStatus.visibility = View.GONE
                layoutDenialRemarks.visibility = View.GONE
            }
        }

        // Highlight badge
        if (item.isHighlighted) {
            tvHighlightStatus.visibility = View.VISIBLE
            tvHighlightStatus.text = "Discussed"
            tvHighlightStatus.setBackgroundResource(R.drawable.bg_rounded_green)
        } else {
            tvHighlightStatus.visibility = View.GONE
        }

        // Admin approval actions (approve/deny for PROPOSED items)
        val layoutApprovalActions = dialogView.findViewById<View>(R.id.layoutApprovalActions)
        val btnApprove = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApprove)
        val btnDeny = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDeny)
        if (isAdmin && item.status == "PROPOSED") {
            layoutApprovalActions.visibility = View.VISIBLE
            btnApprove.setOnClickListener {
                dialog.dismiss()
                showApproveDialog(item)
            }
            btnDeny.setOnClickListener {
                dialog.dismiss()
                showDenyDialog(item)
            }
        } else {
            layoutApprovalActions.visibility = View.GONE
        }

        // Admin highlight toggle button
        val btnToggleHighlight = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnToggleHighlight)
        if (isAdmin) {
            btnToggleHighlight.visibility = View.VISIBLE
            btnToggleHighlight.text = if (item.isHighlighted) "Mark as Pending" else "Mark as Discussed"
            btnToggleHighlight.setOnClickListener {
                dialog.dismiss()
                toggleHighlight(item)
            }
        } else {
            btnToggleHighlight.visibility = View.GONE
        }

        // Close button
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun toggleHighlight(item: AgendaItemDto) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = apiService.toggleHighlight(item.id)
                if (response.isSuccessful && response.body()?.success == true) {
                    val message = if (item.isHighlighted) "Marked as pending" else "Marked as discussed"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    loadOtherMatters()
                } else {
                    showError("Failed to update highlight status")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Also stop swipe refresh animation when loading is done
        if (!isLoading && ::swipeRefreshLayout.isInitialized) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showApproveDialog(item: AgendaItemDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Approve Other Matter")
            .setMessage("Approve \"${item.title}\"?\n\nProposed by: ${item.addedByName}\n\nThis item will be added to the Other Matters agenda.")
            .setPositiveButton("Approve") { _, _ ->
                approveItem(item.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDenyDialog(item: AgendaItemDto) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_deny_proposal, null)
        val etRemarks = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDenialRemarks)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDeny).setOnClickListener {
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
            try {
                val response = apiService.approveItem(itemId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Other matter approved!", Toast.LENGTH_SHORT).show()
                    loadOtherMatters()
                } else {
                    showError("Failed to approve item")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun denyItem(itemId: Long, remarks: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val request = com.mgb.mrfcmanager.data.remote.dto.DenyProposalRequest(denialRemarks = remarks)
                val response = apiService.denyItem(itemId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Proposal denied", Toast.LENGTH_SHORT).show()
                    loadOtherMatters()
                } else {
                    showError("Failed to deny item")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
}

/**
 * Adapter for Other Matters list
 * Shows items with highlight status (green background for discussed items)
 */
class OtherMattersAdapter(
    private val items: List<AgendaItemDto>,
    private val isAdmin: Boolean,
    private val onItemClick: (AgendaItemDto) -> Unit,
    private val onHighlightClick: (AgendaItemDto) -> Unit
) : RecyclerView.Adapter<OtherMattersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_other_matter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position + 1, isAdmin, onItemClick, onHighlightClick)
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardItem: com.google.android.material.card.MaterialCardView =
            itemView.findViewById(R.id.cardItem)
        private val tvItemNumber: TextView = itemView.findViewById(R.id.tvItemNumber)
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val tvItemDescription: TextView = itemView.findViewById(R.id.tvItemDescription)
        private val ivHighlightIndicator: ImageView = itemView.findViewById(R.id.ivHighlightIndicator)
        private val btnToggleHighlight: ImageButton? = itemView.findViewById(R.id.btnToggleHighlight)

        fun bind(
            item: AgendaItemDto,
            number: Int,
            isAdmin: Boolean,
            onItemClick: (AgendaItemDto) -> Unit,
            onHighlightClick: (AgendaItemDto) -> Unit
        ) {
            tvItemNumber.text = "$number."
            tvItemTitle.text = item.title
            tvItemDescription.text = item.description ?: "No description"
            tvItemDescription.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Apply highlight styling (green background for discussed items)
            if (item.isHighlighted) {
                cardItem.setCardBackgroundColor(itemView.context.getColor(R.color.agenda_item_highlighted_bg))
                cardItem.strokeColor = itemView.context.getColor(R.color.agenda_item_highlighted_border)
                cardItem.strokeWidth = 2
                ivHighlightIndicator.visibility = View.VISIBLE
                ivHighlightIndicator.setColorFilter(itemView.context.getColor(R.color.status_success))
            } else {
                cardItem.setCardBackgroundColor(itemView.context.getColor(R.color.surface))
                cardItem.strokeColor = itemView.context.getColor(R.color.divider)
                cardItem.strokeWidth = 1
                ivHighlightIndicator.visibility = View.GONE
            }

            // Admin highlight toggle button
            btnToggleHighlight?.apply {
                visibility = if (isAdmin) View.VISIBLE else View.GONE
                setOnClickListener { onHighlightClick(item) }
            }

            // Item click handler
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
