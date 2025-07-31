package com.example.chatbot.data.repository

import com.example.chatbot.data.dao.UserProfileDao
import com.example.chatbot.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/**
 * Repository for managing user profile data
 */
class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    
    /**
     * Gets the user profile as a Flow
     */
    val userProfile: Flow<UserProfile> = userProfileDao.getUserProfile().map { 
        it ?: UserProfile() // Return default profile if none exists
    }
    
    /**
     * Updates the user's basic profile information
     */
    suspend fun updateUserProfile(
        name: String,
        birthday: String,
        occupation: String,
        hobbies: String
    ) {
        userProfileDao.updateUserProfileData(
            name = name,
            birthday = birthday,
            occupation = occupation,
            hobbies = hobbies
        )
    }
    
    /**
     * Sets a user preference in the JSON preferences string
     */
    suspend fun setUserPreference(key: String, value: String) {
        // First get the current profile
        val profile = userProfileDao.getUserProfile().map { it ?: UserProfile() }.first()
        
        // Create or parse the preferences JSON
        val preferencesJson = try {
            JSONObject(profile.preferences.ifEmpty { "{}" })
        } catch (e: Exception) {
            JSONObject("{}")
        }
        
        // Update the preferences
        preferencesJson.put(key, value)
        userProfileDao.updateUserPreferences(preferencesJson.toString())
    }
    
    /**
     * Gets a user preference from the JSON preferences string
     */
    suspend fun getUserPreference(key: String, defaultValue: String = ""): String {
        // Get the current profile
        val profile = userProfileDao.getUserProfile().map { it ?: UserProfile() }.first()
        
        // Parse the preferences JSON
        val preferencesJson = try {
            JSONObject(profile.preferences.ifEmpty { "{}" })
        } catch (e: Exception) {
            JSONObject("{}")
        }
        
        // Return the preference value if it exists
        return if (preferencesJson.has(key)) {
            preferencesJson.getString(key)
        } else {
            defaultValue
        }
    }
    
    /**
     * Creates a new user profile if it doesn't exist
     */
    suspend fun createProfileIfNotExists() {
        val profile = userProfileDao.getUserProfile().first()
        if (profile == null) {
            userProfileDao.insertUserProfile(UserProfile())
        }
    }
}
