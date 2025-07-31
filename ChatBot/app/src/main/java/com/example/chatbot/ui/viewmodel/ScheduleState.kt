package com.example.chatbot.ui.viewmodel

import com.example.chatbot.data.model.ScheduleItem

/**
 * Represents the UI state for the schedule screen
 */
data class ScheduleState(
    val items: List<ScheduleItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
