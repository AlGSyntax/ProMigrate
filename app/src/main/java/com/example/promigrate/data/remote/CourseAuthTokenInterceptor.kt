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

    private val applicationContext = context.applicationContext
    private val sharedPrefsName = "LanguageCourseTokenPrefs"
    private val tokenKey = "languageCourseToken"
    private val expiryKey = "languageCourseTokenExpiryDateMilli"

    // Sicherheitsmarge für die Token-Erneuerung (z.B. 5 Minuten vor Ablauf)
    private val tokenRefreshSafetyMarginMillis = 5 * 60 * 1000L

    override fun intercept(chain: Interceptor.Chain): Response {
        // Synchronisierungsblock, um Race Conditions bei parallelen Anfragen zu vermeiden,
        // die gleichzeitig eine Token-Erneuerung auslösen könnten.
        synchronized(this) {
            if (checkIfTokenNeedsRefresh(applicationContext)) {
                Log.d("CourseAuthTokenInterceptor", "Token needs refresh. Attempting to refresh synchronously.")
                val refreshedSuccessfully = refreshTokenSynchronously(applicationContext)
                if (refreshedSuccessfully) {
                    Log.d("CourseAuthTokenInterceptor", "Token was refreshed successfully.")
                } else {
                    Log.e("CourseAuthTokenInterceptor", "Token refresh failed. Proceeding with old/no token.")
                    // Optional: Hier könnten Sie entscheiden, die Anfrage fehlschlagen zu lassen,
                    // wenn der Token nicht erneuert werden konnte und ein Token zwingend erforderlich ist.
                    // z.B. return Response.Builder()....code(401)...build()
                }
            }
        }

        val currentToken = getToken(applicationContext)
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // API erwartet 'OAuthAccessToken' im Header oder alternativ 'X-API-Key'.
        // Wir verwenden hier den OAuthAccessToken.
        if (currentToken.isNotEmpty()) {
            // ACHTUNG: Der Header-Name wurde von "Authorization" auf "OAuthAccessToken" geändert,
            // gemäß API-Dokumentation. Das "Bearer " Präfix wird hier nicht verwendet,
            // da die API es scheinbar nicht erwartet, wenn der Header-Name 'OAuthAccessToken' ist.
            // Bitte prüfen Sie ggf. nochmals genau die Server-Anforderung, ob der Wert nur der Token sein soll
            // oder "Bearer <token>". Die Doku sagt "'OAuthAccessToken' inkludieren", was meist nur den Token meint.
            requestBuilder.header("OAuthAccessToken", currentToken)
            Log.d("CourseAuthTokenInterceptor", "Added OAuthAccessToken header to the request.")
        } else {
            // Alternative: X-API-Key verwenden, falls kein Token vorhanden oder gewünscht.
            // val clientId = "bd24f42e-ad0b-4005-b834-23bb6800dc6c"
            // requestBuilder.header("X-API-Key", clientId)
            // Log.d("CourseAuthTokenInterceptor", "No OAuth token, attempting to use X-API-Key (currently commented out).")
            Log.w("CourseAuthTokenInterceptor", "Proceeding without OAuthAccessToken as it's empty.")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }

    private fun checkIfTokenNeedsRefresh(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val expiryDateMillis = sharedPref.getLong(expiryKey, 0L)

        // Token muss erneuert werden, wenn:
        // 1. Kein Ablaufdatum gespeichert ist (expiryDateMillis == 0L) -> initialer Zustand oder Fehler
        // 2. Die aktuelle Zeit nach dem (Ablaufdatum - Sicherheitsmarge) liegt
        val needsRefresh = expiryDateMillis == 0L || System.currentTimeMillis() >= (expiryDateMillis - tokenRefreshSafetyMarginMillis)
        if (needsRefresh) {
            Log.i("CourseAuthTokenInterceptor", "Token check: Needs refresh. Expiry: $expiryDateMillis, Current: ${System.currentTimeMillis()}")
        } else {
            Log.d("CourseAuthTokenInterceptor", "Token check: Does not need refresh. Expiry: $expiryDateMillis, Current: ${System.currentTimeMillis()}")
        }
        return needsRefresh
    }

    /**
     * Versucht, den Token synchron zu erneuern.
     * @return true, wenn der Token erfolgreich erneuert und gespeichert wurde, sonst false.
     */
    private fun refreshTokenSynchronously(context: Context): Boolean {
        // Verwenden Sie einen eigenen OkHttpClient für den Token-Refresh,
        // um nicht in eine Endlosschleife mit dem Interceptor selbst zu geraten.
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", "bd24f42e-ad0b-4005-b834-23bb6800dc6c") // Ihre Client ID
            .add("client_secret", "6776b89e-5728-4643-8cd5-c93aefb5314b") // Ihr Client Secret
            .build()

        val request = Request.Builder()
            .url("https://rest.arbeitsagentur.de/oauth/gettoken_cc")
            .post(requestBody)
            .build()

        try {
            // Führe den Call synchron aus
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBodyString = response.body?.string()
                if (responseBodyString != null) {
                    val jsonObject = JSONObject(responseBodyString)
                    val token = jsonObject.optString("access_token")
                    val expiresIn = jsonObject.optLong("expires_in") // in Sekunden

                    if (token.isNotEmpty() && expiresIn > 0) {
                        // Korrekte Berechnung der Ablaufzeit: aktuelle Zeit + (expires_in * 1000)
                        // `expires_in` ist in Sekunden, daher * 1000 für Millisekunden.
                        val newExpiryDateMillis = System.currentTimeMillis() + (expiresIn * 1000L)
                        saveTokenAndExpiryDate(context, token, newExpiryDateMillis)
                        Log.i("CourseAuthTokenInterceptor", "Token refreshed. New token: ${token.substring(0, Math.min(token.length,10))}..., New expiry: $newExpiryDateMillis")
                        return true
                    } else {
                        Log.e("CourseAuthTokenInterceptor", "Token refresh failed: access_token or expires_in missing in response. Body: $responseBodyString")
                    }
                } else {
                    Log.e("CourseAuthTokenInterceptor", "Token refresh failed: Response body was null.")
                }
            } else {
                // Detailliertere Fehlerbehandlung für nicht erfolgreiche Antworten
                val errorBody = response.body?.string() ?: "No error body"
                Log.e("CourseAuthTokenInterceptor", "Token refresh failed: Unsuccessful response. Code: ${response.code}, Message: ${response.message}, Body: $errorBody")
            }
        } catch (e: IOException) {
            Log.e("CourseAuthTokenInterceptor", "Token refresh failed: IOException: ${e.message}", e)
        } catch (e: org.json.JSONException) {
            Log.e("CourseAuthTokenInterceptor", "Token refresh failed: JSONException: ${e.message}", e)
        }
        return false
    }

    private fun getToken(context: Context): String {
        val sharedPref = context.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        return sharedPref.getString(tokenKey, "") ?: ""
    }

    private fun saveTokenAndExpiryDate(context: Context, token: String, expiryDateMillis: Long) {
        val sharedPref = context.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(tokenKey, token)
            putLong(expiryKey, expiryDateMillis)
            apply() // apply() ist asynchron und schneller als commit()
        }
        Log.d("CourseAuthTokenInterceptor", "Token and expiry date saved. Expiry: $expiryDateMillis")
    }
}
