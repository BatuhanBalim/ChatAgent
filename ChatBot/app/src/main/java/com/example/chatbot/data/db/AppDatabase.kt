package com.example.chatbot.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chatbot.data.dao.ScheduleDao
import com.example.chatbot.data.dao.UserProfileDao
import com.example.chatbot.data.model.ChatSession
import com.example.chatbot.data.model.ScheduleItem
import com.example.chatbot.data.model.UserProfile

/**
 * Main database class for the application
 */
@Database(entities = [ChatSession::class, ScheduleItem::class, UserProfile::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun chatSessionDao(): ChatSessionDao
    
    abstract fun scheduleDao(): ScheduleDao
    
    abstract fun userProfileDao(): UserProfileDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chatbot_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
