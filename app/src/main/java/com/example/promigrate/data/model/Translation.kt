package com.example.promigrate.data.model

data class TranslationRequest(
    val text: List<String>,
    val target_lang: String)




data class TranslationResponse(
    val translations: List<TranslationResult>)

data class TranslationResult(
    val detected_source_language: String,
    val text: String)
