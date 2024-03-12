package com.example.promigrate.data.remote

import android.content.Context
import com.example.promigrate.data.model.BildungsangebotResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ProMigrateCourseAPIService {

    @GET("infosysbub/sprachfoerderung/pc/v1/bildungsangebot")
    suspend fun getBildungsangebot(
        @Query("systematiken") systematiken: String?,
        @Query("orte") orte: String?,
        @Query("sprachniveau") sprachniveau: String?,
        @Query("beginntermine") beginntermine: Int ,
        @Query("sort") sort: String = "basc",
        @Query("umkreis") umkreis: String = "50"
    ): Response<BildungsangebotResponse>



}

object ProMigrateCourseAPI {


    private const val BASE_URL = "https://rest.arbeitsagentur.de/"


    private lateinit var okHttpClient: OkHttpClient

    private lateinit var moshi: Moshi

    lateinit var retrofitService: ProMigrateCourseAPIService
    fun init(context: Context) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(CourseAuthTokenInterceptor(context.applicationContext))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()


        retrofitService = retrofit.create(ProMigrateCourseAPIService::class.java)
    }
}