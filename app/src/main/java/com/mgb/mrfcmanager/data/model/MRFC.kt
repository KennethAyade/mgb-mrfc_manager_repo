package com.mgb.mrfcmanager.data.model

/**
 * MRFC (Municipal Resource and Finance Committee) data model
 * TODO: BACKEND - Add Room @Entity annotation when implementing database
 */
data class MRFC(
    val id: Long,
    val name: String,
    val municipality: String,
    val contactPerson: String,
    val contactNumber: String
)
