package com.mgb.mrfcmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mgb.mrfcmanager.data.remote.dto.AgendaItemDto

/**
 * Local database entity for Agenda Items
 * Cached for offline viewing within meetings
 */
@Entity(
    tableName = "agenda_items",
    foreignKeys = [
        ForeignKey(
            entity = MeetingEntity::class,
            parentColumns = ["id"],
            childColumns = ["agenda_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["agenda_id"]),
        Index(value = ["is_other_matter"]),
        Index(value = ["is_highlighted"])
    ]
)
data class AgendaItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "agenda_id")
    val agendaId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "added_by")
    val addedBy: Long,

    @ColumnInfo(name = "added_by_name")
    val addedByName: String,

    @ColumnInfo(name = "added_by_username")
    val addedByUsername: String,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "mrfc_id")
    val mrfcId: Long?,

    @ColumnInfo(name = "proponent_id")
    val proponentId: Long?,

    @ColumnInfo(name = "file_category")
    val fileCategory: String?,

    @ColumnInfo(name = "is_other_matter")
    val isOtherMatter: Boolean,

    @ColumnInfo(name = "is_highlighted")
    val isHighlighted: Boolean,

    @ColumnInfo(name = "highlighted_by")
    val highlightedBy: Long?,

    @ColumnInfo(name = "highlighted_at")
    val highlightedAt: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String,

    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
) {
    /**
     * Convert to DTO
     */
    fun toDto(): AgendaItemDto = AgendaItemDto(
        id = id,
        agendaId = agendaId,
        title = title,
        description = description,
        addedBy = addedBy,
        addedByName = addedByName,
        addedByUsername = addedByUsername,
        orderIndex = orderIndex,
        status = status,
        mrfcId = mrfcId,
        proponentId = proponentId,
        fileCategory = fileCategory,
        isOtherMatter = isOtherMatter,
        isHighlighted = isHighlighted,
        highlightedBy = highlightedBy,
        highlightedAt = highlightedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        /**
         * Create entity from DTO
         */
        fun fromDto(dto: AgendaItemDto): AgendaItemEntity {
            return AgendaItemEntity(
                id = dto.id,
                agendaId = dto.agendaId,
                title = dto.title,
                description = dto.description,
                addedBy = dto.addedBy,
                addedByName = dto.addedByName,
                addedByUsername = dto.addedByUsername,
                orderIndex = dto.orderIndex,
                status = dto.status,
                mrfcId = dto.mrfcId,
                proponentId = dto.proponentId,
                fileCategory = dto.fileCategory,
                isOtherMatter = dto.isOtherMatter,
                isHighlighted = dto.isHighlighted,
                highlightedBy = dto.highlightedBy,
                highlightedAt = dto.highlightedAt,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
    }
}
