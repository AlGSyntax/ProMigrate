package com.example.promigrate.data.remote

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Interceptor für alle Arbeitsagentur-Aufrufe.
 *
 * 1. Fügt IMMER den statischen X-API-Key an.
 * 2. Fügt – sofern vorhanden – zusätzlich einen Bearer-Token an.
 * 3. Erneuert den Token synchron, wenn er abgelaufen ist ODER ein 401
 *    (Unauthorized) zurückkommt (Retry-Mechanismus).
 *
 *  Hinweise:
 *  - Speichert Token & Ablaufzeit verschlüsselt per EncryptedSharedPreferences.
 *  - Verhindert gleichzeitige Mehrfach-Refreshes via @Volatile + synchronized.
 */
class AuthTokenInterceptor(
    context: Context,
    private val apiKey: String = "jobboerse-jobsuche"
) : Interceptor {

    private val appCtx = context.applicationContext

    /** vermeidet parallele Refresh-Calls in Mehr-Thread-Situationen */
    @Volatile
    private var isRefreshing = false

    private val prefs by lazy {
        // <-- sichere Preferences (MasterKey wird von Jetpack erzeugt)
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            appCtx,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        // ---------- 1) ursprüngliche Anfrage mit API-Key & evtl. Token aufbauen ----------
        val originalRequest = chain.request()
        val originalBuilder = originalRequest.newBuilder()
            .addHeader("X-API-Key", apiKey)                        // fester Schlüssel

        val token = getToken()                                     // ggf. leer
        if (token.isNotBlank()) originalBuilder.addHeader("Authorization", "Bearer $token")

        var request = originalBuilder.build()
        var response = chain.proceed(request)

        // ---------- 2) Token erneuern, falls 401 oder Ablauf ----------
        if (response.code == 401 && !isTokenEndpoint(request)) {
            response.close()   // wichtig: erste Response schließen

            synchronized(this) {
                if (!isRefreshing) {
                    isRefreshing = true
                    try {
                        refreshTokenSynchronously()                // kann 200-300 ms dauern
                    } finally {
                        isRefreshing = false
                    }
                }
            }

            // ---------- 3) Wiederholungsanfrage mit neuem Token ----------
            val newToken = getToken()
            val retryRequest = request.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $newToken")
                .build()

            response = chain.proceed(retryRequest)                 // zweiter Versuch
        }

        return response
    }

    /* -----------------------------------------------------------
       Hilfsfunktionen
       ----------------------------------------------------------- */

    /** Prüft, ob URL bereits der Token-Endpoint ist → sonst Endlosschleife */
    private fun isTokenEndpoint(request: Request): Boolean =
        request.url.encodedPath.endsWith("/oauth/gettoken_cc")

    /** true, wenn Ablaufzeit überschritten */
    private fun tokenNeedsRefresh(): Boolean =
        System.currentTimeMillis() > prefs.getLong(KEY_EXPIRY, 0L)

    /** Holt gespeicherten Token (oder leeren String) */
    private fun getToken(): String = prefs.getString(KEY_TOKEN, "") ?: ""

    /**
     * Ruft synchron den Client-Credentials-Flow auf.
     *   grant_type=client_credentials
     */
    private fun refreshTokenSynchronously() {
        // Doppelt prüfen, um unnötige Calls zu vermeiden
        if (!tokenNeedsRefresh()) return

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", "c003a37f-024f-462a-b36d-b001be4cd24a")
            .add("client_secret", "32a39620-32b3-4307-9aa1-511e3d7f48a8")
            .add("scope", "jobsuche openid")
            .build()

        val request = Request.Builder()
            .url("https://rest.arbeitsagentur.de/oauth/gettoken_cc")
            .addHeader("X-API-Key", apiKey)     // <-- auch hier mitgeben
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e(TAG, "Token-Refresh fehlgeschlagen (${resp.code})")
                    return
                }
                val json = JSONObject(resp.body?.string().orEmpty())
                val accessToken = json.getString("access_token")
                val expiresIn = json.getLong("expires_in")     // Sekunden
                saveToken(
                    accessToken,
                    System.currentTimeMillis() + expiresIn * 1_000L
                )
            }
        } catch (ioe: IOException) {
            Log.e(TAG, "IO-Fehler beim Token-Refresh: ${ioe.message}")
        }
    }

    private fun saveToken(token: String, expiryMillis: Long) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putLong(KEY_EXPIRY, expiryMillis)
        }.apply()
    }

    companion object {
        private const val TAG = "AuthTokenInterceptor"
        private const val KEY_TOKEN = "token"
        private const val KEY_EXPIRY = "expiry_date_millis"
    }
}