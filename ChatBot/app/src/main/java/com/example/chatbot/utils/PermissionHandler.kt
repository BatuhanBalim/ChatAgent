package com.example.chatbot.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for handling runtime permissions
 */
object PermissionHandler {
    
    const val CALENDAR_PERMISSION_REQUEST_CODE = 1001
    
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
     * Request calendar permissions
     */
    fun requestCalendarPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            ),
            CALENDAR_PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * Check if permission request was granted
     */
    fun isPermissionGranted(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && 
               grantResults.all { it == PackageManager.PERMISSION_GRANTED }
    }
}
