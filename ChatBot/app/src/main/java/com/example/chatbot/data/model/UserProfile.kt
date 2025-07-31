package com.example.chatbot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing the user's personal profile information
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single profile per app
    val name: String = "",
    val birthday: String = "",
    val occupation: String = "",
    val hobbies: String = "",
    val preferences: String = "", // JSON string for additional preferences
    val lastUpdated: Long = System.currentTimeMillis()
)
