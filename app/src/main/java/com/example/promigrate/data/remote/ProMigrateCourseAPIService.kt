package com.example.promigrate.data.remote

import android.content.Context
import com.example.promigrate.data.model.EducationalOfferResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Service-Schnittstelle für die Abfrage von Bildungsangeboten.
interface ProMigrateCourseAPIService {

    // HTTP GET-Anfrage an die API, um Bildungsangebote zu erhalten.
    @GET("infosysbub/sprachfoerderung/pc/v1/bildungsangebot")
    suspend fun getEducationalOffer(
        @Query("systematiken") systematiken: String?,  // Filtert nach Systematiken.
        @Query("orte") orte: String?,  // Filtert nach Orten.
        @Query("sprachniveau") sprachniveau: String?,  // Filtert nach Sprachniveau.
        @Query("beginntermine") beginntermine: Int,  // Filtert nach Beginnterminen.
        @Query("sort") sort: String = "basc",  // Sortierreihenfolge.
        @Query("umkreis") umkreis: String = "25"  // Suchumkreis.
    ): Response<EducationalOfferResponse>
}

// Objekt zur Initialisierung und Bereitstellung des ProMigrateCourseAPIService.
object ProMigrateCourseAPI {

    private const val BASE_URL = "https://rest.arbeitsagentur.de/"  // Basis-URL der API.

    // Client für HTTP-Anfragen.
    private lateinit var okHttpClient: OkHttpClient

    // Moshi-Instanz für die JSON-Verarbeitung.
    private lateinit var moshi: Moshi

    // Öffentlich zugänglicher Retrofit-Service.
    lateinit var retrofitService: ProMigrateCourseAPIService

    // Initialisiert den API-Service mit den erforderlichen Komponenten.
    fun init(context: Context) {
        // Logging-Interceptor für Debugging-Zwecke.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Erstellt und konfiguriert eine Moshi-Instanz.
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        // Erstellt und konfiguriert den OkHttpClient.
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(CourseAuthTokenInterceptor(context.applicationContext))
            .build()

        // Erstellt und konfiguriert eine Retrofit-Instanz.
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        // Erstellt eine Instanz des ProMigrateCourseAPIService.Test um was udududu
        retrofitService = retrofit.create(ProMigrateCourseAPIService::class.java)
    }
}
