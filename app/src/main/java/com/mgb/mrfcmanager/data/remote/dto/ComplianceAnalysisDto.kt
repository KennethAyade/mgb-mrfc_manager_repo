package com.mgb.mrfcmanager.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * CMVR Compliance Analysis Result
 * Contains auto-calculated compliance data from PDF analysis
 */
@JsonClass(generateAdapter = true)
data class ComplianceAnalysisDto(
    @Json(name = "id")
    val id: Long? = null,

    @Json(name = "document_id")
    val documentId: Long,

    @Json(name = "document_name")
    val documentName: String,

    @Json(name = "analysis_status")
    val analysisStatus: String, // "PENDING", "COMPLETED", "FAILED"

    @Json(name = "compliance_percentage")
    val compliancePercentage: Double?,

    @Json(name = "compliance_rating")
    val complianceRating: String?, // "FULLY_COMPLIANT", "PARTIALLY_COMPLIANT", "NON_COMPLIANT"

    @Json(name = "total_items")
    val totalItems: Int?,

    @Json(name = "compliant_items")
    val compliantItems: Int?,

    @Json(name = "non_compliant_items")
    val nonCompliantItems: Int?,

    @Json(name = "na_items")
    val naItems: Int?,

    @Json(name = "applicable_items")
    val applicableItems: Int?, // total_items - na_items

    @Json(name = "compliance_details")
    val complianceDetails: ComplianceDetailsDto?,

    @Json(name = "non_compliant_list")
    val nonCompliantList: List<NonCompliantItemDto>?,

    @Json(name = "admin_notes")
    val adminNotes: String?,

    @Json(name = "admin_adjusted")
    val adminAdjusted: Boolean = false,

    @Json(name = "analyzed_at")
    val analyzedAt: String?,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null,

    @Json(name = "reviewed_at")
    val reviewedAt: String? = null,

    @Json(name = "reviewed_by")
    val reviewedBy: String? = null
)

@JsonClass(generateAdapter = true)
data class ComplianceDetailsDto(
    @Json(name = "ecc_compliance")
    val eccCompliance: ComplianceSectionDto?,

    @Json(name = "epep_compliance")
    val epepCompliance: ComplianceSectionDto?,

    @Json(name = "impact_management")
    val impactManagement: ComplianceSectionDto?,

    @Json(name = "water_quality")
    val waterQuality: ComplianceSectionDto?,

    @Json(name = "air_quality")
    val airQuality: ComplianceSectionDto?,

    @Json(name = "noise_quality")
    val noiseQuality: ComplianceSectionDto?,

    @Json(name = "waste_management")
    val wasteManagement: ComplianceSectionDto?,

    @Json(name = "recommendations")
    val recommendations: RecommendationsSectionDto?
)

@JsonClass(generateAdapter = true)
data class ComplianceSectionDto(
    @Json(name = "section_name")
    val sectionName: String,

    @Json(name = "total")
    val total: Int,

    @Json(name = "compliant")
    val compliant: Int,

    @Json(name = "non_compliant")
    val nonCompliant: Int,

    @Json(name = "na")
    val na: Int,

    @Json(name = "percentage")
    val percentage: Double
)

@JsonClass(generateAdapter = true)
data class RecommendationsSectionDto(
    @Json(name = "total")
    val total: Int,

    @Json(name = "complied")
    val complied: Int,

    @Json(name = "not_complied")
    val notComplied: Int,

    @Json(name = "ongoing")
    val ongoing: Int,

    @Json(name = "percentage")
    val percentage: Double
)

@JsonClass(generateAdapter = true)
data class NonCompliantItemDto(
    @Json(name = "requirement")
    val requirement: String,

    @Json(name = "page_number")
    val pageNumber: Int?,

    @Json(name = "severity")
    val severity: String? = null,

    @Json(name = "notes")
    val notes: String? = null
)

/**
 * Request to trigger compliance analysis
 */
@JsonClass(generateAdapter = true)
data class AnalyzeComplianceRequest(
    @Json(name = "document_id")
    val documentId: Long
)

/**
 * Request to update compliance analysis with admin adjustments
 */
@JsonClass(generateAdapter = true)
data class UpdateComplianceAnalysisRequest(
    @Json(name = "document_id")
    val documentId: Long,

    @Json(name = "compliance_percentage")
    val compliancePercentage: Double?,

    @Json(name = "compliance_rating")
    val complianceRating: String?,

    @Json(name = "admin_notes")
    val adminNotes: String?,

    @Json(name = "admin_adjusted")
    val adminAdjusted: Boolean = true
)

