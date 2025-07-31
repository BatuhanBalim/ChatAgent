package com.example.chatbot.data.dao

import androidx.room.*
import com.example.chatbot.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user profile data
 */
@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)
    
    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)
    
    @Query("UPDATE user_profile SET name = :name, birthday = :birthday, occupation = :occupation, hobbies = :hobbies, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateUserProfileData(
        name: String,
        birthday: String,
        occupation: String,
        hobbies: String,
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE user_profile SET preferences = :preferences, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateUserPreferences(
        preferences: String,
        timestamp: Long = System.currentTimeMillis()
    )
}
