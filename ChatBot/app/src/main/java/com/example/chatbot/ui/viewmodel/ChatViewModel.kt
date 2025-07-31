package com.example.chatbot.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.data.model.Message
import com.example.chatbot.data.preferences.PreferencesManager
import com.example.chatbot.data.repository.ChatRepository
import com.example.chatbot.data.api.ApiConstants
import com.example.chatbot.data.db.AppDatabase
import com.example.chatbot.data.model.ChatSession
import com.example.chatbot.data.model.ScheduleItem
import com.example.chatbot.data.model.UserProfile
import com.example.chatbot.data.repository.ChatSessionRepository
import com.example.chatbot.data.repository.ScheduleRepository
import com.example.chatbot.data.repository.UserProfileRepository
import com.example.chatbot.utils.CalendarIntegration
import com.example.chatbot.utils.PermissionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ViewModel to manage chat state and handle user interactions
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository()
    private val preferencesManager = PreferencesManager(application)
    
    // Database and repository for chat history
    private val database = AppDatabase.getDatabase(application)
    private val chatSessionRepository = ChatSessionRepository(database.chatSessionDao())
    private val scheduleRepository = ScheduleRepository(database.scheduleDao())
    private val userProfileRepository = UserProfileRepository(database.userProfileDao())
    
    // UI state
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // Chat history state
    private val _chatHistoryState = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatHistoryState: StateFlow<List<ChatSession>> = _chatHistoryState.asStateFlow()
    
    // Profile state
    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()
    
    // Schedule state
    private val _scheduleState = MutableStateFlow(ScheduleState())
    val scheduleState: StateFlow<ScheduleState> = _scheduleState.asStateFlow()
    
    // Current session ID (null if not saved yet)
    private var currentSessionId: String? = null
    
    init {
        // Set the embedded API key on initialization
        _uiState.update { it.copy(apiKey = ApiConstants.OPENAI_API_KEY) }
        
        // Load chat history
        loadChatHistory()
        
        // Load user profile
        loadUserProfile()
        
        // Load schedule items
        loadScheduleItems()
        
        // Create profile if it doesn't exist
        viewModelScope.launch {
            userProfileRepository.createProfileIfNotExists()
        }
    }
    
    /**
     * Loads the chat history from the database
     */
    private fun loadChatHistory() {
        viewModelScope.launch {
            chatSessionRepository.allChatSessions.collect { sessions ->
                _chatHistoryState.update { sessions }
            }
        }
    }
    
    /**
     * Loads the user profile from the database
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            userProfileRepository.userProfile.collect { profile ->
                _profileState.update { 
                    it.copy(
                        name = profile.name,
                        birthday = profile.birthday,
                        occupation = profile.occupation,
                        hobbies = profile.hobbies
                    )
                }
            }
        }
    }
    
    /**
     * Loads schedule items from the database
     */
    private fun loadScheduleItems() {
        viewModelScope.launch {
            Log.d("ChatViewModel", "Loading schedule items")
            scheduleRepository.allScheduleItems.collect { items ->
                Log.d("ChatViewModel", "Received ${items.size} schedule items from Flow")
                _scheduleState.update { it.copy(items = items) }
            }
        }
    }
    
    /**
     * Creates a new chat session (clears the current chat)
     */
    fun createNewChat() {
        currentSessionId = null
        _uiState.update { 
            it.copy(
                messages = emptyList(),
                isLoading = false,
                inputEnabled = true,
                error = null
            )
        }
    }
    
    /**
     * Loads a chat session from history
     */
    fun loadChatSession(sessionId: String) {
        viewModelScope.launch {
            val session = chatSessionRepository.getChatSessionById(sessionId)
            if (session != null) {
                currentSessionId = sessionId
                _uiState.update { 
                    it.copy(
                        messages = session.messages,
                        isLoading = false,
                        inputEnabled = true,
                        error = null
                    )
                }
            }
        }
    }
    
    /**
     * Saves the current chat as a session in history
     */
    fun saveCurrentChat() {
        viewModelScope.launch {
            val messages = _uiState.value.messages
            if (messages.isNotEmpty()) {
                if (currentSessionId == null) {
                    // Create new session
                    val newSessionId = chatSessionRepository.createChatSession(messages)
                    currentSessionId = newSessionId
                } else {
                    // Update existing session
                    chatSessionRepository.updateChatSession(currentSessionId!!, messages)
                }
            }
        }
    }
    
    /**
     * Deletes a chat session from history
     */
    fun deleteChatSession(sessionId: String) {
        viewModelScope.launch {
            chatSessionRepository.deleteChatSession(sessionId)
            
            // If we're deleting the current session, create a new one
            if (sessionId == currentSessionId) {
                createNewChat()
            }
        }
    }
    
    /**
     * Sends a user message to the OpenAI API
     * Note: Using embedded API key, no need to pass it as parameter
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        // First check if it's a command for adding schedule/reminders
        val commandResult = processLocalCommand(content)
        if (commandResult) {
            return // Command was processed locally, no need to send to API
        }
        
        // Create and add user message to the chat
        val userMessage = Message(
            content = content,
            isUserMessage = true,
            timestamp = Date()
        )
        
        // Add user message to the list
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + userMessage,
                isLoading = true,
                inputEnabled = false,
                error = null
            )
        }
        
        // Make API call with context enhancement
        viewModelScope.launch {
            try {
                // Show loading state
                _uiState.update { it.copy(isLoading = true) }
                
                // Add context about the user profile and upcoming schedule items
                val profileInfo = _profileState.value
                val scheduleItems = _scheduleState.value.items.filter { !it.isCompleted && it.dateTime.after(Date()) }
                                    .sortedBy { it.dateTime }.take(3)
                
                // Create a system message with user context
                val contextMessage = buildUserContextMessage(profileInfo, scheduleItems)
                
                val result = repository.sendMessageToOpenAI(
                    apiKey = ApiConstants.OPENAI_API_KEY,
                    messageHistory = _uiState.value.messages,
                    userMessage = content,
                    systemContext = contextMessage
                )
                
                result.fold(
                    onSuccess = { responseMessage ->
                        // Add AI response to the list
                        _uiState.update { currentState ->
                            currentState.copy(
                                messages = currentState.messages + responseMessage,
                                isLoading = false,
                                inputEnabled = true,
                                error = null
                            )
                        }
                        
                        // Auto-save the chat after each message exchange
                        saveCurrentChat()
                    },
                    onFailure = { error ->
                        // Handle error
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                inputEnabled = true,
                                error = error.message ?: "Unknown error occurred"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                // Handle any unexpected errors
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        inputEnabled = true,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
    
    /**
     * Process local commands without needing API calls
     * Returns true if the command was processed locally
     */
    private fun processLocalCommand(content: String): Boolean {
        // Simple command parsing
        val lowerContent = content.lowercase()
        
        // Schedule/reminder commands
        if (lowerContent.contains("remind me") || lowerContent.contains("add to schedule") || 
            lowerContent.contains("schedule") && (lowerContent.contains("new") || lowerContent.contains("add"))) {
            
            try {
                // Extract date from the message
                val datePattern = "(today|tomorrow|\\d{1,2}[/-]\\d{1,2}|\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|january|february|march|april|may|june|july|august|september|october|november|december)"
                    .toRegex(RegexOption.IGNORE_CASE)
                val timePattern = "(\\d{1,2}(:\\d{2})?(\\s?[ap]m)?)"
                    .toRegex(RegexOption.IGNORE_CASE)
                
                val dateMatch = datePattern.find(lowerContent)
                val timeMatch = timePattern.find(lowerContent)
                
                val calendar = Calendar.getInstance()
                var dateFound = false
                var timeFound = false
                
                // Parse date if found
                if (dateMatch != null) {
                    val dateStr = dateMatch.value
                    when {
                        dateStr.equals("today", ignoreCase = true) -> {
                            // Already set to today
                            dateFound = true
                        }
                        dateStr.equals("tomorrow", ignoreCase = true) -> {
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                            dateFound = true
                        }
                        // More date parsing could be added here
                    }
                }
                
                // Parse time if found
                if (timeMatch != null) {
                    val timeStr = timeMatch.value
                    // Simple time parsing - could be enhanced
                    val isAM = timeStr.lowercase().contains("am")
                    val isPM = timeStr.lowercase().contains("pm")
                    
                    val timeParts = timeStr.replace(Regex("[^0-9:]"), "").split(":")
                    if (timeParts.isNotEmpty()) {
                        val hour = timeParts[0].toInt() 
                        val adjustedHour = when {
                            isPM && hour < 12 -> hour + 12
                            isAM && hour == 12 -> 0
                            else -> hour
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, adjustedHour)
                        
                        if (timeParts.size > 1) {
                            calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                        } else {
                            calendar.set(Calendar.MINUTE, 0)
                        }
                        calendar.set(Calendar.SECOND, 0)
                        timeFound = true
                    }
                }
                
                // Extract the title - remove the command words and date/time parts
                var title = content
                    .replace(Regex("remind me (to|about) ", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("add to schedule ", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("schedule ", RegexOption.IGNORE_CASE), "")
                    .replace(dateMatch?.value ?: "", "")
                    .replace(timeMatch?.value ?: "", "")
                    .replace(Regex("(on|at|by) ", RegexOption.IGNORE_CASE), "")
                    .trim()
                
                if (title.length > 3 && (dateFound || timeFound)) {
                    // If title derived from parsing is too short, use a default
                    if (title.length < 5) {
                        title = "Reminder: " + content.trim()
                    }
                    
                    // Add the schedule item
                    viewModelScope.launch {
                        Log.d("ChatViewModel", "Creating schedule item: $title at ${calendar.time}")
                        try {
                            // Use withContext to ensure this runs on IO thread
                            withContext(Dispatchers.IO) {
                                val itemId = scheduleRepository.createScheduleItem(
                                    title = title,
                                    description = if (content != title) content else "",
                                    dateTime = calendar.time
                                )
                                Log.d("ChatViewModel", "Successfully created schedule item with ID: $itemId")
                                
                                // Add to device calendar if permissions are available
                                if (CalendarIntegration.hasCalendarPermissions(getApplication())) {
                                    val calendarSuccess = CalendarIntegration.addEventToCalendar(
                                        context = getApplication(),
                                        title = title,
                                        description = if (content != title) content else "",
                                        startTime = calendar.time
                                    )
                                    if (calendarSuccess) {
                                        Log.d("ChatViewModel", "Event added to device calendar successfully")
                                    } else {
                                        Log.w("ChatViewModel", "Failed to add event to device calendar")
                                    }
                                } else {
                                    Log.w("ChatViewModel", "Calendar permissions not granted")
                                }
                                
                                // Force refresh the schedule items
                                val items = scheduleRepository.allScheduleItems.first()
                                Log.d("ChatViewModel", "Current items in database: ${items.size}")
                                _scheduleState.update { it.copy(items = items) }
                            }
                            
                            // Add bot confirmation message
                            val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
                            val formattedDate = dateFormatter.format(calendar.time)
                            val confirmMessage = Message(
                                content = "I've added \"$title\" to your schedule for $formattedDate.",
                                isUserMessage = false,
                                timestamp = Date()
                            )
                            
                            _uiState.update { currentState ->
                                currentState.copy(
                                    messages = currentState.messages + Message(
                                        content = content,
                                        isUserMessage = true,
                                        timestamp = Date()
                                    ) + confirmMessage
                                )
                            }
                            
                            // Auto-save the chat after the exchange
                            saveCurrentChat()
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "Error creating schedule item", e)
                        }
                    }
                    return true
                }
            } catch (e: Exception) {
                // If parsing fails, continue to API call
            }
        }
        
        return false
    }
    
    /**
     * Build a system context message based on user profile and schedule
     */
    private fun buildUserContextMessage(profile: ProfileState, upcomingItems: List<ScheduleItem>): String {
        val contextBuilder = StringBuilder("You are a personal assistant chatbot. ")
        
        // Add user name if available
        if (profile.name.isNotBlank()) {
            contextBuilder.append("The user's name is ${profile.name}. ")
        }
        
        // Add other profile information if available
        if (profile.occupation.isNotBlank()) {
            contextBuilder.append("They work as ${profile.occupation}. ")
        }
        
        if (profile.birthday.isNotBlank()) {
            contextBuilder.append("Their birthday is ${profile.birthday}. ")
        }
        
        if (profile.hobbies.isNotBlank()) {
            contextBuilder.append("Their interests include ${profile.hobbies}. ")
        }
        
        // Add upcoming schedule information
        if (upcomingItems.isNotEmpty()) {
            contextBuilder.append("\n\nUpcoming schedule: ")
            val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            
            upcomingItems.forEach { item ->
                contextBuilder.append("\n- ${item.title} on ${dateFormatter.format(item.dateTime)}")
            }
        }
        
        return contextBuilder.toString()
    }
    
    /**
     * Clears the error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Updates the user profile information
     */
    fun updateProfile(name: String, birthday: String, occupation: String, hobbies: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateUserProfile(
                    name = name,
                    birthday = birthday, 
                    occupation = occupation,
                    hobbies = hobbies
                )
                
                _profileState.update {
                    it.copy(
                        name = name,
                        birthday = birthday,
                        occupation = occupation,
                        hobbies = hobbies,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _profileState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Add a schedule item and optionally add it to device calendar
     */
    public fun addScheduleItem(title: String, description: String, dateTime: Date) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Add to app's internal schedule
                    val itemId = scheduleRepository.createScheduleItem(
                        title = title,
                        description = description,
                        dateTime = dateTime
                    )
                    Log.d("ChatViewModel", "Schedule item created with ID: $itemId")
                    
                    // Add to device calendar if permissions are available
                    if (CalendarIntegration.hasCalendarPermissions(getApplication())) {
                        val calendarSuccess = CalendarIntegration.addEventToCalendar(
                            context = getApplication(),
                            title = title,
                            description = description,
                            startTime = dateTime
                        )
                        if (calendarSuccess) {
                            Log.d("ChatViewModel", "Event added to device calendar successfully")
                        }
                    }
                    
                    // Refresh schedule items
                    val items = scheduleRepository.allScheduleItems.first()
                    _scheduleState.update { it.copy(items = items) }
                }
            } catch (e: Exception) {
                _scheduleState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Deletes a schedule item
     */
    fun deleteScheduleItem(id: String) {
        viewModelScope.launch {
            try {
                scheduleRepository.deleteScheduleItem(id)
            } catch (e: Exception) {
                _scheduleState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Marks a schedule item as completed
     */
    fun markScheduleItemCompleted(id: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                scheduleRepository.markScheduleItemCompleted(id, isCompleted)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error adding schedule item", e)
            }
        }
    }
    
    /**
     * Check if calendar permissions are granted
     */
    fun hasCalendarPermissions(): Boolean {
        return CalendarIntegration.hasCalendarPermissions(getApplication())
    }
}

/**
 * Represents the UI state for the chat screen
 */
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val inputEnabled: Boolean = true,
    val error: String? = null,
    val apiKey: String = ""
)
