package com.example.promigrate.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.promigrate.data.local.UserDatabase
import com.example.promigrate.data.model.UserProfile
import java.util.Locale

const val TAG = "Repository"
class Repository (context: Context) {

    companion object {
        private var INSTANCE: Repository? = null

        fun getInstance(context: Context): Repository {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Repository(context)
                }
            }
            return INSTANCE!!
        }
    }

    private val db = UserDatabase.getDatabase(context)
    private val userProfileDao = db.userProfileDao()

    suspend fun saveUserProfile(userProfile: UserProfile) {
        try {
            userProfileDao.insertOrUpdateUserProfile(userProfile)
            Log.d(
                TAG, "Benutzerprofil erfolgreich gespeichert:" +
                    " ${userProfile.userId}")
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Speichern des Benutzerprofils", e)
        }
    }

    fun getUserProfile(userId: String): LiveData<UserProfile> {
        return try {
            val userProfile = userProfileDao.getUserProfile(userId)
            Log.d(TAG, "Benutzerprofil erfolgreich geladen: $userId")
            userProfile
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Laden des Benutzerprofils", e)
            MutableLiveData()
        }
    }



    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

    fun saveLanguageSetting(languageCode: String) {
        try {
            sharedPreferences.edit().putString("SelectedLanguage", languageCode).apply()
            Log.d(TAG, "Spracheinstellung gespeichert: $languageCode")
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Speichern der Spracheinstellung", e)
        }
    }

    fun loadLanguageSetting(): String {
        return try {
            val languageSetting = sharedPreferences.getString("SelectedLanguage",
                Locale.getDefault().language) ?: Locale.getDefault().language
            Log.d(TAG, "Geladene Spracheinstellung: $languageSetting")
            languageSetting
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Laden der Spracheinstellung", e)
            Locale.getDefault().language
        }
    }
}
