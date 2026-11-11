package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a Society Notice stored in the local SQLite database.
 */
@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: NoticeCategory = NoticeCategory.MAINTENANCE_REMINDER,
    val content: String,
    val date: String,
    val isUrgent: Boolean = false,
    val issuedBy: String = "Managing Committee",
    val acknowledgedByUsers: String = "" // Comma-separated room numbers
)

typealias NoticeEntity = Notice
