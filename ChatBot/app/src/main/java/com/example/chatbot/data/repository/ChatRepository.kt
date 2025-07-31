package com.example.chatbot.data.repository

import com.example.chatbot.data.api.ApiClient
import com.example.chatbot.data.api.ChatCompletionRequest
import com.example.chatbot.data.api.ChatMessage
import com.example.chatbot.data.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to handle chat operations and API calls
 */
class ChatRepository {
    private val openAiService = ApiClient.openAiService
    
    /**
     * Sends a message to the OpenAI API and returns the response
     * @param apiKey The OpenAI API key
     * @param messageHistory The list of previous messages for context
     * @param userMessage The new user message to send
     * @param systemContext Optional system context message to provide additional instructions to the model
     * @return Result containing either the AI response message or an error
     */
    suspend fun sendMessageToOpenAI(
        apiKey: String,
        messageHistory: List<Message>,
        userMessage: String,
        systemContext: String? = null
    ): Result<Message> = withContext(Dispatchers.IO) {
        try {
            // Convert app Message objects to OpenAI ChatMessage format
            val chatMessages = convertToChatMessages(messageHistory, userMessage, systemContext)
            
            // Create API request
            val request = ChatCompletionRequest(
                messages = chatMessages
            )
            
            // Make API call
            val response = openAiService.createChatCompletion(
                authHeader = "Bearer $apiKey",
                request = request
            )
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                return@withContext Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
            
            val chatCompletionResponse = response.body()
                ?: return@withContext Result.failure(Exception("Empty response received"))
            
            if (chatCompletionResponse.choices.isEmpty()) {
                return@withContext Result.failure(Exception("No response choices received"))
            }
            
            // Extract the assistant's response
            val assistantMessage = chatCompletionResponse.choices[0].message
            
            // Convert to app Message model
            val responseMessage = Message(
                content = assistantMessage.content,
                isUserMessage = false
            )
            
            Result.success(responseMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Converts app Message objects to OpenAI ChatMessage format
     * @param messageHistory List of previous messages
     * @param newUserMessage The new user message
     * @param systemContext Optional system context to add as a system message
     */
    private fun convertToChatMessages(
        messageHistory: List<Message>,
        newUserMessage: String,
        systemContext: String? = null
    ): List<ChatMessage> {
        val chatMessages = mutableListOf<ChatMessage>()
        
        // Add system message if provided
        if (!systemContext.isNullOrBlank()) {
            chatMessages.add(ChatMessage(role = "system", content = systemContext))
        }
        
        // Add message history
        messageHistory.forEach { message ->
            chatMessages.add(
                ChatMessage(
                    role = if (message.isUserMessage) "user" else "assistant",
                    content = message.content
                )
            )
        }
        
        // Add the new user message
        chatMessages.add(ChatMessage(role = "user", content = newUserMessage))
        
        return chatMessages
    }
}
