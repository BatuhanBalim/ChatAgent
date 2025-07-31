package com.example.chatbot.data.api

/**
 * Model classes for OpenAI API requests and responses
 */

// Request models
data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    val max_tokens: Int = 150,
    val temperature: Double = 0.7
)

data class ChatMessage(
    val role: String,
    val content: String
)

// Response models
data class ChatCompletionResponse(
    val id: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String?
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)
