package com.example.promigrate.data.remote

import com.example.promigrate.BuildConfig
import com.example.promigrate.data.model.TranslationRequest
import com.example.promigrate.data.model.TranslationResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepLApiService {

    @POST("/v2/translate")
    suspend fun translateText(@Body requestBody: TranslationRequest): TranslationResponse

    companion object {
        fun create(): DeepLApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "DeepL-Auth-Key ${BuildConfig.DEEP_L_API_KEY}")
                        .addHeader("User-Agent", "YourApp/1.2.3")
                        .build()
                    chain.proceed(newRequest)
                })
                .build()

            return Retrofit.Builder()
                .baseUrl("https://api-free.deepl.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DeepLApiService::class.java)
        }
    }
}