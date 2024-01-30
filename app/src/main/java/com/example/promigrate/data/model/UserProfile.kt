package com.example.promigrate.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserProfile(
    @PrimaryKey
    val userId:String,
    val languageCode: String// Languagecode sauber managen in Firebase !
)

