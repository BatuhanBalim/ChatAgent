package com.example.chatbot.data.model

import java.util.Date
import java.util.UUID

/**
 * Represents a chat message in the application
 */
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUserMessage: Boolean,
    val timestamp: Date = Date()
)
