package com.example.promigrate.data.remote

import android.content.Context
import android.util.Log
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CourseAuthTokenInterceptor(context: Context) : Interceptor {

    // Verwendung des Applikationskontexts, um Leaks zu vermeiden.
    private val applicationContext = context.applicationContext

    // Diese Methode wird für jeden HTTP-Aufruf durch den Interceptor aufgerufen.
    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(this) {
            // Überprüft, ob der Token erneuert werden muss.
            val tokenShouldRefresh = checkIfTokenNeedsRefresh(applicationContext)
            if (tokenShouldRefresh) {
                // Wenn der Token erneuert werden muss, wird dies synchron gemacht.
                refreshTokenSynchronously(applicationContext)

            }
        }

        // Erstellt eine neue Anfrage mit dem aktualisierten Token.
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${getToken(applicationContext)}")
            .build()
        // Führt die Anfrage mit dem angefügten Token aus.
        return chain.proceed(request)
    }

    // Überprüft, ob der Token abgelaufen ist.
    private fun checkIfTokenNeedsRefresh(context: Context): Boolean {
        val sharedPref =
            context.getSharedPreferences("LanguageCourseTokenPrefs", Context.MODE_PRIVATE)
        val expiryDateMillis = sharedPref.getLong("languageCourseTokenExpiryDateMillis", 0)
        return System.currentTimeMillis() > expiryDateMillis
    }

    // Erneuert den Token synchron.
    private fun refreshTokenSynchronously(context: Context) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", "bd24f42e-ad0b-4005-b834-23bb6800dc6c")
            .add("client_secret", "6776b89e-5728-4643-8cd5-c93aefb5314b")
            .build()
        val request = Request.Builder()
            .url("https://rest.arbeitsagentur.de/oauth/gettoken_cc")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonObject = JSONObject(responseBody ?: "")
                val token = jsonObject.getString("access_token")
                val expiresIn = jsonObject.getLong("expires_in")
                val expiryDateMillis = System.currentTimeMillis() + expiresIn * 3000
                saveTokenAndExpiryDate(context, token, expiryDateMillis)
            } else {
                Log.e(
                    "AuthTokenInterceptor",
                    "Token konnte nicht erneuert werden: ${response.message}"
                )
            }
        } catch (_: IOException) {

        }
    }

    // Holt den aktuellen Token aus den SharedPreferences.
    private fun getToken(context: Context): String {
        val sharedPref =
            context.getSharedPreferences("LanguageCourseTokenPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("languageCourseToken", "") ?: ""
    }

    // Speichert den neuen Token und das Ablaufdatum in den SharedPreferences.
    private fun saveTokenAndExpiryDate(context: Context, token: String, expiryDateMillis: Long) {
        val sharedPref =
            context.getSharedPreferences("LanguageCourseTokenPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("languageCourseToken", token)
            putLong("languageCourseTokenExpiryDateMillis", expiryDateMillis)
            apply()
        }
    }
}
