package com.example.chatbot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.chatbot.data.db.Converters
import java.util.Date

/**
 * Represents a complete chat session that can be saved and loaded from history
 */
@Entity(tableName = "chat_sessions")
@TypeConverters(Converters::class)
data class ChatSession(
    @PrimaryKey
    val id: String,
    val title: String,
    val messages: List<Message>,
    val createdAt: Date,
    val updatedAt: Date
)
