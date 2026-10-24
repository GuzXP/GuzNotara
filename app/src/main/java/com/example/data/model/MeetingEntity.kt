package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0L,
    val audioFilePath: String = "",
    val status: String = "RECORDED", // RECORDED, TRANSCRIBING, COMPLETED, ERROR
    val language: String = "id-ID",
    val notes: String = ""
)
