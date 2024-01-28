package com.example.promigrate.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.promigrate.data.model.UserProfile

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(userProfile: UserProfile)

    @Query("SELECT * FROM UserProfile WHERE userId = :userId")
    fun getUserProfile(userId: String): LiveData<UserProfile>

    // Weitere Methoden nach Bedarf
}

