package com.example.chatbot.ui.viewmodel

/**
 * Represents the UI state for the profile screen
 */
data class ProfileState(
    val name: String = "",
    val birthday: String = "",
    val occupation: String = "",
    val hobbies: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
