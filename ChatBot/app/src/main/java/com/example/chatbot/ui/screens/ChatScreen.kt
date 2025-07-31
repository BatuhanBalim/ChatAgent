package com.example.chatbot.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.chatbot.ui.components.ErrorMessage
import com.example.chatbot.ui.components.MessageInput
import com.example.chatbot.ui.components.MessageList
import com.example.chatbot.ui.viewmodel.ChatViewModel

/**
 * Main chat screen composable
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Error message (if any)
                if (uiState.error != null) {
                    ErrorMessage(
                        errorMessage = uiState.error ?: "",
                        onDismiss = { viewModel.clearError() },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // Message list
                MessageList(
                    messages = uiState.messages,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.weight(1f)
                )
                
                // Input field
                MessageInput(
                    onSendMessage = { message -> 
                        viewModel.sendMessage(message) 
                    },
                    isEnabled = uiState.inputEnabled
                )
            }
            
            // New Chat button - now positioned at the bottom-right
            FloatingActionButton(
                onClick = { viewModel.createNewChat() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New Chat",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
