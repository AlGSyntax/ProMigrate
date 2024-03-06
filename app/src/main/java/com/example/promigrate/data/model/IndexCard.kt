package com.example.promigrate.data.model

import java.util.UUID

data class IndexCard(
    val id: String? = UUID.randomUUID().toString(), // Generiert eine eindeutige ID
    var frontText: String = "",
    var backText: String = "",
    var isFlipped: Boolean = false
)

