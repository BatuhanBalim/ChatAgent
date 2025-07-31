package com.example.chatbot.data.repository

import com.example.chatbot.data.db.ChatSessionDao
import com.example.chatbot.data.model.ChatSession
import com.example.chatbot.data.model.Message
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

/**
 * Repository for handling chat session operations
 */
class ChatSessionRepository(private val chatSessionDao: ChatSessionDao) {
    
    // Get all chat sessions sorted by most recent
    val allChatSessions: Flow<List<ChatSession>> = chatSessionDao.getAllChatSessions()
    
    // Get a specific chat session by ID
    suspend fun getChatSessionById(id: String): ChatSession? {
        return chatSessionDao.getChatSessionById(id)
    }
    
    // Create a new chat session
    suspend fun createChatSession(messages: List<Message>): String {
        if (messages.isEmpty()) return ""
        
        // Generate a title from the first few messages
        val title = generateSessionTitle(messages)
        val sessionId = UUID.randomUUID().toString()
        val now = Date()
        
        val chatSession = ChatSession(
            id = sessionId,
            title = title,
            messages = messages,
            createdAt = now,
            updatedAt = now
        )
        
        chatSessionDao.insertChatSession(chatSession)
        return sessionId
    }
    
    // Update an existing chat session
    suspend fun updateChatSession(sessionId: String, messages: List<Message>) {
        val existingSession = chatSessionDao.getChatSessionById(sessionId)
        
        if (existingSession != null) {
            val updatedSession = existingSession.copy(
                messages = messages,
                updatedAt = Date()
            )
            chatSessionDao.updateChatSession(updatedSession)
        }
    }
    
    // Delete a chat session
    suspend fun deleteChatSession(sessionId: String) {
        chatSessionDao.deleteChatSessionById(sessionId)
    }
    
    // Helper method to generate a title from the first message
    private fun generateSessionTitle(messages: List<Message>): String {
        val firstUserMessage = messages.firstOrNull { it.isUserMessage }
        return if (firstUserMessage != null) {
            // Use the first 30 characters of the first user message as the title
            if (firstUserMessage.content.length > 30) {
                "${firstUserMessage.content.take(30)}..."
            } else {
                firstUserMessage.content
            }
        } else {
            "New Chat ${Date()}"
        }
    }
}
