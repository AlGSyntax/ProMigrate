package com.example.promigrate.data.remote

import android.content.Context
import com.example.promigrate.data.model.JobDetailsResponse
import com.example.promigrate.data.model.JobResponse
import com.example.promigrate.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Basis-URL für die API-Endpunkte.
const val BASE_URL = "https://rest.arbeitsagentur.de/"

// API-Schnittstellendefinition für ProMigrate.
interface ProMigrateAPIService {

    // Abrufen von Jobs basierend auf Standort und Berufsfeld.
    @GET("jobboerse/jobsuche-service/pc/v4/jobs")
    suspend fun getJobs(
        @Query("wo") wo: String?,
        @Query("berufsfeld") berufsfeld: String?
    ): Response<JobResponse>

    // Abrufen verfügbarer Berufsfelder.
    @GET("jobboerse/jobsuche-service/pc/v4/jobs")
    suspend fun getOccupationalFields(): Response<JobResponse>

    // Abrufen verfügbarer Arbeitsorte.
    @GET("jobboerse/jobsuche-service/pc/v4/jobs")
    suspend fun getWorkLocations(): Response<JobResponse>

    // Abrufen von Jobangeboten basierend auf einer spezifischen Suche.
    @GET("jobboerse/jobsuche-service/pc/v4/jobs")
    suspend fun getJobOffers(
        @Query("was") was: String,
        @Query("wo") wo: String
    ): Response<JobResponse>

    // Abrufen von Details zu einem spezifischen Job.
    @GET("jobboerse/jobsuche-service/pc/v4/jobdetails/{encodedHashID}")
    suspend fun getJobDetails(@Path("encodedHashID") encodedHashID: String?): Response<JobDetailsResponse>
}

// Singleton für die API-Initialisierung und -Konfiguration.
object ProMigrateAPI {
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var moshi: Moshi

    // Service-Instanz für den Zugriff auf die API.
    lateinit var retrofitService: ProMigrateAPIService

    // Initialisiert die API mit Konfigurationen.
    fun init(context: Context) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthTokenInterceptor(context.applicationContext))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofitService = retrofit.create(ProMigrateAPIService::class.java)
    }
}
