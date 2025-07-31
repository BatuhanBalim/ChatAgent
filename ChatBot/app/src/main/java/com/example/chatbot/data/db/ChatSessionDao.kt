package com.example.chatbot.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chatbot.data.model.ChatSession
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the chat_sessions table
 */
@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllChatSessions(): Flow<List<ChatSession>>
    
    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getChatSessionById(id: String): ChatSession?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(chatSession: ChatSession)
    
    @Update
    suspend fun updateChatSession(chatSession: ChatSession)
    
    @Delete
    suspend fun deleteChatSession(chatSession: ChatSession)
    
    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteChatSessionById(id: String)
}
