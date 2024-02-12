package com.example.promigrate.data.remote

import com.example.promigrate.data.model.TranslationRequest
import com.example.promigrate.data.model.TranslationResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface DeepLApiService {


    @POST("/v2/translate")
    @Headers("Authorization: DeepL-Auth-Key b3231e9c-fe21-1292-f347-0c1124304d1c:fx", "User-Agent: YourApp/1.2.3")
    suspend fun translateText(@Body requestBody: TranslationRequest): TranslationResponse

    companion object {
        fun create(): DeepLApiService {
            return Retrofit.Builder()
                .baseUrl("https://api-free.deepl.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DeepLApiService::class.java)
        }
    }
}