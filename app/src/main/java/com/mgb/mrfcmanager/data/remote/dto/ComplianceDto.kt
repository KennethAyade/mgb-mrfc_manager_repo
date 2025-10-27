package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json

/**
 * Compliance DTO for API responses
 */
data class ComplianceDto(
    @Json(name = "id") val id: Long,
    @Json(name = "mrfc_id") val mrfcId: Long,
    @Json(name = "proponent_id") val proponentId: Long? = null,
    @Json(name = "quarter") val quarter: String,
    @Json(name = "year") val year: Int,
    @Json(name = "compliance_type") val complianceType: String, // MTF, AEPEP_PHYSICAL, AEPEP_FINANCIAL
    @Json(name = "status") val status: String, // COMPLIANT, PARTIAL, NON_COMPLIANT
    @Json(name = "score") val score: Double? = null,
    @Json(name = "remarks") val remarks: String? = null,
    @Json(name = "report_date") val reportDate: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String? = null
)

/**
 * Request for creating compliance record
 */
data class CreateComplianceRequest(
    @Json(name = "mrfc_id") val mrfcId: Long,
    @Json(name = "proponent_id") val proponentId: Long? = null,
    @Json(name = "quarter") val quarter: String,
    @Json(name = "year") val year: Int,
    @Json(name = "compliance_type") val complianceType: String,
    @Json(name = "status") val status: String,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "remarks") val remarks: String? = null,
    @Json(name = "report_date") val reportDate: String
)

/**
 * Compliance summary/dashboard data
 */
data class ComplianceSummaryDto(
    @Json(name = "total_proponents") val totalProponents: Int,
    @Json(name = "compliant") val compliant: Int,
    @Json(name = "partial") val partial: Int,
    @Json(name = "non_compliant") val nonCompliant: Int,
    @Json(name = "compliance_rate") val complianceRate: Double,
    @Json(name = "by_quarter") val byQuarter: Map<String, Int>? = null
)
