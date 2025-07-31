package com.example.chatbot.data.dao

import androidx.room.*
import com.example.chatbot.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for Schedule items
 */
@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_items ORDER BY dateTime ASC")
    fun getAllScheduleItems(): Flow<List<ScheduleItem>>
    
    @Query("SELECT * FROM schedule_items WHERE id = :id")
    suspend fun getScheduleItemById(id: String): ScheduleItem?
    
    @Query("SELECT * FROM schedule_items WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime ASC")
    fun getScheduleItemsByDateRange(startDate: Date, endDate: Date): Flow<List<ScheduleItem>>
    
    @Query("SELECT * FROM schedule_items WHERE dateTime >= :now AND isCompleted = 0 ORDER BY dateTime ASC LIMIT 5")
    fun getUpcomingScheduleItems(now: Date): Flow<List<ScheduleItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(scheduleItem: ScheduleItem)
    
    @Update
    suspend fun updateScheduleItem(scheduleItem: ScheduleItem)
    
    @Query("UPDATE schedule_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateScheduleItemCompletionStatus(id: String, isCompleted: Boolean)
    
    @Delete
    suspend fun deleteScheduleItem(scheduleItem: ScheduleItem)
    
    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteScheduleItemById(id: String)
}
