package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "document_sections",
    foreignKeys = [
        ForeignKey(
            entity = MeetingEntity::class,
            parentColumns = ["id"],
            childColumns = ["meetingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("meetingId")]
)
data class DocumentSectionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val meetingId: String,
    val title: String,
    val content: String,
    val sectionOrder: Int,
    val sectionType: String = "DISCUSSION" // INFO, OVERVIEW, DISCUSSION, QA, CLARIFICATION, ACTION_ITEMS
)
