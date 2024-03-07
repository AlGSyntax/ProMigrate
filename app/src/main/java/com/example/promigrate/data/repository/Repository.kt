package com.example.promigrate.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.promigrate.data.model.JobDetailsResponse
import com.example.promigrate.data.model.Profile
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.data.model.TranslationRequest
import com.example.promigrate.data.model.TranslationResult
import com.example.promigrate.data.remote.DeepLApiService
import com.example.promigrate.data.remote.ProMigrateAPIService
import com.example.promigrate.data.remote.ProMigrateLangLearnAPIService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Locale

class Repository (context: Context, val firebaseAuth: FirebaseAuth,
                  private val firestore: FirebaseFirestore,private val apiService: ProMigrateAPIService,
                  private val deepLApiService: DeepLApiService,private val langLearnAPIService: ProMigrateLangLearnAPIService
) {


    private val TAG = "Repository"
    private val storage = FirebaseStorage.getInstance()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)//

    companion object {
        private var INSTANCE: Repository? = null

        fun getInstance(context: Context, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore,
                        apiService: ProMigrateAPIService,deepLApiService: DeepLApiService, langLearnAPIService: ProMigrateLangLearnAPIService):
                Repository {
            return INSTANCE ?: Repository(context, firebaseAuth, firestore,apiService,deepLApiService, langLearnAPIService).also { INSTANCE = it }
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


    fun saveLanguageSetting(languageCode: String) {
        try {
            sharedPreferences.edit().putString("SelectedLanguage", languageCode).apply()
            Log.d(TAG, "Spracheinstellung gespeichert: $languageCode")
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Speichern der Spracheinstellung", e)
        }
    }



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





    fun createUserProfile(userId: String, profile: Profile) {
        val profileRef = firestore.collection("user").document(userId)
        profileRef.set(profile)
            .addOnSuccessListener { Log.d(TAG, "Profil erfolgreich erstellt.") }
            .addOnFailureListener { e -> Log.e(TAG, "Fehler beim Erstellen des Profils", e) }
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

    fun updateUserProfileField(userId: String, field: String, value: Any) {
        // Beispiel-Implementierung, passe sie entsprechend deiner Datenstruktur an
        firestore.collection("user").document(userId).update(field, value)
    }






    suspend fun getBerufsfelder(): Result<List<String>> {
        return try {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "Starte Abruf der Berufsfelder um: $startTime")

            val response = apiService.getBerufsfelder()
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "Berufsfelder-Anfrage abgeschlossen um: $endTime, Dauer: ${endTime - startTime} ms")

            if (response.isSuccessful && response.body() != null) {
                val berufsfelderListe = response.body()!!.facetten.berufsfeld.counts.keys.toList()
                Log.d(TAG, "Berufsfelder erfolgreich abgerufen: ${response.body()}, HTTP Status Code: ${response.code()}")
                Result.success(berufsfelderListe)
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Berufsfelder: ${response.message()}, HTTP Status Code: ${response.code()}, Response Body: ${response.errorBody()?.string()}")
                Result.failure(Exception("Fehler beim Abrufen der Berufsfelder: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme beim Abrufen der Berufsfelder", e)
            Result.failure(e)
        }
    }


    suspend fun getArbeitsorte(): Result<List<String>> {
        return try {
            Log.d(TAG, "Starte Abruf der Arbeitsorte.")
            val response = apiService.getArbeitsorte()
            if (response.isSuccessful && response.body() != null) {
                Log.e(TAG, "Arbeitsorte erfolgreich abgerufen: ${response.body()}")
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
                    if (it.titel != null) it.titel to it.refnr else null
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



    suspend fun getJobDetails(encodedHashID: String): Result<JobDetailsResponse> {
        return try {
            // Kodiere die HashID in Base64
            val base64EncodedHashID = Base64.encodeToString(encodedHashID.toByteArray(charset("UTF-8")), Base64.NO_WRAP)

            val response = apiService.getJobDetails(base64EncodedHashID)
            Log.d(TAG, "API-Antwort: ${response.raw()}")
            if (response.isSuccessful && response.body() != null) {
                val jobDetail = response.body()
                jobDetail?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Jobdetails nicht gefunden für HashID: $encodedHashID"))
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobdetails für HashID: $encodedHashID, Fehler: ${response.message()}")
                Result.failure(Exception("Fehler beim Abrufen der Jobdetails für HashID: $encodedHashID, Fehler: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme beim Abrufen der Jobdetails für HashID: $encodedHashID, Ausnahme: ${e.message}")
            Result.failure(e)
        }
    }

// ... other code ...

    suspend fun getBildungsangebote(
        systematiken: String?,
        orte: String,
        sprachniveau: String,
        beginntermine: Int
    ): Result<List<TerminResponse>> {
        return try {
            val response = langLearnAPIService.getBildungsangebot(systematiken, orte, sprachniveau, beginntermine, "basc")
            if (response.isSuccessful) {
                val termine = response.body()?._embedded?.termine ?: listOf()
                Result.success(termine)
            } else {
                Log.e("Repository", "Fehler beim Abrufen der Bildungsangebote: ${response.message()}")
                Result.failure(Exception("Fehler beim Abrufen der Bildungsangebote: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("Repository", "Exception beim Abrufen der Bildungsangebote", e)
            Result.failure(e)
        }
    }



// ... other code ...















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
