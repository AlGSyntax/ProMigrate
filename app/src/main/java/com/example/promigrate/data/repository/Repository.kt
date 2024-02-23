package com.example.promigrate.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.promigrate.data.model.Job
import com.example.promigrate.data.model.Profile
import com.example.promigrate.data.model.TranslationRequest
import com.example.promigrate.data.model.TranslationResult
import com.example.promigrate.data.remote.DeepLApiService
import com.example.promigrate.data.remote.ProMigrateAPIService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Locale

const val TAG = "Repository"
class Repository (context: Context, val firebaseAuth: FirebaseAuth,
                  private val firestore: FirebaseFirestore,private val apiService: ProMigrateAPIService,
    private val deepLApiService: DeepLApiService
) {

    private val storage = FirebaseStorage.getInstance()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)//

    companion object {
        private var INSTANCE: Repository? = null

        fun getInstance(context: Context, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore,
                        apiService: ProMigrateAPIService,deepLApiService: DeepLApiService):
                Repository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Repository(context, firebaseAuth, firestore,apiService,deepLApiService).also { INSTANCE = it }
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

    fun getUserProfile(userId: String): MutableLiveData<Profile?> {
        val userProfileLiveData = MutableLiveData<Profile?>()

        firestore.collection("user").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Versuche, das Dokument in ein Profile-Objekt zu konvertieren
                    val userProfile = documentSnapshot.toObject(Profile::class.java)
                    userProfileLiveData.value = userProfile
                } else {
                    Log.d(TAG, "Benutzerprofil nicht gefunden")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Fehler beim Laden des Benutzerprofils", exception)
            }

        return userProfileLiveData
    }

    /**
    fun fetchUserProfile(): LiveData<Profile?> {
        val userProfileLiveData = MutableLiveData<Profile?>()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val docRef = FirebaseFirestore.getInstance().collection("user").document(userId)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                val userProfile = documentSnapshot.toObject<Profile>()
                userProfileLiveData.value = userProfile
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error fetching user profile", e)
                userProfileLiveData.value = null
            }
        } else {
            Log.w(TAG, "User ID is null, can't fetch user profile")
            userProfileLiveData.value = null
        }
        return userProfileLiveData
    }
    */



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

    fun updateUserProfileField(userId: String, field: String, value: Any) {
        // Beispiel-Implementierung, passe sie entsprechend deiner Datenstruktur an
        firestore.collection("user").document(userId).update(field, value)
    }


    /**
    suspend fun getJobs(was: String?, wo: String?, berufsfeld: String?): Result<JobResponse> {
        return try {
            val response = apiService.getJobs(was, wo, berufsfeld)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Jobs erfolgreich abgerufen: ${response.body()}")
                Result.success(response.body()!!)

            } else {
                Result.failure(RuntimeException("response error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */

    suspend fun translateText(text: String, targetLanguage: String): TranslationResult? {
        Log.d("translateText", "Übersetzung startet: Text = $text, Zielsprache = $targetLanguage")
        return try {
            val response = deepLApiService.translateText(TranslationRequest(listOf(text), targetLanguage))
            Log.d("translateText", "Übersetzung erfolgreich, Antwort = $response")
            response.translations.first() // Annahme, dass nur ein Text übersetzt wird
        } catch (e: Exception) {
            Log.e("translateText", "Fehler bei der Übersetzung", e)
            null
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

    suspend fun getArbeitsorte(): Result<List<String>> {
        return try {
            Log.d(TAG, "Starte Abruf der Arbeitsorte.")
            val response = apiService.getArbeitsorte()
            if (response.isSuccessful && response.body() != null) {
                // Hier wird angenommen, dass du nur die Arbeitsorte extrahieren möchtest.
                val arbeitsorteListe = response.body()!!.facetten.arbeitsort.counts.keys.toList()
                Log.d(TAG, "Arbeitsorte erfolgreich abgerufen: ${response.body()}")
                Result.success(arbeitsorteListe)
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Arbeitsorte: ${response.message()}")
                Result.failure(Exception("Fehler beim Abrufen der Arbeitsorte: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme beim Abrufen der Arbeitsorte: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getJobs(berufsfeld: String, arbeitsort: String): Result<List<String>> {
        return try {
            val response = apiService.getJobs(berufsfeld = berufsfeld, wo = arbeitsort)
            Log.d(TAG, "API-Antwort: ${response.raw()}")
            if (response.isSuccessful && response.body() != null) {
                // Verwende ein Set, um Duplikate zu vermeiden
                val uniqueJobs = response.body()!!.stellenangebote.mapNotNull { it.beruf }.toSet()
                // Konvertiere das Set zurück in eine Liste, um das Ergebnis zurückzugeben
                val berufsListe = uniqueJobs.toList()
                Result.success(berufsListe)
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobs: ${response.message()}")
                Result.failure(Exception("Fehler beim Abrufen der Jobs: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme beim Abrufen der Jobs: ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun getJobOffers(was: String, arbeitsort: String): Result<List<Pair<String, String>>> {
        return try {
            val response = apiService.getJobOffers(was, wo = arbeitsort)
            Log.d(TAG, "API-Antwort: ${response.raw()}")
            if (response.isSuccessful && response.body() != null) {
                // Erstelle eine Liste von Paaren aus Jobtitel und Hash-ID
                val jobPairs = response.body()!!.stellenangebote.mapNotNull {
                    if (it.titel != null) it.titel to it.hashId else null
                }
                Result.success(jobPairs)
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobs: ${response.message()}")
                Result.failure(Exception("Fehler beim Abrufen der Jobs: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme beim Abrufen der Jobs: ${e.message}")
            Result.failure(e)
        }
    }



    suspend fun getJobDetails(encodedHashID: String): Result<Job> {
        return try {
            val response = apiService.getJobDetails(encodedHashID)
            Log.d(TAG, "API-Antwort: ${response.raw()}")
            if (response.isSuccessful && response.body() != null) {
                // Wir nehmen an, dass die API eine ähnliche Antwort wie die Liste der Jobs zurückgibt,
                // aber in diesem Fall nur mit einem einzigen Job-Objekt in der 'stellenangebote' Liste.
                val jobDetail = response.body()!!.stellenangebote.firstOrNull()
                jobDetail?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Jobdetails nicht gefunden"))
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobdetails: ${response.message()}")
                Result.failure(Exception("Fehler beim Abrufen der Jobdetails: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme beim Abrufen der Jobdetails: ${e.message}")
            Result.failure(e)
        }
    }



















    /**
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
    */

}
