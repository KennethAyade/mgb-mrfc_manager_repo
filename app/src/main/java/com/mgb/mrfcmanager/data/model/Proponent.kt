package com.mgb.mrfcmanager.data.model

/**
 * Proponent data model
 * TODO: BACKEND - Add Room @Entity annotation when implementing database
 */
data class Proponent(
    val id: Long,
    val mrfcId: Long,
    val name: String,
    val companyName: String,
    val status: String // "Active", "Inactive"
)
