package com.mgb.mrfcmanager.ui.meeting.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

/**
 * Agenda Fragment
 * Shows list of agenda items (discussion topics)
 * ALL users can view and add agenda items
 */
class AgendaFragment : Fragment() {

    private lateinit var rvAgendaItems: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: AgendaItemViewModel
    private lateinit var agendaItemAdapter: AgendaItemAdapter

    private val agendaItems = mutableListOf<AgendaItemDto>()
    private var agendaId: Long = 0L
    private var mrfcId: Long = 0L

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

    companion object {
        private const val ARG_AGENDA_ID = "agenda_id"
        private const val ARG_MRFC_ID = "mrfc_id"

        fun newInstance(agendaId: Long, mrfcId: Long): AgendaFragment {
            return AgendaFragment().apply {
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
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        loadAgendaItems()
        loadMRFCsAndProponentsData() // Load MRFC and Proponent names for detail dialog

        fabAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when fragment becomes visible
        // This ensures data is fresh when switching tabs
        loadAgendaItems()
    }

    private fun initializeViews(view: View) {
        rvAgendaItems = view.findViewById(R.id.rvAgendaItems)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        fabAddItem = view.findViewById(R.id.fabAddItem)
        progressBar = view.findViewById(R.id.progressBar)
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
        agendaItemAdapter = AgendaItemAdapter(agendaItems) { item ->
            showAgendaItemDetailDialog(item)
        }
        rvAgendaItems.layoutManager = LinearLayoutManager(requireContext())
        rvAgendaItems.adapter = agendaItemAdapter
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

    private fun loadAgendaItems() {
        viewModel.loadItemsByAgenda(agendaId)
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

        // Show admin actions if user is admin
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        if (userRole == "ADMIN" || userRole == "SUPER_ADMIN") {
            layoutAdminActions.visibility = View.VISIBLE

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

    private fun navigateToFileCategoryPage(category: String?, proponentId: Long) {
        if (category.isNullOrEmpty() || proponentId == 0L) {
            Toast.makeText(requireContext(), "Proponent information not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Navigate to DocumentListActivity with category filter
        val intent = Intent(requireContext(), com.mgb.mrfcmanager.ui.admin.DocumentListActivity::class.java).apply {
            putExtra("PROPONENT_ID", proponentId)
            putExtra("CATEGORY_FILTER", category)
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
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}

/**
 * Adapter for displaying agenda items
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
        private val tvItemNumber: TextView = itemView.findViewById(R.id.tvItemNumber)
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val tvItemDescription: TextView = itemView.findViewById(R.id.tvItemDescription)

        fun bind(item: AgendaItemDto, number: Int, onItemClick: (AgendaItemDto) -> Unit) {
            tvItemNumber.text = "$number."
            tvItemTitle.text = item.title
            tvItemDescription.text = item.description ?: "No description"
            tvItemDescription.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Set click listener on the whole item
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
