package com.mgb.mrfcmanager.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.ComplianceApiService
import com.mgb.mrfcmanager.data.remote.dto.ComplianceDto
import com.mgb.mrfcmanager.data.remote.dto.ComplianceSummaryDto
import com.mgb.mrfcmanager.data.repository.ComplianceRepository
import com.mgb.mrfcmanager.viewmodel.ComplianceListState
import com.mgb.mrfcmanager.viewmodel.ComplianceSummaryState
import com.mgb.mrfcmanager.viewmodel.ComplianceViewModel
import com.mgb.mrfcmanager.viewmodel.ComplianceViewModelFactory
import kotlin.random.Random

class ComplianceDashboardActivity : AppCompatActivity() {

    private lateinit var pieChartCompliance: PieChart
    private lateinit var tvCompliantCount: TextView
    private lateinit var tvPartialCount: TextView
    private lateinit var tvNonCompliantCount: TextView
    private lateinit var rvComplianceList: RecyclerView
    private lateinit var btnGenerateReport: MaterialButton
    private lateinit var btnExportData: MaterialButton
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private val complianceData = mutableListOf<ComplianceDto>()
    private lateinit var complianceAdapter: ComplianceAdapter

    private lateinit var viewModel: ComplianceViewModel
    private var mrfcId: Long = 0L
    private var currentQuarter: String? = null
    private var currentYear: Int? = null

    // Unused local models - app uses backend DTOs
    /*
    data class ComplianceItem(
        val proponent: Proponent,
        val compliancePercentage: Int,
        val status: ComplianceStatus
    )

    enum class ComplianceStatus {
        COMPLIANT,
        PARTIAL,
        NON_COMPLIANT
    }
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compliance_dashboard)

        // Get intent extras
        mrfcId = intent.getLongExtra("MRFC_ID", 0L)
        currentQuarter = intent.getStringExtra("QUARTER")
        currentYear = intent.getIntExtra("YEAR", 0).takeIf { it != 0 }

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        setupSwipeRefresh()
        observeViewModelStates()
        loadComplianceData()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        pieChartCompliance = findViewById(R.id.pieChartCompliance)
        tvCompliantCount = findViewById(R.id.tvCompliantCount)
        tvPartialCount = findViewById(R.id.tvPartialCount)
        tvNonCompliantCount = findViewById(R.id.tvNonCompliantCount)
        rvComplianceList = findViewById(R.id.rvComplianceList)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        btnExportData = findViewById(R.id.btnExportData)
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val complianceApiService = retrofit.create(ComplianceApiService::class.java)
        val complianceRepository = ComplianceRepository(complianceApiService)
        val factory = ComplianceViewModelFactory(complianceRepository)
        viewModel = ViewModelProvider(this, factory)[ComplianceViewModel::class.java]
    }

    private fun setupSwipeRefresh() {
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.setOnRefreshListener {
        //     loadComplianceData()
        // }
    }

    private fun observeViewModelStates() {
        // Observe compliance list state
        viewModel.complianceListState.observe(this) { state ->
            when (state) {
                is ComplianceListState.Loading -> showLoading(true)
                is ComplianceListState.Success -> {
                    showLoading(false)
                    complianceData.clear()
                    complianceData.addAll(state.data)
                    complianceAdapter.notifyDataSetChanged()
                }
                is ComplianceListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is ComplianceListState.Idle -> showLoading(false)
            }
        }

        // Observe compliance summary state
        viewModel.complianceSummaryState.observe(this) { state ->
            when (state) {
                is ComplianceSummaryState.Loading -> showLoading(true)
                is ComplianceSummaryState.Success -> {
                    showLoading(false)
                    updateDashboardFromSummary(state.data)
                }
                is ComplianceSummaryState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is ComplianceSummaryState.Idle -> showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.isRefreshing = false
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupRecyclerView() {
        complianceAdapter = ComplianceAdapter(complianceData)
        rvComplianceList.layoutManager = LinearLayoutManager(this)
        rvComplianceList.adapter = complianceAdapter
    }

    private fun setupClickListeners() {
        btnGenerateReport.setOnClickListener {
            // TODO: BACKEND - Generate compliance report PDF
            Toast.makeText(this, "Generating compliance report...", Toast.LENGTH_SHORT).show()
        }

        btnExportData.setOnClickListener {
            // TODO: BACKEND - Export data to Excel/CSV
            Toast.makeText(this, "Exporting compliance data...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadComplianceData() {
        if (mrfcId == 0L) {
            showError("MRFC ID is required")
            return
        }

        // Load compliance records and summary
        viewModel.loadCompliance(mrfcId = mrfcId, quarter = currentQuarter, year = currentYear)
        viewModel.loadComplianceSummary(mrfcId = mrfcId, quarter = currentQuarter, year = currentYear)
    }

    private fun updateDashboardFromSummary(summary: ComplianceSummaryDto) {
        // Update summary cards
        tvCompliantCount.text = summary.compliant.toString()
        tvPartialCount.text = summary.partial.toString()
        tvNonCompliantCount.text = summary.nonCompliant.toString()

        // Setup pie chart
        setupPieChart(summary.compliant, summary.partial, summary.nonCompliant)
    }

    private fun updateDashboard() {
        // Calculate overall statistics from loaded compliance data
        val totalProponents = complianceData.size
        val compliantCount = complianceData.count { it.status.equals("COMPLIANT", ignoreCase = true) }
        val partialCount = complianceData.count { it.status.equals("PARTIAL", ignoreCase = true) }
        val nonCompliantCount = complianceData.count { it.status.equals("NON_COMPLIANT", ignoreCase = true) }

        // Update summary cards
        tvCompliantCount.text = compliantCount.toString()
        tvPartialCount.text = partialCount.toString()
        tvNonCompliantCount.text = nonCompliantCount.toString()

        // Setup pie chart
        setupPieChart(compliantCount, partialCount, nonCompliantCount)
    }

    private fun setupPieChart(compliant: Int, partial: Int, nonCompliant: Int) {
        val entries = ArrayList<PieEntry>()

        if (compliant > 0) entries.add(PieEntry(compliant.toFloat(), "Compliant"))
        if (partial > 0) entries.add(PieEntry(partial.toFloat(), "Partial"))
        if (nonCompliant > 0) entries.add(PieEntry(nonCompliant.toFloat(), "Non-Compliant"))

        val dataSet = PieDataSet(entries, "")

        // Set colors
        val colors = ArrayList<Int>()
        if (compliant > 0) colors.add(ContextCompat.getColor(this, R.color.status_compliant))
        if (partial > 0) colors.add(ContextCompat.getColor(this, R.color.status_partial))
        if (nonCompliant > 0) colors.add(ContextCompat.getColor(this, R.color.status_non_compliant))
        dataSet.colors = colors

        // Styling
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartCompliance))

        // Configure chart
        pieChartCompliance.data = data
        pieChartCompliance.description.isEnabled = false
        pieChartCompliance.isDrawHoleEnabled = true
        pieChartCompliance.setHoleColor(Color.TRANSPARENT)
        pieChartCompliance.holeRadius = 58f
        pieChartCompliance.transparentCircleRadius = 61f
        pieChartCompliance.setDrawCenterText(true)

        val total = compliant + partial + nonCompliant
        // Weighted compliance: full=1.0, partial=0.5, non=0.0
        val complianceRate = if (total > 0) (((compliant * 1.0) + (partial * 0.5)) / total * 100).toInt() else 0
        pieChartCompliance.centerText = "$complianceRate%\nCompliance"
        pieChartCompliance.setCenterTextSize(18f)
        pieChartCompliance.setCenterTextColor(Color.WHITE)

        pieChartCompliance.legend.isEnabled = true
        pieChartCompliance.legend.textColor = Color.WHITE
        pieChartCompliance.legend.textSize = 12f

        pieChartCompliance.setEntryLabelColor(Color.WHITE)
        pieChartCompliance.setEntryLabelTextSize(10f)

        pieChartCompliance.animateY(1000)
        pieChartCompliance.invalidate()
    }

    // Adapter for compliance list
    class ComplianceAdapter(
        private val complianceList: List<ComplianceDto>
    ) : RecyclerView.Adapter<ComplianceAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_compliance, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(complianceList[position])
        }

        override fun getItemCount() = complianceList.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvProponentName: TextView = itemView.findViewById(R.id.tvProponentName)
            private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
            private val cardStatus: MaterialCardView = itemView.findViewById(R.id.cardStatus)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            private val progressCompliance: ProgressBar = itemView.findViewById(R.id.progressCompliance)
            private val tvPercentage: TextView = itemView.findViewById(R.id.tvPercentage)

            fun bind(compliance: ComplianceDto) {
                // Display compliance type and quarter
                tvProponentName.text = compliance.complianceType
                tvCompanyName.text = "${compliance.quarter} ${compliance.year}"

                // Calculate percentage from score (assuming score is out of 100)
                val percentage = compliance.score?.toInt() ?: 0
                progressCompliance.progress = percentage
                tvPercentage.text = "$percentage%"

                // Set status badge based on compliance status
                val (statusText, statusColor) = when (compliance.status.uppercase()) {
                    "COMPLIANT" -> "Compliant" to R.color.status_compliant
                    "PARTIAL" -> "Partial" to R.color.status_partial
                    "NON_COMPLIANT" -> "Non-Compliant" to R.color.status_non_compliant
                    else -> compliance.status to R.color.status_pending
                }

                tvStatus.text = statusText
                cardStatus.setCardBackgroundColor(itemView.context.getColor(statusColor))
                progressCompliance.progressTintList = itemView.context.getColorStateList(statusColor)
            }
        }
    }
}
