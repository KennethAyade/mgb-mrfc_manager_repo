package com.mgb.mrfcmanager.data.model

/**
 * Document data model
 * TODO: BACKEND - Add Room @Entity annotation when implementing database
 * TODO: BACKEND - Add fileUri field when implementing file storage
 */
data class Document(
    val id: Long,
    val fileName: String,
    val fileType: String,
    val category: String,
    val uploadDate: String
)
