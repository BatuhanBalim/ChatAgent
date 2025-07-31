package com.example.chatbot.utils

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.*

/**
 * Utility class for integrating with the device's calendar app
 */
object CalendarIntegration {
    
    private const val TAG = "CalendarIntegration"
    
    /**
     * Check if calendar permissions are granted
     */
    fun hasCalendarPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get the primary calendar ID for the device
     */
    private fun getPrimaryCalendarId(context: Context): Long? {
        if (!hasCalendarPermissions(context)) {
            Log.w(TAG, "Calendar permissions not granted")
            return null
        }
        
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.IS_PRIMARY
        )
        
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )
        
        cursor?.use {
            while (it.moveToNext()) {
                val calendarId = it.getLong(0)
                val isPrimary = it.getInt(2)
                
                // Return the first primary calendar found
                if (isPrimary == 1) {
                    Log.d(TAG, "Found primary calendar with ID: $calendarId")
                    return calendarId
                }
            }
            
            // If no primary calendar found, return the first available calendar
            if (it.moveToFirst()) {
                val calendarId = it.getLong(0)
                Log.d(TAG, "Using first available calendar with ID: $calendarId")
                return calendarId
            }
        }
        
        Log.w(TAG, "No calendar found")
        return null
    }
    
    /**
     * Add an event to the device calendar
     */
    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        startTime: Date,
        endTime: Date? = null
    ): Boolean {
        if (!hasCalendarPermissions(context)) {
            Log.w(TAG, "Calendar permissions not granted")
            return false
        }
        
        val calendarId = getPrimaryCalendarId(context) ?: return false
        
        // Default end time to 1 hour after start time if not provided
        val eventEndTime = endTime ?: Date(startTime.time + 60 * 60 * 1000)
        
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTime.time)
            put(CalendarContract.Events.DTEND, eventEndTime.time)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_DEFAULT)
            put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        }
        
        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                Log.d(TAG, "Event added to calendar successfully: $uri")
                true
            } else {
                Log.e(TAG, "Failed to add event to calendar")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding event to calendar", e)
            false
        }
    }
    
    /**
     * Add a reminder for the event (optional)
     */
    fun addEventReminder(
        context: Context,
        eventId: Long,
        minutesBefore: Int = 15
    ): Boolean {
        if (!hasCalendarPermissions(context)) {
            return false
        }
        
        val values = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, minutesBefore)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        
        return try {
            val uri = context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
            uri != null
        } catch (e: Exception) {
            Log.e(TAG, "Error adding reminder", e)
            false
        }
    }
}
