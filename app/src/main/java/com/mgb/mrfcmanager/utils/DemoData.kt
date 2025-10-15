package com.mgb.mrfcmanager.utils

import com.mgb.mrfcmanager.data.model.*

/**
 * Demo Data for UI-only implementation
 * TODO: BACKEND - Remove this class when backend is integrated
 */
object DemoData {

    // Users
    val adminUser = User(1, "admin", "Admin User", "Admin")
    val regularUser = User(2, "user1", "John Doe", "User")

    // MRFCs
    val mrfcList = listOf(
        MRFC(1, "MRFC - 1", "Dingras", "Juan Dela Cruz", "0917-123-4567"),
        MRFC(2, "MRFC - 2", "Laoag", "Maria Santos", "0918-234-5678"),
        MRFC(3, "MRFC - 3", "Batac", "Pedro Reyes", "0919-345-6789"),
        MRFC(4, "MRFC - 4", "Bangui", "Ana Garcia", "0920-456-7890"),
        MRFC(5, "MRFC - 5", "Pagudpud", "Carlos Lopez", "0921-567-8901")
    )

    // Proponents
    val proponentList = listOf(
        Proponent(1, 1, "Proponent 1", "El Pentad Mining Corporation", "Active"),
        Proponent(2, 1, "Proponent 2", "Galeon Sand and Gravel Quarrying", "Active"),
        Proponent(3, 1, "Proponent 3", "Northern Mining Inc.", "Active"),
        Proponent(4, 1, "Proponent 4", "Pacific Resources Corp.", "Inactive"),
        Proponent(5, 1, "Proponent 5", "Ilocos Mining Company", "Active"),
        Proponent(6, 1, "Proponent 6", "Sunrise Quarry Operations", "Active"),
        Proponent(7, 1, "Proponent 7", "Mountain Peak Minerals", "Active"),
        Proponent(8, 1, "Proponent 8", "Valley Stone Extraction", "Active"),
        Proponent(9, 1, "Proponent 9", "Coastal Aggregates Ltd.", "Active"),
        Proponent(10, 1, "Proponent 10", "Highland Resources Group", "Active")
    )

    // Standard Agenda Items
    val standardAgendaItems = listOf(
        AgendaItem(1, "Approval of Agenda"),
        AgendaItem(2, "Review and Approval of the Minutes of the Previous Meeting"),
        AgendaItem(3, "Matters Arising from the Minutes of the Previous Meeting"),
        AgendaItem(4, "MTF Disbursement Report"),
        AgendaItem(5, "Presentation of the AEPEP Physical and Financial Status"),
        AgendaItem(6, "Presentation of Quarterly Research Accomplishments"),
        AgendaItem(7, "Presentation and Approval of the Quarterly Compliance Monitoring Validation Report (CMVR)"),
        AgendaItem(8, "Other Matters")
    )

    // Sample Agendas
    fun getAgendaForQuarter(mrfcId: Long, quarter: String): Agenda {
        return Agenda(
            id = mrfcId * 10 + quarter.hashCode().toLong(),
            mrfcId = mrfcId,
            quarter = quarter,
            meetingDate = "2025-03-15",
            location = "Municipal Hall Conference Room",
            items = standardAgendaItems
        )
    }

    // Sample Documents
    val documentList = listOf(
        Document(1, "MTF_Report_Q1_2025.pdf", "PDF", "MTF Disbursement", "2025-01-15", "1st Quarter 2025"),
        Document(2, "AEPEP_Physical_Q1.xlsx", "Excel", "AEPEP Physical", "2025-01-20", "1st Quarter 2025"),
        Document(3, "Research_Accomplishments.docx", "Word", "Research", "2025-02-01", "1st Quarter 2025"),
        Document(4, "CMVR_Report.pdf", "PDF", "CMVR", "2025-02-10", "1st Quarter 2025"),
        Document(5, "Minutes_Q4_2024.pdf", "PDF", "Minutes", "2024-12-15", "4th Quarter 2024"),
        Document(6, "MTF_Report_Q2_2025.pdf", "PDF", "MTF Disbursement", "2025-04-15", "2nd Quarter 2025"),
        Document(7, "AEPEP_Financial_Q2.xlsx", "Excel", "AEPEP Financial", "2025-05-10", "2nd Quarter 2025"),
        Document(8, "Compliance_Report_Q2.pdf", "PDF", "Compliance", "2025-06-01", "2nd Quarter 2025")
    )

    // Matters Arising
    val mattersArisingList = listOf(
        MatterArising(
            1, 1,
            "Review of Environmental Compliance Certificate (ECC) renewal process",
            "In Progress",
            "Environmental Team",
            "2024-12-15",
            "Awaiting DENR approval"
        ),
        MatterArising(
            2, 1,
            "Submission of updated Social Development Management Plan (SDMP)",
            "Pending",
            "Community Relations Officer",
            "2024-12-15",
            "Due by end of quarter"
        ),
        MatterArising(
            3, 1,
            "Follow-up on MTF fund utilization report from previous quarter",
            "Resolved",
            "Finance Department",
            "2024-09-20",
            "Report submitted and approved"
        )
    )

    // Quarters
    val quarters = listOf(
        "1st Quarter 2025",
        "2nd Quarter 2025",
        "3rd Quarter 2025",
        "4th Quarter 2025"
    )

    // Helper function to get proponents by MRFC ID
    fun getProponentsByMrfcId(mrfcId: Long): List<Proponent> {
        return proponentList.filter { it.mrfcId == mrfcId }
    }

    // Helper function to get documents by category
    fun getDocumentsByCategory(category: String): List<Document> {
        return documentList.filter { it.category == category }
    }
}
