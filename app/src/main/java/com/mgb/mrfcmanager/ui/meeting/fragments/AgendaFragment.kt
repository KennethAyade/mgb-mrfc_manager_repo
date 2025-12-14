package com.mgb.mrfcmanager.ui.meeting.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import kotlinx.coroutines.launch
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.AgendaItemApiService
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto
import com.mgb.mrfcmanager.data.repository.AgendaItemRepository
import com.mgb.mrfcmanager.data.repository.Result
import com.mgb.mrfcmanager.viewmodel.AgendaItemViewModel
import com.mgb.mrfcmanager.viewmodel.AgendaItemViewModelFactory
import com.mgb.mrfcmanager.viewmodel.ItemsListState
import com.mgb.mrfcmanager.viewmodel.MeetingRealtimeViewModel

/**
 * Agenda Fragment
 * Shows list of agenda items (discussion topics)
 * ALL users can view and add agenda items
 */
class AgendaFragment : Fragment() {

    private val realtimeViewModel: MeetingRealtimeViewModel by activityViewModels()
    private var lastRealtimeRefreshAt: Long = 0L

    private lateinit var rvAgendaItems: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var tvOfflineBanner: TextView? = null

    // Other Matters section (shown at bottom of Agenda tab)
    private lateinit var tvOtherMattersHeader: TextView
    private lateinit var rvOtherMattersInAgenda: RecyclerView
    private lateinit var tvOtherMattersEmptyState: TextView
    private lateinit var otherMattersAdapter: OtherMattersInAgendaAdapter
    private val otherMattersApproved = mutableListOf<AgendaItemDto>()

    private lateinit var apiService: AgendaItemApiService

    private lateinit var viewModel: AgendaItemViewModel
    private lateinit var agendaItemAdapter: AgendaItemAdapter

    private val agendaItems = mutableListOf<AgendaItemDto>()
    private var agendaId: Long = 0L
    private var mrfcId: Long = 0L

    // Smart polling for highlight sync (10 second interval)
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            // Silent refresh - only if network is available
            silentRefresh()
            // Schedule next refresh
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

        fun newInstance(agendaId: Long, mrfcId: Long): AgendaFragment {
            return AgendaFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_AGENDA_ID, agendaId)
                    putLong(ARG_MRFC_ID, mrfcId)
                }
            }
        }
    }

    // Data for dropdowns
    private val mrfcList = mutableListOf<Pair<Long, String>>() // ID to Name
    private val proponentList = mutableListOf<Pair<Long, String>>() // ID to Name
    private val fileCategoryList = listOf(
        "CMVR" to "CMVR",
        "RESEARCH_ACCOMPLISHMENTS" to "Research Accomplishments",
        "SDMP" to "SDMP",
        "PRODUCTION_REPORT" to "Production Report",
        "SAFETY_REPORT" to "Safety Report",
        "MTF_REPORT" to "MTF Disbursement",
        "AEPEP" to "AEPEP Report",
        "OTHER" to "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            agendaId = it.getLong(ARG_AGENDA_ID)
            mrfcId = it.getLong(ARG_MRFC_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agenda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupApiService()
        setupViewModel()
        setupRecyclerView()
        setupOtherMattersSection()
        observeViewModel()
        observeRealtimeEvents()
        loadAgendaItems()
        loadOtherMattersForAgendaSection()
        loadMRFCsAndProponentsData() // Load MRFC and Proponent names for detail dialog

        fabAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Start smart polling for highlight sync.
        // Phase 1: refresh immediately on tab return (no initial 30s wait).
        startPolling()
        // Update offline indicator
        updateOfflineIndicator()
    }

    override fun onPause() {
        super.onPause()
        // Stop polling when fragment is not visible
        stopPolling()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up handler callbacks
        stopPolling()
    }

    /**
     * Start polling for agenda updates
     * Phase 1: run the first refresh immediately when the fragment becomes visible,
     * then continue polling at REFRESH_INTERVAL_MS.
     */
    private fun startPolling() {
        if (!isPollingActive) {
            isPollingActive = true
            // Run immediately (tab return should sync highlights right away)
            refreshHandler.post(refreshRunnable)
        }
    }

    /**
     * Stop polling when fragment is paused or destroyed
     */
    private fun stopPolling() {
        isPollingActive = false
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    /**
     * Silent refresh - fetches new data without showing loading indicator
     * Only runs if network is available and minimum interval has passed
     */
    private fun silentRefresh() {
        val now = System.currentTimeMillis()
        // Respect minimum interval to prevent excessive API calls
        if (now - lastRefreshTime < MIN_REFRESH_INTERVAL_MS) {
            return
        }

        // Check network availability
        val networkManager = MRFCManagerApp.getNetworkManager()
        if (!networkManager.isNetworkAvailable()) {
            updateOfflineIndicator()
            return
        }

        lastRefreshTime = now

        // Silent load - don't show loading spinner
        lifecycleScope.launch {
            try {
                val response = apiService.getItemsByAgenda(agendaId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data ?: emptyList()
                    // Only update if data has changed (check for highlight changes)
                    val approvedItems = items.filter { it.status == "APPROVED" }
                    if (hasHighlightChanges(approvedItems)) {
                        updateAgendaItems(approvedItems)
                    }
                }

                // Also refresh other matters section
                val otherMattersResponse = apiService.getOtherMatters(agendaId)
                if (otherMattersResponse.isSuccessful && otherMattersResponse.body()?.success == true) {
                    val otherItems = otherMattersResponse.body()?.data ?: emptyList()
                    val approved = otherItems.filter { it.status == "APPROVED" }
                    updateOtherMattersSection(approved)
                }

                // Hide offline indicator on success
                hideOfflineIndicator()
            } catch (e: Exception) {
                // Silent fail - don't show error for background refresh
                updateOfflineIndicator()
            }
        }
    }

    /**
     * Check if there are highlight changes that need UI update
     */
    private fun hasHighlightChanges(newItems: List<AgendaItemDto>): Boolean {
        if (newItems.size != agendaItems.size) return true

        for (newItem in newItems) {
            val existingItem = agendaItems.find { it.id == newItem.id }
            if (existingItem == null || existingItem.isHighlighted != newItem.isHighlighted) {
                return true
            }
        }
        return false
    }

    /**
     * Update offline indicator based on network status
     */
    private fun updateOfflineIndicator() {
        val networkManager = MRFCManagerApp.getNetworkManager()
        if (!networkManager.isNetworkAvailable()) {
            showOfflineIndicator()
        } else {
            hideOfflineIndicator()
        }
    }

    /**
     * Show offline indicator banner
     */
    private fun showOfflineIndicator() {
        tvOfflineBanner?.visibility = View.VISIBLE
    }

    /**
     * Hide offline indicator banner
     */
    private fun hideOfflineIndicator() {
        tvOfflineBanner?.visibility = View.GONE
    }

    private fun initializeViews(view: View) {
        rvAgendaItems = view.findViewById(R.id.rvAgendaItems)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        // Offline banner (may be null if not in layout)
        tvOfflineBanner = view.findViewById(R.id.tvOfflineBanner)
        fabAddItem = view.findViewById(R.id.fabAddItem)
        progressBar = view.findViewById(R.id.progressBar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // Other Matters section views
        tvOtherMattersHeader = view.findViewById(R.id.tvOtherMattersHeader)
        rvOtherMattersInAgenda = view.findViewById(R.id.rvOtherMattersInAgenda)
        tvOtherMattersEmptyState = view.findViewById(R.id.tvOtherMattersEmptyState)

        // Prevent nested RecyclerView scroll conflicts (layout uses NestedScrollView)
        rvAgendaItems.isNestedScrollingEnabled = false
        rvOtherMattersInAgenda.isNestedScrollingEnabled = false

        // Setup SwipeRefresh for manual refresh (allows users to sync agenda highlights)
        swipeRefreshLayout.setColorSchemeResources(R.color.primary)
        swipeRefreshLayout.setOnRefreshListener {
            // Refresh both agenda items and other matters
            loadAgendaItems()
            loadOtherMattersForAgendaSection()
        }

        // Hide FAB for USER role - only admins can add agenda items directly
        // Regular users should use "Other Matters" tab to create proposals
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        val isAdmin = userRole == "ADMIN" || userRole == "SUPER_ADMIN"

        if (!isAdmin) {
            fabAddItem.visibility = android.view.View.GONE
        }
    }

    private fun setupApiService() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        apiService = retrofit.create(AgendaItemApiService::class.java)
    }

    private fun setupViewModel() {
        val repository = AgendaItemRepository(apiService)
        val factory = AgendaItemViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AgendaItemViewModel::class.java]
    }

    private fun setupRecyclerView() {
        agendaItemAdapter = AgendaItemAdapter(agendaItems) { item ->
            showAgendaItemDetailDialog(item)
        }
        rvAgendaItems.layoutManager = LinearLayoutManager(requireContext())
        rvAgendaItems.adapter = agendaItemAdapter
    }

    private fun setupOtherMattersSection() {
        otherMattersAdapter = OtherMattersInAgendaAdapter(otherMattersApproved)
        rvOtherMattersInAgenda.layoutManager = LinearLayoutManager(requireContext())
        rvOtherMattersInAgenda.adapter = otherMattersAdapter

        // Default state
        tvOtherMattersHeader.visibility = View.VISIBLE
        tvOtherMattersEmptyState.visibility = View.GONE
    }

    private fun observeViewModel() {
        viewModel.itemsListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ItemsListState.Loading -> {
                    showLoading(true)
                }
                is ItemsListState.Success -> {
                    showLoading(false)
                    updateAgendaItems(state.data)
                }
                is ItemsListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is ItemsListState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun observeRealtimeEvents() {
        realtimeViewModel.events.observe(viewLifecycleOwner) { evt ->
            evt ?: return@observe
            // Ignore events for other meetings
            if (evt.agendaId != agendaId) return@observe

            // Debounce to avoid refresh storms
            val now = System.currentTimeMillis()
            if (now - lastRealtimeRefreshAt < 500L) return@observe
            lastRealtimeRefreshAt = now

            // Force refresh even if we recently refreshed via polling
            lastRefreshTime = 0L
            silentRefresh()
        }
    }

    private fun loadAgendaItems() {
        viewModel.loadItemsByAgenda(agendaId)
    }

    private fun loadOtherMattersForAgendaSection() {
        lifecycleScope.launch {
            try {
                val response = apiService.getOtherMatters(agendaId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data ?: emptyList()
                    // Agenda tab should only show visible (approved) other matters.
                    val approved = items.filter { it.status == "APPROVED" }
                    updateOtherMattersSection(approved)
                } else {
                    updateOtherMattersSection(emptyList())
                }
            } catch (_: Exception) {
                updateOtherMattersSection(emptyList())
            }
        }
    }

    private fun updateOtherMattersSection(items: List<AgendaItemDto>) {
        otherMattersApproved.clear()
        otherMattersApproved.addAll(items)
        otherMattersAdapter.notifyDataSetChanged()

        if (otherMattersApproved.isEmpty()) {
            tvOtherMattersEmptyState.visibility = View.VISIBLE
        } else {
            tvOtherMattersEmptyState.visibility = View.GONE
        }
    }

    private fun loadMRFCsAndProponentsData() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)

        // Load MRFCs from API
        lifecycleScope.launch {
            try {
                val mrfcApiService = retrofit.create(com.mgb.mrfcmanager.data.remote.api.MrfcApiService::class.java)
                val response = mrfcApiService.getAllMrfcs(page = 1, limit = 100)
                val apiResponse = response.body()
                if (response.isSuccessful && apiResponse?.success == true) {
                    mrfcList.clear()
                    apiResponse.data?.mrfcs?.forEach { mrfc ->
                        mrfcList.add(mrfc.id to mrfc.name)
                    }
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }

        // Load Proponents from API - filter by MRFC if available
        lifecycleScope.launch {
            try {
                val proponentApiService = retrofit.create(com.mgb.mrfcmanager.data.remote.api.ProponentApiService::class.java)
                // Pass mrfcId to filter proponents by the current MRFC (0 means general meeting, show all)
                val filterMrfcId = if (mrfcId > 0) mrfcId else null
                val response = proponentApiService.getAllProponents(page = 1, limit = 100, mrfcId = filterMrfcId)
                val apiResponse = response.body()
                if (response.isSuccessful && apiResponse?.success == true) {
                    proponentList.clear()
                    apiResponse.data?.proponents?.forEach { proponent ->
                        proponentList.add(proponent.id to proponent.name)
                    }
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    private fun updateAgendaItems(items: List<AgendaItemDto>) {
        agendaItems.clear()
        // DOUBLE-CHECK: Only show APPROVED items in Agenda tab
        // (Backend should filter, but this is a safety check)
        val approvedItems = items.filter { it.status == "APPROVED" }
        agendaItems.addAll(approvedItems)
        agendaItemAdapter.notifyDataSetChanged()

        if (agendaItems.isEmpty()) {
            rvAgendaItems.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "No agenda items yet.\n\nAdd items using the + button.\n(Regular users: Your proposals will appear after admin approval)"
        } else {
            rvAgendaItems.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_agenda_item, null)

        val spinnerMrfc = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerMrfc)
        val spinnerProponent = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerProponent)
        val spinnerFileCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerFileCategory)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        // Populate File Category dropdown
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            fileCategoryList.map { it.second }
        )
        spinnerFileCategory.setAdapter(categoryAdapter)

        // Load MRFCs and Proponents for dropdowns
        loadMRFCsAndProponents(spinnerMrfc, spinnerProponent)

        // Track selected values
        var selectedMrfcId: Long? = null
        var selectedProponentId: Long? = null
        var selectedFileCategory: String? = null

        spinnerMrfc.setOnItemClickListener { _, _, position, _ ->
            selectedMrfcId = mrfcList[position].first

            // Reload proponents filtered by the selected MRFC
            selectedProponentId = null
            spinnerProponent.text.clear()
            loadProponentsForMrfc(selectedMrfcId, spinnerProponent)
        }

        spinnerProponent.setOnItemClickListener { _, _, position, _ ->
            selectedProponentId = proponentList[position].first
        }

        spinnerFileCategory.setOnItemClickListener { _, _, position, _ ->
            selectedFileCategory = fileCategoryList[position].first
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Agenda Item")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    addAgendaItem(
                        title = title,
                        description = description,
                        mrfcId = selectedMrfcId,
                        proponentId = selectedProponentId,
                        fileCategory = selectedFileCategory
                    )
                } else {
                    Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadMRFCsAndProponents(spinnerMrfc: AutoCompleteTextView, spinnerProponent: AutoCompleteTextView) {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)

        // Load MRFCs from API
        lifecycleScope.launch {
            try {
                val mrfcApiService = retrofit.create(com.mgb.mrfcmanager.data.remote.api.MrfcApiService::class.java)
                val response = mrfcApiService.getAllMrfcs(page = 1, limit = 100)
                val apiResponse = response.body()
                if (response.isSuccessful && apiResponse?.success == true) {
                    mrfcList.clear()
                    apiResponse.data?.mrfcs?.forEach { mrfc ->
                        mrfcList.add(mrfc.id to mrfc.name)
                    }

                    val mrfcAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        mrfcList.map { it.second }
                    )
                    spinnerMrfc.setAdapter(mrfcAdapter)
                }
            } catch (e: Exception) {
                // Silently fail - dropdowns will remain empty
            }
        }

        // Load Proponents from API
        lifecycleScope.launch {
            try {
                val proponentApiService = retrofit.create(com.mgb.mrfcmanager.data.remote.api.ProponentApiService::class.java)
                val response = proponentApiService.getAllProponents(page = 1, limit = 100, mrfcId = mrfcId)
                val apiResponse = response.body()
                if (response.isSuccessful && apiResponse?.success == true) {
                    proponentList.clear()
                    apiResponse.data?.proponents?.forEach { proponent ->
                        proponentList.add(proponent.id to proponent.name)
                    }

                    val proponentAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        proponentList.map { it.second }
                    )
                    spinnerProponent.setAdapter(proponentAdapter)
                }
            } catch (e: Exception) {
                // Silently fail - dropdowns will remain empty
            }
        }
    }

    private fun loadProponentsForMrfc(mrfcId: Long?, spinnerProponent: AutoCompleteTextView) {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)

        lifecycleScope.launch {
            try {
                val proponentApiService = retrofit.create(com.mgb.mrfcmanager.data.remote.api.ProponentApiService::class.java)
                // Filter proponents by the selected MRFC
                val response = proponentApiService.getAllProponents(page = 1, limit = 100, mrfcId = mrfcId)
                val apiResponse = response.body()
                if (response.isSuccessful && apiResponse?.success == true) {
                    proponentList.clear()
                    apiResponse.data?.proponents?.forEach { proponent ->
                        proponentList.add(proponent.id to proponent.name)
                    }

                    val proponentAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        proponentList.map { it.second }
                    )
                    spinnerProponent.setAdapter(proponentAdapter)
                }
            } catch (e: Exception) {
                // Silently fail - dropdown will remain empty
            }
        }
    }

    private fun addAgendaItem(
        title: String,
        description: String,
        mrfcId: Long? = null,
        proponentId: Long? = null,
        fileCategory: String? = null
    ) {
        val orderIndex = agendaItems.size

        // Check user role for appropriate success message
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        val isAdmin = userRole == "ADMIN" || userRole == "SUPER_ADMIN"

        viewModel.createItem(
            agendaId = agendaId,
            title = title,
            description = description,
            orderIndex = orderIndex,
            mrfcId = mrfcId,
            proponentId = proponentId,
            fileCategory = fileCategory
        ) { result ->
            when (result) {
                is Result.Success -> {
                    val message = if (isAdmin) {
                        "✅ Agenda item added successfully!"
                    } else {
                        "✅ Proposal Submitted Successfully!\n\nYour proposal is pending admin review. Check the Proposals tab to track its status."
                    }

                    // Use Snackbar for better visibility and multi-line support
                    view?.let {
                        Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                            .setAction("OK") { /* Dismiss */ }
                            .setTextMaxLines(5)
                            .show()
                    }
                }
                is Result.Error -> {
                    showError("Failed to add item: ${result.message}")
                }
                is Result.Loading -> {
                    // Loading state
                }
            }
        }
    }

    private fun showAgendaItemDetailDialog(item: AgendaItemDto) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_agenda_item_detail, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Get views
        val tvItemTitle = dialogView.findViewById<TextView>(R.id.tvItemTitle)
        val tvItemDescription = dialogView.findViewById<TextView>(R.id.tvItemDescription)
        val cardDescription = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardDescription)
        val cardRelatedInfo = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRelatedInfo)
        val layoutMrfc = dialogView.findViewById<LinearLayout>(R.id.layoutMrfc)
        val layoutProponent = dialogView.findViewById<LinearLayout>(R.id.layoutProponent)
        val layoutFileCategory = dialogView.findViewById<LinearLayout>(R.id.layoutFileCategory)
        val tvMrfcId = dialogView.findViewById<TextView>(R.id.tvMrfcId)
        val tvProponentId = dialogView.findViewById<TextView>(R.id.tvProponentId)
        val tvFileCategory = dialogView.findViewById<TextView>(R.id.tvFileCategory)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvStatus)
        val tvHighlightStatus = dialogView.findViewById<TextView>(R.id.tvHighlightStatus)
        val btnToggleHighlight = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnToggleHighlight)
        val btnViewRelatedFiles = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewRelatedFiles)
        val layoutAdminActions = dialogView.findViewById<LinearLayout>(R.id.layoutAdminActions)
        val btnEdit = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEdit)
        val btnDelete = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDelete)
        val btnClose = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClose)

        // Set data
        tvItemTitle.text = item.title
        tvItemDescription.text = item.description
        cardDescription.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE

        // Set status badge
        tvStatus.text = item.status
        when (item.status) {
            "APPROVED" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_rounded_green)
                tvStatus.setTextColor(requireContext().getColor(android.R.color.white))
            }
            "PROPOSED" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_rounded_orange)
                tvStatus.setTextColor(requireContext().getColor(android.R.color.white))
            }
            "DENIED" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_rounded_red)
                tvStatus.setTextColor(requireContext().getColor(android.R.color.white))
            }
        }

        // Show related info if available
        var hasRelatedInfo = false
        if (item.mrfcId != null) {
            layoutMrfc.visibility = View.VISIBLE
            // Look up MRFC name from list
            val mrfcName = mrfcList.find { it.first == item.mrfcId }?.second ?: "MRFC #${item.mrfcId}"
            tvMrfcId.text = mrfcName
            hasRelatedInfo = true
        }
        if (item.proponentId != null) {
            layoutProponent.visibility = View.VISIBLE
            // Look up Proponent name from list
            val proponentName = proponentList.find { it.first == item.proponentId }?.second ?: "Proponent #${item.proponentId}"
            tvProponentId.text = proponentName
            hasRelatedInfo = true
        }
        if (!item.fileCategory.isNullOrEmpty()) {
            layoutFileCategory.visibility = View.VISIBLE
            tvFileCategory.text = item.fileCategory
            hasRelatedInfo = true

            // Show "View Related Files" button
            btnViewRelatedFiles.visibility = View.VISIBLE
            btnViewRelatedFiles.text = "View ${item.fileCategory} Files"
        }
        cardRelatedInfo.visibility = if (hasRelatedInfo) View.VISIBLE else View.GONE

        // View Related Files button click
        btnViewRelatedFiles.setOnClickListener {
            dialog.dismiss()
            navigateToFileCategoryPage(item.fileCategory, item.proponentId ?: 0)
        }

        // Show highlight status
        if (item.isHighlighted) {
            tvHighlightStatus.visibility = View.VISIBLE
            tvHighlightStatus.text = "Discussed"
            tvHighlightStatus.setBackgroundResource(R.drawable.bg_rounded_green)
        } else {
            tvHighlightStatus.visibility = View.GONE
        }

        // Show admin actions if user is admin
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        if (userRole == "ADMIN" || userRole == "SUPER_ADMIN") {
            layoutAdminActions.visibility = View.VISIBLE

            // Toggle Highlight button
            btnToggleHighlight.visibility = View.VISIBLE
            btnToggleHighlight.text = if (item.isHighlighted) "Mark as Pending" else "Mark as Discussed"
            btnToggleHighlight.setOnClickListener {
                dialog.dismiss()
                toggleHighlight(item)
            }

            // Edit button
            btnEdit.setOnClickListener {
                dialog.dismiss()
                showEditAgendaItemDialog(item)
            }

            // Delete button
            btnDelete.setOnClickListener {
                dialog.dismiss()
                showDeleteConfirmation(item)
            }
        }

        // Close button
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun toggleHighlight(item: AgendaItemDto) {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val apiService = retrofit.create(AgendaItemApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = apiService.toggleHighlight(item.id)
                if (response.isSuccessful && response.body()?.success == true) {
                    val message = if (item.isHighlighted) "Marked as pending" else "Marked as discussed"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    loadAgendaItems() // Refresh the list
                } else {
                    Toast.makeText(requireContext(), "Failed to update highlight status", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToFileCategoryPage(category: String?, proponentId: Long) {
        if (category.isNullOrEmpty() || proponentId == 0L) {
            Toast.makeText(requireContext(), "Proponent information not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Navigate to DocumentListActivity with category filter
        val intent = Intent(requireContext(), com.mgb.mrfcmanager.ui.admin.DocumentListActivity::class.java).apply {
            putExtra("PROPONENT_ID", proponentId)
            putExtra("CATEGORY", category) // Fixed: Use "CATEGORY" to match DocumentListActivity constant
        }
        startActivity(intent)
    }

    private fun showEditAgendaItemDialog(item: AgendaItemDto) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_agenda_item, null)

        val spinnerMrfc = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerMrfc)
        val spinnerProponent = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerProponent)
        val spinnerFileCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerFileCategory)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        // Pre-fill existing values
        etTitle.setText(item.title)
        etDescription.setText(item.description ?: "")

        // Populate File Category dropdown
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            fileCategoryList.map { it.second }
        )
        spinnerFileCategory.setAdapter(categoryAdapter)

        // Pre-select file category if exists
        if (!item.fileCategory.isNullOrEmpty()) {
            val categoryIndex = fileCategoryList.indexOfFirst { it.first == item.fileCategory }
            if (categoryIndex >= 0) {
                spinnerFileCategory.setText(fileCategoryList[categoryIndex].second, false)
            }
        }

        // Load MRFCs and Proponents for dropdowns
        loadMRFCsAndProponents(spinnerMrfc, spinnerProponent)

        // Track selected values (initialize with current values)
        var selectedMrfcId: Long? = item.mrfcId
        var selectedProponentId: Long? = item.proponentId
        var selectedFileCategory: String? = item.fileCategory

        spinnerMrfc.setOnItemClickListener { _, _, position, _ ->
            selectedMrfcId = mrfcList[position].first

            // Reload proponents filtered by the selected MRFC
            selectedProponentId = null
            spinnerProponent.text.clear()
            loadProponentsForMrfc(selectedMrfcId, spinnerProponent)
        }

        spinnerProponent.setOnItemClickListener { _, _, position, _ ->
            selectedProponentId = proponentList[position].first
        }

        spinnerFileCategory.setOnItemClickListener { _, _, position, _ ->
            selectedFileCategory = fileCategoryList[position].first
        }

        // Pre-select MRFC and Proponent after data is loaded
        lifecycleScope.launch {
            // Wait a bit for data to load
            kotlinx.coroutines.delay(500)

            // Pre-select MRFC
            if (item.mrfcId != null) {
                val mrfcIndex = mrfcList.indexOfFirst { it.first == item.mrfcId }
                if (mrfcIndex >= 0) {
                    spinnerMrfc.setText(mrfcList[mrfcIndex].second, false)
                }
            }

            // Pre-select Proponent
            if (item.proponentId != null) {
                val proponentIndex = proponentList.indexOfFirst { it.first == item.proponentId }
                if (proponentIndex >= 0) {
                    spinnerProponent.setText(proponentList[proponentIndex].second, false)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Agenda Item")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    updateAgendaItem(
                        item = item,
                        title = title,
                        description = description,
                        mrfcId = selectedMrfcId,
                        proponentId = selectedProponentId,
                        fileCategory = selectedFileCategory
                    )
                } else {
                    Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateAgendaItem(
        item: AgendaItemDto,
        title: String,
        description: String,
        mrfcId: Long?,
        proponentId: Long?,
        fileCategory: String?
    ) {
        viewModel.updateItem(
            id = item.id,
            agendaId = agendaId,
            title = title,
            description = description.ifEmpty { null },
            mrfcId = mrfcId,
            proponentId = proponentId,
            fileCategory = fileCategory
        ) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Agenda item updated successfully", Toast.LENGTH_SHORT).show()
                    loadAgendaItems()
                }
                is Result.Error -> {
                    showError("Failed to update item: ${result.message}")
                }
                is Result.Loading -> {
                    // Loading state
                }
            }
        }
    }

    private fun showDeleteConfirmation(item: AgendaItemDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Agenda Item")
            .setMessage("Are you sure you want to delete \"${item.title}\"?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAgendaItem(item)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAgendaItem(item: AgendaItemDto) {
        viewModel.deleteItem(item.id, agendaId) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Agenda item deleted successfully", Toast.LENGTH_SHORT).show()
                    loadAgendaItems()
                }
                is Result.Error -> {
                    showError("Failed to delete item: ${result.message}")
                }
                is Result.Loading -> {
                    // Loading state
                }
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
}

/**
 * Read-only adapter for the Agenda tab's "Other Matters" section.
 * Shows only approved other matters items (no approval controls here).
 */
class OtherMattersInAgendaAdapter(
    private val items: List<AgendaItemDto>
) : RecyclerView.Adapter<OtherMattersInAgendaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_other_matter_in_agenda, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemNumber: TextView = itemView.findViewById(R.id.tvItemNumber)
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val tvItemDescription: TextView = itemView.findViewById(R.id.tvItemDescription)
        private val tvDiscussedBadge: TextView = itemView.findViewById(R.id.tvDiscussedBadge)

        fun bind(item: AgendaItemDto, number: Int) {
            tvItemNumber.text = number.toString()
            tvItemTitle.text = item.title
            tvItemDescription.text = item.description ?: "No description"
            tvItemDescription.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE

            if (item.isHighlighted) {
                tvDiscussedBadge.visibility = View.VISIBLE
            } else {
                tvDiscussedBadge.visibility = View.GONE
            }
        }
    }
}

/**
 * Adapter for displaying agenda items
 * Shows items with highlight status (green background for discussed items)
 */
class AgendaItemAdapter(
    private val items: List<AgendaItemDto>,
    private val onItemClick: (AgendaItemDto) -> Unit
) : RecyclerView.Adapter<AgendaItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position + 1, onItemClick)
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardItem: com.google.android.material.card.MaterialCardView =
            itemView.findViewById(R.id.cardItem)
        private val tvItemNumber: TextView = itemView.findViewById(R.id.tvItemNumber)
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val tvItemDescription: TextView = itemView.findViewById(R.id.tvItemDescription)
        private val ivHighlightIndicator: ImageView = itemView.findViewById(R.id.ivHighlightIndicator)

        fun bind(item: AgendaItemDto, number: Int, onItemClick: (AgendaItemDto) -> Unit) {
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

            // Set click listener on the whole item
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
