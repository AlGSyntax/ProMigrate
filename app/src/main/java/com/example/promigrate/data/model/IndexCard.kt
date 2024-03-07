package com.example.promigrate.data.model

data class IndexCard(
    var id: String? = null,  // Entferne die UUID-Initialisierung
    var frontText: String = "",
    var backText: String = "",
    var isFlipped: Boolean = false
)

