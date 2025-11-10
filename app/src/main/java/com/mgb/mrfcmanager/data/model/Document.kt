package com.mgb.mrfcmanager.data.model

/**
 * Local Document data model
 * NOTE: This is kept for backward compatibility
 * The app now uses DocumentDto from the backend API
 */
data class Document(
    val id: Long,
    val fileName: String,
    val fileType: String,
    val category: String,
    val uploadDate: String,
    val quarter: String
)

