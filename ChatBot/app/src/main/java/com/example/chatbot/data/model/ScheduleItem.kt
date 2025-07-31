package com.example.chatbot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Entity representing a scheduled item (reminder, appointment, etc.)
 */
@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val dateTime: Date,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date()
)
