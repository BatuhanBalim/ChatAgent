package com.example.chatbot.data.repository

import com.example.chatbot.data.dao.ScheduleDao
import com.example.chatbot.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID
import java.util.Calendar

/**
 * Repository for managing schedule items
 */
class ScheduleRepository(private val scheduleDao: ScheduleDao) {
    
    /**
     * Get all schedule items ordered by date
     */
    val allScheduleItems: Flow<List<ScheduleItem>> = scheduleDao.getAllScheduleItems()
    
    /**
     * Get upcoming schedule items (not completed and in the future)
     */
    fun getUpcomingItems(): Flow<List<ScheduleItem>> = scheduleDao.getUpcomingScheduleItems(Date())
    
    /**
     * Get schedule items for a specific date
     */
    fun getItemsForDate(date: Date): Flow<List<ScheduleItem>> {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endDate = calendar.time
        
        return scheduleDao.getScheduleItemsByDateRange(startDate, endDate)
    }
    
    /**
     * Create a new schedule item
     */
    suspend fun createScheduleItem(
        title: String,
        description: String,
        dateTime: Date
    ): String {
        val id = UUID.randomUUID().toString()
        val scheduleItem = ScheduleItem(
            id = id,
            title = title,
            description = description,
            dateTime = dateTime
        )
        scheduleDao.insertScheduleItem(scheduleItem)
        return id
    }
    
    /**
     * Get a schedule item by its ID
     */
    suspend fun getScheduleItemById(id: String): ScheduleItem? {
        return scheduleDao.getScheduleItemById(id)
    }
    
    /**
     * Update a schedule item
     */
    suspend fun updateScheduleItem(scheduleItem: ScheduleItem) {
        scheduleDao.updateScheduleItem(scheduleItem)
    }
    
    /**
     * Mark a schedule item as completed or not completed
     */
    suspend fun markScheduleItemCompleted(id: String, isCompleted: Boolean) {
        scheduleDao.updateScheduleItemCompletionStatus(id, isCompleted)
    }
    
    /**
     * Delete a schedule item
     */
    suspend fun deleteScheduleItem(id: String) {
        scheduleDao.deleteScheduleItemById(id)
    }
}
