package com.mgb.mrfcmanager.data.model

/**
 * Agenda data model
 * TODO: BACKEND - Add Room @Entity annotation when implementing database
 * TODO: BACKEND - AgendaItem list will need special handling (TypeConverter or separate table)
 */
data class Agenda(
    val id: Long,
    val mrfcId: Long,
    val quarter: String,
    val meetingDate: String,
    val location: String,
    val items: List<AgendaItem>
)

/**
 * Agenda Item data model
 * TODO: BACKEND - Add Room @Entity annotation when implementing database
 */
data class AgendaItem(
    val id: Long,
    val title: String,
    val description: String = ""
)
