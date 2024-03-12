package com.example.promigrate.data.model


/**
Diese Klasse beinhaltet Datenklassen, die für die Kommunikation mit dem DeepL Übersetzungsservice
verwendet werden. Diese Modelle erleichtern das Senden von Übersetzungsanfragen und das Empfangen von Antworten.
 */
data class TranslationRequest(
    val text: List<String>,
    val target_lang: String
)


data class TranslationResponse(
    val translations: List<TranslationResult>
)

data class TranslationResult(
    val detected_source_language: String,
    val text: String
)
