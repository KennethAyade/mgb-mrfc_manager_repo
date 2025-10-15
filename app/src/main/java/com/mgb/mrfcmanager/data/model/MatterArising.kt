package com.mgb.mrfcmanager.data.model

/**
 * Matter Arising data model
 * Represents unresolved issues from previous meetings requiring follow-up
 * TODO: BACKEND - Add Room @Entity annotation when implementing database
 */
data class MatterArising(
    val id: Long,
    val agendaId: Long,
    val issue: String,
    val status: String, // "Resolved", "Pending", "In Progress"
    val assignedTo: String,
    val dateRaised: String,
    val remarks: String = ""
)
