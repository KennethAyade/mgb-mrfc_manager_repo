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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.model.Proponent
import com.mgb.mrfcmanager.utils.DemoData
import kotlin.random.Random

class ComplianceDashboardActivity : AppCompatActivity() {

    private lateinit var pieChartCompliance: PieChart
    private lateinit var tvCompliantCount: TextView
    private lateinit var tvPartialCount: TextView
    private lateinit var tvNonCompliantCount: TextView
    private lateinit var rvComplianceList: RecyclerView
    private lateinit var btnGenerateReport: MaterialButton
    private lateinit var btnExportData: MaterialButton

    private val complianceData = mutableListOf<ComplianceItem>()
    private lateinit var complianceAdapter: ComplianceAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compliance_dashboard)

        setupToolbar()
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadComplianceData()
        updateDashboard()
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
        // Generate demo compliance data for each proponent
        // TODO: BACKEND - Fetch actual compliance data from database

        DemoData.proponentList.forEach { proponent ->
            val percentage = Random.nextInt(40, 101) // Random percentage between 40-100
            val status = when {
                percentage >= 80 -> ComplianceStatus.COMPLIANT
                percentage >= 60 -> ComplianceStatus.PARTIAL
                else -> ComplianceStatus.NON_COMPLIANT
            }
            complianceData.add(ComplianceItem(proponent, percentage, status))
        }

        complianceAdapter.notifyDataSetChanged()
    }

    private fun updateDashboard() {
        // Calculate overall statistics
        val totalProponents = complianceData.size
        val compliantCount = complianceData.count { it.status == ComplianceStatus.COMPLIANT }
        val partialCount = complianceData.count { it.status == ComplianceStatus.PARTIAL }
        val nonCompliantCount = complianceData.count { it.status == ComplianceStatus.NON_COMPLIANT }

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
        val complianceRate = if (total > 0) (compliant * 100) / total else 0
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
        private val complianceList: List<ComplianceItem>
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

            fun bind(item: ComplianceItem) {
                tvProponentName.text = item.proponent.name
                tvCompanyName.text = item.proponent.companyName
                progressCompliance.progress = item.compliancePercentage
                tvPercentage.text = "${item.compliancePercentage}%"

                // Set status badge
                val (statusText, statusColor) = when (item.status) {
                    ComplianceStatus.COMPLIANT -> "Compliant" to R.color.status_compliant
                    ComplianceStatus.PARTIAL -> "Partial" to R.color.status_partial
                    ComplianceStatus.NON_COMPLIANT -> "Non-Compliant" to R.color.status_non_compliant
                }

                tvStatus.text = statusText
                cardStatus.setCardBackgroundColor(itemView.context.getColor(statusColor))
                progressCompliance.progressTintList = itemView.context.getColorStateList(statusColor)
            }
        }
    }
}
