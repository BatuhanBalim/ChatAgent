package com.example.chatbot.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chatbot.data.model.ChatSession
import com.example.chatbot.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen to display chat history
 */
@Composable
fun ChatHistoryScreen(
    viewModel: ChatViewModel,
    onChatSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chatHistoryState by viewModel.chatHistoryState.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (chatHistoryState.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No chat history yet",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your conversations will appear here once you start chatting",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // Chat history list
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(chatHistoryState) { session ->
                    ChatSessionItem(
                        chatSession = session,
                        onChatSelected = { onChatSelected(session.id) },
                        onDeleteSession = { viewModel.deleteChatSession(session.id) }
                    )
                }
            }
        }
    }
}

/**
 * Displays a single chat session in the history list
 */
@Composable
fun ChatSessionItem(
    chatSession: ChatSession,
    onChatSelected: () -> Unit,
    onDeleteSession: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onChatSelected() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chatSession.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onDeleteSession
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete chat"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Updated: ${dateFormat.format(chatSession.updatedAt)}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show a preview of the chat (first few messages)
            val previewCount = minOf(chatSession.messages.size, 2)
            if (previewCount > 0) {
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                chatSession.messages.take(previewCount).forEach { message ->
                    val prefix = if (message.isUserMessage) "You: " else "AI: "
                    Text(
                        text = "$prefix ${message.content}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                if (chatSession.messages.size > previewCount) {
                    Text(
                        text = "... ${chatSession.messages.size - previewCount} more messages",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
