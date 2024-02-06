package com.example.promigrate.data.model

/**
 * Datenklasse, die ein Benutzerprofil repräsentiert.
 * @property isPremium Gibt an, ob der Benutzer ein Premium-Konto hat.
 * @property username Der Benutzername des Benutzers.
 * @property languageCode Der Sprachcode, der die bevorzugte Sprache des Benutzers angibt.
 * @property profileImageUrl Die URL des Profilbilds des Benutzers.
 * @property name Der Name des Benutzers.
 * @property age Das Alter des Benutzers.
 * @property work Die aktuelle berufliche Tätigkeit des Benutzers.
 */
data class Profile(
    val isPremium: Boolean = false,
    val username: String = "",
    val languageCode: String = "",
    val profileImageUrl: String? = null, // URL des Profilbilds
    val name: String = "",
    val age: Int = 0, // Alter als Int, da es normalerweise als Zahl gespeichert wird
    val work: String = "",
    val languageLevel : String = "",
    val desiredLocation: String = ""
)
