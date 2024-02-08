package com.example.promigrate.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.promigrate.data.local.UserDatabase
import com.example.promigrate.data.model.JobResponse
import com.example.promigrate.data.model.Profile
import com.example.promigrate.data.model.UserProfile
import com.example.promigrate.data.remote.ProMigrateAPIService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Locale

const val TAG = "Repository"
class Repository (context: Context, val firebaseAuth: FirebaseAuth,
                  private val firestore: FirebaseFirestore,private val apiService: ProMigrateAPIService
) {

    private val storage = FirebaseStorage.getInstance()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)//

    companion object {
        private var INSTANCE: Repository? = null

        fun getInstance(context: Context, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore,apiService: ProMigrateAPIService):
                Repository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Repository(context, firebaseAuth, firestore,apiService).also { INSTANCE = it }
            }
        }
    }



    fun setupUserEnvironment(firebaseUser: FirebaseUser) {
        val profileRef = FirebaseFirestore.getInstance().collection("user").document(firebaseUser.uid)
        // Hier könntest du weitere Setup-Aktionen durchführen oder die Referenzen bei Bedarf zurückgeben
        Log.d(TAG, "Benutzerumgebungseinrichtung erfolgreich für ${firebaseUser.uid}")
    }


    fun createUserProfile(userId: String, profile: Profile) {
        val profileRef = firestore.collection("user").document(userId)
        profileRef.set(profile)
            .addOnSuccessListener { Log.d(TAG, "Profil erfolgreich erstellt.") }
            .addOnFailureListener { e -> Log.e(TAG, "Fehler beim Erstellen des Profils", e) }
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

        firestore.collection("user").document(userId).get()
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


    suspend fun uploadAndUpdateProfilePicture(uri: Uri, userId: String): String {
        val imageRef = storage.reference.child("images/$userId/profilePicture")
        try {
            val uploadTask = imageRef.putFile(uri).await()
            if (uploadTask.task.isSuccessful) {
                val imageUrl = imageRef.downloadUrl.await().toString()
                firestore.collection("user").document(userId).update("profilePicture", imageUrl).await()
                return imageUrl // Gibt die URL des hochgeladenen und aktualisierten Bildes zurück
            } else {
                throw Exception("Upload fehlgeschlagen")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Hochladen und Aktualisieren des Profilbildes", e)
            throw e
        }
    }

    suspend fun updateUserProfile(userId: String, profileData: Map<String, Any>) {
        try {
            firestore.collection("user").document(userId).update(profileData).await()
            Log.d(TAG, "Profil erfolgreich aktualisiert")
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Aktualisieren des Profils", e)
            throw e
        }
    }


    suspend fun getJobs(was: String?, wo: String?, berufsfeld: String?): Result<JobResponse> {
        return try {
            val response = apiService.getJobs(was, wo, berufsfeld)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(RuntimeException("response error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBerufsfelder(): Result<List<String>> {
        return try {
            Log.d(TAG, "Starte Abruf der Berufsfelder.")
            val response = apiService.getBerufsfelder()
            if (response.isSuccessful && response.body() != null) {
                // Hier wird angenommen, dass du nur die Berufsfelder extrahieren möchtest.
                val berufsfelderListe = response.body()!!.facetten.berufsfeld.counts.keys.toList()
                Log.d(TAG, "Berufsfelder erfolgreich abgerufen: ${response.body()}")
                Result.success(berufsfelderListe)
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Berufsfelder: ${response.message()}")
                Result.failure(Exception("Fehler beim Abrufen der Berufsfelder: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme beim Abrufen der Berufsfelder: ${e.message}")
            Result.failure(e)
        }
    }



//TODO




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
