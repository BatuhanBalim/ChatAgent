package com.example.chatbot.ui.screens

import android.content.Context
import android.util.Log
import com.example.chatbot.data.model.ScheduleItem
import java.util.Date
import java.util.UUID

/**
 * Helper class for debugging database issues
 */
object DebugHelper {
    
    /**
     * Creates a sample schedule item for testing
     */
    fun createSampleSchedule(context: Context, scheduleCreator: (String, String, Date) -> Unit) {
        try {
            val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
            val title = "Sample task (${UUID.randomUUID().toString().substring(0, 4)})"
            val description = "This is a sample task created for testing purposes"
            
            Log.d("DebugHelper", "Creating sample schedule item: $title at $tomorrow")
            scheduleCreator(title, description, tomorrow)
        } catch (e: Exception) {
            Log.e("DebugHelper", "Error creating sample schedule", e)
        }
    }
}
