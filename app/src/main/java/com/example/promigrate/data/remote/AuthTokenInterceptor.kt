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

/**
 * Ein Interceptor, der für jeden Netzwerkanruf einen Authentifizierungstoken hinzufügt.
 * Erneuert den Token automatisch, wenn er abgelaufen ist.
 *
 * @property applicationContext Der Applikationskontext.
 */
class AuthTokenInterceptor(context: Context) : Interceptor {

    private val applicationContext = context.applicationContext

    /**
     * Interzeptiert den ausgehenden Anruf und fügt den Authentifizierungstoken hinzu.
     *
     * @param chain: Die Interceptor-Kette.
     * @return: Die Antwort des Anrufs.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(this) {
            val tokenShouldRefresh = checkIfTokenNeedsRefresh(applicationContext)
            if (tokenShouldRefresh) {
                refreshTokenSynchronously(applicationContext)
            }
        }

        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${getToken(applicationContext)}")
            .build()
        return chain.proceed(request)
    }

    /**
     * Überprüft, ob der Token erneuert werden muss.
     *
     * @param context: Der Kontext.
     * @return: True, wenn der Token erneuert werden muss, sonst false.
     */
    private fun checkIfTokenNeedsRefresh(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val expiryDateMillis = sharedPref.getLong("expiry_date_millis", 0)
        return System.currentTimeMillis() > expiryDateMillis
    }

    /**
     * Erneuert den Token synchron.
     *
     * @param context: Der Kontext.
     */
    private fun refreshTokenSynchronously(context: Context) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", "c003a37f-024f-462a-b36d-b001be4cd24a")
            .add("client_secret", "32a39620-32b3-4307-9aa1-511e3d7f48a8")
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
                val expiryDateMillis = System.currentTimeMillis() + expiresIn * 1000
                saveTokenAndExpiryDate(context, token, expiryDateMillis)
            } else {
                Log.e("AuthTokenInterceptor", "Token konnte nicht erneuert werden: ${response.message}")
            }
        } catch (e: IOException) {
            Log.e("AuthTokenInterceptor", "Fehler beim Erneuern des Tokens: ${e.message}")
        }
    }

    /**
     * Ruft den gespeicherten Token ab.
     *
     * @param context: Der Kontext.
     * @return: Der gespeicherte Token.
     */
    private fun getToken(context: Context): String {
        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("token", "") ?: ""
    }

    /**
     * Speichert den Token und sein Ablaufdatum.
     *
     * @param context: Der Kontext.
     * @param token: Der Token.
     * @param expiryDateMillis: Das Ablaufdatum in Millisekunden.
     */
    private fun saveTokenAndExpiryDate(context: Context, token: String, expiryDateMillis: Long) {
        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("token", token)
            putLong("expiry_date_millis", expiryDateMillis)
            apply()
        }
    }
}
