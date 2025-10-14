package com.mgb.mrfcmanager.data.model

/**
 * User data model
 * TODO: BACKEND - Add Room @Entity annotation when implementing database
 */
data class User(
    val id: Long,
    val username: String,
    val name: String,
    val role: String // "Admin" or "User"
)
