package com.example.promigrate.data.remote

import android.content.Context
import com.example.promigrate.data.model.JobDetailsResponse
import com.example.promigrate.data.model.JobResponse
import com.google.android.datatransport.BuildConfig
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

const val BASE_URL = "https://rest.arbeitsagentur.de/"

interface ProMigrateAPIService {

    @GET("jobboerse/jobsuche-service/pc/v4/jobs")
    suspend fun getJobs(
        @Query("wo") wo: String?,
        @Query("berufsfeld") berufsfeld: String?
    ): Response<JobResponse>


    @GET("jobboerse/jobsuche-service/pc/v4/jobs")
    suspend fun getBerufsfelder(): Response<JobResponse>

    @GET("jobboerse/jobsuche-service/pc/v4/jobs")
    suspend fun getArbeitsorte(): Response<JobResponse>

    @GET("jobboerse/jobsuche-service/pc/v4/jobs")
    suspend fun getJobOffers(
        @Query("was") was: String,
        @Query("wo") wo: String,
        // Hinzufügen des Arbeitsorts als Parameter
    ): Response<JobResponse>

    @GET("jobboerse/jobsuche-service/pc/v2/jobdetails/{encodedHashID}")
    suspend fun getJobDetails(@Path("encodedHashID") encodedHashID: String?): Response<JobDetailsResponse>





}

object ProMigrateAPI {
    private lateinit var okHttpClient: OkHttpClient

    private lateinit var moshi: Moshi

    // Verschiebe die Deklaration von retrofitService außerhalb der init Methode
    lateinit var retrofitService: ProMigrateAPIService


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

        // Initialisiere retrofitService hier
        retrofitService = retrofit.create(ProMigrateAPIService::class.java)
    }
}



