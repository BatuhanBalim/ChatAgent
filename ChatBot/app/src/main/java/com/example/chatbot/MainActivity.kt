package com.example.chatbot

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.chatbot.ui.screens.DebugHelper
import com.example.chatbot.ui.screens.MainScreen
import com.example.chatbot.ui.theme.ChatBotTheme
import com.example.chatbot.ui.viewmodel.ChatViewModel
import com.example.chatbot.utils.PermissionHandler
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: ChatViewModel
    
    // Calendar permission launcher
    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "Calendar permissions granted! Events will now sync to your calendar.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Calendar permissions denied. Events won't sync to calendar.", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this, 
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ChatViewModel::class.java]
        
        // Request calendar permissions on first launch
        if (!viewModel.hasCalendarPermissions()) {
            requestCalendarPermissions()
        }
        
        enableEdgeToEdge()
        setContent {
            ChatBotTheme {
                val coroutineScope = rememberCoroutineScope()
                
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Use MainScreen instead of ChatScreen
                        MainScreen(viewModel = viewModel)
                    }
                    
                    // Debug button for testing schedule functionality
                    FloatingActionButton(
                        onClick = { 
                            if (!viewModel.hasCalendarPermissions()) {
                                requestCalendarPermissions()
                            }
                            Toast.makeText(this@MainActivity, "Adding test schedule item", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                DebugHelper.createSampleSchedule(this@MainActivity) { title, desc, date ->
                                    viewModel.addScheduleItem(title, desc, date)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Test Schedule")
                    }
                }
            }
        }
    }
    
    /**
     * Request calendar permissions from the user
     */
    private fun requestCalendarPermissions() {
        calendarPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.READ_CALENDAR,
                android.Manifest.permission.WRITE_CALENDAR
            )
        )
    }
}