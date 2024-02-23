package com.example.promigrate.data.model


data class Profile(
    val isPremium: Boolean = false,
    val username: String? = "",
    val languageCode: String? = "",
    val profileImageUrl: String? = null, // URL des Profilbilds
    val name: String? = "",
    val age: Int? = 0, // Alter als Int, da es normalerweise als Zahl gespeichert wird
    val fieldOfWork: String? = "",
    val languageLevel : String? = "",
    val desiredLocation: String? = "",
    val street: String? = "",
    val birthplace: String?= "",
    val maidenname: String? = "",
    var selectedJobs: Map<String, String>? = null
)
