package com.example.chatbot.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.chatbot.ui.viewmodel.ChatViewModel

/**
 * Main screen with bottom navigation between chat and history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ChatViewModel) {
    // Track the current tab
    var selectedTab by remember { mutableStateOf(Tab.CHAT) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (selectedTab == Tab.CHAT) "AI Assistant" else "Chat History",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.values().forEach { tab ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = { 
                            selectedTab = tab
                            
                            // If switching to chat tab with no active chat, create a new one
                            if (tab == Tab.CHAT && viewModel.uiState.value.messages.isEmpty()) {
                                viewModel.createNewChat()
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Content based on selected tab
        when (selectedTab) {
            Tab.CHAT -> {
                ChatScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            Tab.SCHEDULE -> {
                ScheduleScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            Tab.PROFILE -> {
                ProfileScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            Tab.HISTORY -> {
                ChatHistoryScreen(
                    viewModel = viewModel,
                    onChatSelected = { sessionId ->
                        viewModel.loadChatSession(sessionId)
                        selectedTab = Tab.CHAT
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Enum representing the available tabs
 */
enum class Tab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    CHAT("Chat", Icons.Filled.Home),
    SCHEDULE("Schedule", Icons.Filled.DateRange),
    PROFILE("Profile", Icons.Filled.Person),
    HISTORY("History", Icons.Filled.List)
}
