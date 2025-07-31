package com.example.chatbot.data.db

import androidx.room.TypeConverter
import com.example.chatbot.data.model.Message
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Type converters for Room database to store complex objects
 */
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromMessageList(messages: List<Message>): String {
        return gson.toJson(messages)
    }
    
    @TypeConverter
    fun toMessageList(messagesString: String): List<Message> {
        val listType = object : TypeToken<List<Message>>() {}.type
        return gson.fromJson(messagesString, listType)
    }
}
