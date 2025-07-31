package com.example.chatbot.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Utility class to securely store and retrieve the API key
 */
class PreferencesManager(context: Context) {

    companion object {
        private const val PREFERENCES_FILE = "chatbot_secure_prefs"
        private const val API_KEY = "api_key"
    }
    
    // Create or retrieve the MasterKey for encryption
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    // Create or open an encrypted SharedPreferences file
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFERENCES_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Save the API key to encrypted SharedPreferences
     */
    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(API_KEY, apiKey).apply()
    }
    
    /**
     * Retrieve the saved API key
     * @return The API key or an empty string if not found
     */
    fun getApiKey(): String {
        return sharedPreferences.getString(API_KEY, "") ?: ""
    }
    
    /**
     * Check if an API key has been saved
     */
    fun hasApiKey(): Boolean {
        return !getApiKey().isBlank()
    }
    
    /**
     * Clear the saved API key
     */
    fun clearApiKey() {
        sharedPreferences.edit().remove(API_KEY).apply()
    }
}
