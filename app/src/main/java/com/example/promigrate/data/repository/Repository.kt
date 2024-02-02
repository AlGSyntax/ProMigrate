package com.example.promigrate.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.promigrate.data.local.UserDatabase
import com.example.promigrate.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

const val TAG = "Repository"
class Repository (context: Context, private val firebaseAuth: FirebaseAuth,
                  private val firestore: FirebaseFirestore
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)//

    companion object {
        private var INSTANCE: Repository? = null

        fun getInstance(context: Context, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore):
                Repository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Repository(context, firebaseAuth, firestore).also { INSTANCE = it }
            }
        }
    }



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

    fun getUserProfile(userId: String): MutableLiveData<UserProfile?> {
        val userProfileLiveData = MutableLiveData<UserProfile?>()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userProfile = document.toObject(UserProfile::class.java)
                    userProfileLiveData.postValue(userProfile)
                } else {
                    Log.d(TAG, "Benutzerprofil nicht gefunden")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Fehler beim Laden des Benutzerprofils", e)
            }

        return userProfileLiveData
    }

    //TODO ALLE FIRESTORE METHODEN INS REPOSITORY!!!!

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








}
