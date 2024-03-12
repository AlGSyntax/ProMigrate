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

/**
 * Schnittstelle für den DeepL API-Dienst.
 */
interface DeepLApiService {

   /**
     * Übersetzt Text mithilfe der DeepL-API.
     *
     * @param requestBody Der Anforderungstext, der den zu übersetzenden Text enthält.
     * @return Die Antwort der DeepL-API, die den übersetzten Text enthält.
     */
    @POST("/v2/translate")
    suspend fun translateText(@Body requestBody: TranslationRequest): TranslationResponse

    companion object {
       /**
         * Erstellt eine Instanz des DeepL-API-Dienstes.
         *
         * @return Eine Instanz des DeepL-API-Dienstes.
         */
        fun create(): DeepLApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        // Den DeepL-API-Schlüssel zum Authorization-Header hinzufügen.
                        .addHeader("Authorization", "DeepL-Auth-Key ${BuildConfig.DEEP_L_API_KEY}")
                        // Den User-Agent-Header hinzufügen.
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