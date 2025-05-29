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
import com.example.promigrate.data.remote.ProMigrateCourseAPIService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Die Repository-Klasse dient als zentrale Anlaufstelle für die Datenverarbeitung in der Anwendung.
 * Sie kommuniziert mit verschiedenen Datenquellen (FirebaseAuth, Firestore, ProMigrateAPIService, DeepLApiService, ProMigrateCourseAPIService)
 * und stellt Methoden zur Verfügung, um Daten zu lesen, zu schreiben und zu aktualisieren.
 *
 * @param context: Der Kontext der Anwendung.
 * @param firebaseAuth: Eine Instanz von FirebaseAuth zur Authentifizierung von Benutzern.
 * @param firestore: Eine Instanz von FirebaseFirestore zur Interaktion mit der Firestore-Datenbank.
 * @param apiService: Eine Instanz von ProMigrateAPIService zur Interaktion mit der ProMigrate-API.
 * @param deepLApiService: Eine Instanz von DeepLApiService zur Interaktion mit der DeepL-API.
 * @param courseAPIService: Eine Instanz von ProMigrateCourseAPIService zur Interaktion mit der ProMigrateCourse-API.
 */
class Repository(
    context: Context,
    val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val apiService: ProMigrateAPIService,
    private val deepLApiService: DeepLApiService,
    private val courseAPIService: ProMigrateCourseAPIService
) {


    // Eine Instanz von FirebaseStorage, die zum Hochladen und Herunterladen von Dateien von Firebase Cloud Storage verwendet wird.
    private val storage = FirebaseStorage.getInstance()

    // Eine Instanz von SharedPreferences, die zum Speichern und Abrufen von einfachen Anwendungseinstellungen verwendet wird.
    // Die Einstellungen werden in einer Datei namens "AppSettings" im privaten Modus gespeichert,
    // d.h. sie sind nur für diese Anwendung zugänglich.
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)


    companion object {
        // Singleton-Instanz der Repository-Klasse
        private var INSTANCE: Repository? = null

        /**
         * Gibt eine Singleton-Instanz der Repository-Klasse zurück.
         * Wenn die Instanz noch nicht initialisiert wurde, wird sie mit den gegebenen Parametern erstellt.
         *
         * @param context: Der Kontext der Anwendung.
         * @param firebaseAuth: Eine Instanz von FirebaseAuth zur Authentifizierung von Benutzern.
         * @param firestore: Eine Instanz von FirebaseFirestore zur Interaktion mit der Firestore-Datenbank.
         * @param apiService: Eine Instanz von ProMigrateAPIService zur Interaktion mit der ProMigrate-API.
         * @param deepLApiService: Eine Instanz von DeepLApiService zur Interaktion mit der DeepL-API.
         * @param langLearnAPIService: Eine Instanz von ProMigrateCourseAPIService zur Interaktion mit der ProMigrateCourse-API.
         * @return: Eine Singleton-Instanz der Repository-Klasse.
         */
        fun getInstance(
            context: Context,
            firebaseAuth: FirebaseAuth,
            firestore: FirebaseFirestore,
            apiService: ProMigrateAPIService,
            deepLApiService: DeepLApiService,
            langLearnAPIService: ProMigrateCourseAPIService
        ):
                Repository {
            return INSTANCE ?: Repository(
                context,
                firebaseAuth,
                firestore,
                apiService,
                deepLApiService,
                langLearnAPIService
            ).also { INSTANCE = it }
        }
    }

    /**
     * Lädt die Spracheinstellung des Benutzers aus den SharedPreferences.
     * Wenn keine Spracheinstellung gefunden wird, wird die Standardsprache des Systems zurückgegeben.
     *
     * @return Die gespeicherte Spracheinstellung des Benutzers oder die Standardsprache des Systems.
     */
    suspend fun loadLanguageSetting(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val languageSetting = sharedPreferences.getString(
                "SelectedLanguage",
                Locale.getDefault().language
            ) ?: Locale.getDefault().language

            languageSetting
        } catch (e: Exception) {

            Locale.getDefault().language
        }
    }


    /**
     * Speichert die Spracheinstellung des Benutzers in den SharedPreferences.
     * Die Einstellung wird unter dem Schlüssel "SelectedLanguage" gespeichert.
     *
     * @param languageCode: Der Sprachcode, der gespeichert werden soll.
     */
    suspend fun saveLanguageSetting(languageCode: String) = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit().putString("SelectedLanguage", languageCode).apply()
        } catch (_: Exception) {
        }
    }


    /**
     * Übersetzt einen gegebenen Text in die angegebene Zielsprache mithilfe eines Übersetzungsdienstes (z. B. DeepL API).
     * Diese Funktion wird asynchron ausgeführt und liefert das Ergebnis der Übersetzung zurück.
     * Im Fehlerfall wird null zurückgegeben und ein entsprechender Fehler wird geloggt.
     *
     * @param text: Der Text, der übersetzt werden soll.
     * @param targetLanguage: Der Sprachcode der Zielsprache, in die der Text übersetzt werden soll.
     * @return: Ein Objekt vom Typ TranslationResult, das das Ergebnis der Übersetzung enthält, oder null bei einem Fehler.
     */
    suspend fun translateText(text: String, targetLanguage: String): TranslationResult? = withContext(
        Dispatchers.IO) {
        Log.d("translateText", "Übersetzung startet: Text = $text, Zielsprache = $targetLanguage")
        return@withContext try {
            // Ruft den Übersetzungsdienst mit dem gegebenen Text und Zielsprachcode auf.
            val response =
                deepLApiService.translateText(TranslationRequest(listOf(text), targetLanguage))
            Log.d("translateText", "Übersetzung erfolgreich, Antwort = $response")
            // Gibt das erste Übersetzungsergebnis zurück, da hier immer nur ein Text übersetzt wird.
            response.translations.first() // Gibt das erste Übersetzungsergebnis zurück
        } catch (e: Exception) {
            Log.e("translateText", "Fehler bei der Übersetzung", e)
            null
        }
    }



    /**
     * Holt das Benutzerprofil für die gegebene Benutzer-ID aus der Firestore-Datenbank.
     * Wenn das Dokument existiert, wird es in ein Profile-Objekt konvertiert und in einem MutableLiveData-Objekt gespeichert.
     * Das MutableLiveData-Objekt wird dann zurückgegeben, sodass es beobachtet werden kann.
     *
     * @param userId: Die eindeutige Benutzer-ID, für die das Profil abgerufen werden soll.
     * @return Ein MutableLiveData-Objekt, das das abgerufene Benutzerprofil enthält.
     */
    suspend fun getUserProfile(userId: String): MutableLiveData<Profile?> = withContext(Dispatchers.IO) {
        val userProfileLiveData = MutableLiveData<Profile?>()

        firestore.collection("user").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Versucht, das Dokument in ein Profile-Objekt zu konvertieren
                    val userProfile = documentSnapshot.toObject(Profile::class.java)
                    userProfileLiveData.value = userProfile
                }
            }


        return@withContext userProfileLiveData
    }


    /**
     * Erstellt ein neues Benutzerprofil in der Firestore-Datenbank.
     *
     * Diese Methode speichert ein Profile-Objekt in der Firestore-Datenbank unter der
     * spezifischen Benutzer-ID.
     *
     * @param userId: Die eindeutige Benutzer-ID, unter der das Profil gespeichert wird.
     * @param profile: Das Profile-Objekt, das in der Datenbank gespeichert werden soll.
     */
    suspend fun createUserProfile(userId: String, profile: Profile) = withContext(Dispatchers.IO) {
        val profileRef = firestore.collection("user").document(userId)
        profileRef.set(profile)
    }


    /**
     * Lädt ein Profilbild hoch und aktualisiert das Benutzerprofil in der Firestore-Datenbank mit der URL des hochgeladenen Bildes.
     * Diese Funktion wird asynchron ausgeführt und gibt die URL des hochgeladenen Bildes zurück.
     * Im Fehlerfall wird eine Ausnahme ausgelöst.
     *
     * @param uri: Die Uri des hochzuladenden Bildes.
     * @param userId: Die eindeutige Benutzer-ID, für die das Profilbild aktualisiert werden soll.
     * @return Die URL des hochgeladenen Bildes.
     * @throws Exception: Wenn der Upload fehlschlägt oder ein anderer Fehler auftritt.
     */
    suspend fun uploadAndUpdateProfilePicture(uri: Uri, userId: String): String = withContext(Dispatchers.IO) {
        val imageRef = storage.reference.child("images/$userId/profilePicture")
        try {
            val uploadTask = imageRef.putFile(uri).await()
            if (uploadTask.task.isSuccessful) {
                val imageUrl = imageRef.downloadUrl.await().toString()
                firestore.collection("user").document(userId).update("profilePicture", imageUrl)
                    .await()
                return@withContext imageUrl // Gibt die URL des hochgeladenen und aktualisierten Bildes zurück
            } else {
                throw Exception("Upload fehlgeschlagen")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Aktualisiert das Benutzerprofil in der Firestore-Datenbank mit den bereitgestellten Profildaten.
     * Diese Funktion wird asynchron ausgeführt und wirft eine Ausnahme, wenn ein Fehler auftritt.
     *
     * @param userId: Die eindeutige Benutzer-ID, für die das Profil aktualisiert werden soll.
     * @param profileData: Eine Map von Schlüssel-Wert-Paaren, die die zu aktualisierenden Profildaten repräsentieren.
     * @throws Exception: Wenn ein Fehler beim Aktualisieren des Profils auftritt.
     */
    suspend fun updateUserProfile(userId: String, profileData: Map<String, Any>): Void = withContext(Dispatchers.IO) {
        try {
            firestore.collection("user").document(userId).update(profileData).await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Aktualisiert ein bestimmtes Feld des Benutzerprofils in der Firestore-Datenbank mit dem bereitgestellten Wert.
     * Diese Funktion wird asynchron ausgeführt.
     *
     * @param userId: Die eindeutige Benutzer-ID, für die das Profil aktualisiert werden soll.
     * @param field: Der Name des Feldes, das aktualisiert werden soll.
     * @param value: Der neue Wert, der in das Feld geschrieben werden soll.
     */
    suspend fun updateUserProfileField(userId: String, field: String, value: Any) = withContext(Dispatchers.IO) {
        firestore.collection("user").document(userId).update(field, value)
    }


    /**
     * Ruft die Berufsfelder von der ProMigrate-API ab.
     * Diese Funktion wird asynchron ausgeführt und gibt ein Result-Objekt zurück, das eine Liste von Berufsfeldern enthält.
     * Im Erfolgsfall enthält das Result-Objekt eine Liste der abgerufenen Berufsfelder.
     * Im Fehlerfall enthält das Result-Objekt eine Ausnahme mit einer Fehlermeldung.
     *
     * @return Ein Result-Objekt, das entweder eine Liste von Berufsfeldern oder eine Ausnahme enthält.
     * @throws Exception: Wenn ein Fehler beim Abrufen der Berufsfelder auftritt.
     */
    suspend fun getOccupationalFields(): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getOccupationalFields()
            if (response.isSuccessful && response.body() != null) {
                val berufsfelderListe = response.body()!!.facetten.berufsfeld.counts.keys.toList()
                Result.success(berufsfelderListe)
            } else {
                Result.failure(Exception("Fehler beim Abrufen der Berufsfelder: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Ruft die Arbeitsorte von der ProMigrate-API ab.
     * Diese Funktion wird asynchron ausgeführt und gibt ein Result-Objekt zurück, das eine Liste von Arbeitsorten enthält.
     * Im Erfolgsfall enthält das Result-Objekt eine Liste der abgerufenen Arbeitsorte.
     * Im Fehlerfall enthält das Result-Objekt eine Ausnahme mit einer Fehlermeldung.
     *
     * @return Ein Result-Objekt, das entweder eine Liste von Arbeitsorten oder eine Ausnahme enthält.
     * @throws Exception: Wenn ein Fehler beim Abrufen der Arbeitsorte auftritt.
     */
    suspend fun getWorkLocations(): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getWorkLocations()
            if (response.isSuccessful && response.body() != null) {
                val arbeitsorteListe = response.body()!!.facetten.arbeitsort.counts.keys.toList()
                Result.success(arbeitsorteListe)
            } else {
                Result.failure(Exception("Fehler beim Abrufen der Arbeitsorte: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ruft die Jobs von der ProMigrate-API ab, basierend auf dem gegebenen Berufsfeld und Arbeitsort.
     * Diese Funktion wird asynchron ausgeführt und gibt ein Result-Objekt zurück, das eine Liste von Jobs enthält.
     * Im Erfolgsfall enthält das Result-Objekt eine Liste der abgerufenen Jobs.
     * Im Fehlerfall enthält das Result-Objekt eine Ausnahme mit einer Fehlermeldung.
     *
     * @param berufsfeld: Das Berufsfeld, für das die Jobs abgerufen werden sollen.
     * @param arbeitsort: Der Arbeitsort, für den die Jobs abgerufen werden sollen.
     * @return Ein Result-Objekt, das entweder eine Liste von Jobs oder eine Ausnahme enthält.
     * @throws Exception: Wenn ein Fehler beim Abrufen der Jobs auftritt.
     */
    suspend fun getJobs(berufsfeld: String, arbeitsort: String): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getJobs(berufsfeld = berufsfeld, wo = arbeitsort)

            if (response.isSuccessful && response.body() != null) {
                // Verwendet ein Set, um Duplikate zu vermeiden
                val uniqueJobs = response.body()!!.stellenangebote.mapNotNull { it.beruf }.toSet()
                // Konvertiert das Set zurück in eine Liste, um das Ergebnis zurückzugeben
                val berufsListe = uniqueJobs.toList()
                Result.success(berufsListe)
            } else {

                Result.failure(Exception("Fehler beim Abrufen der Jobs: ${response.message()}"))
            }
        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    /**
     * Ruft die Jobangebote von der ProMigrate-API ab, basierend auf dem gegebenen Berufsfeld und Arbeitsort.
     * Diese Funktion wird asynchron ausgeführt und gibt ein Result-Objekt zurück, das eine Liste von Paaren enthält,
     * wobei jedes Paar aus dem Titel des Jobs und seiner Referenznummer besteht.
     * Im Erfolgsfall enthält das Result-Objekt eine Liste der abgerufenen Jobangebote.
     * Im Fehlerfall enthält das Result-Objekt eine Ausnahme mit einer Fehlermeldung.
     *
     * @param was: Das Berufsfeld, für das die Jobangebote abgerufen werden sollen.
     * @param arbeitsort: Der Arbeitsort, für den die Jobangebote abgerufen werden sollen.
     * @return Ein Result-Objekt, das entweder eine Liste von Paaren (Titel des Jobs, Referenznummer) oder eine Ausnahme enthält.
     * @throws Exception: Wenn ein Fehler beim Abrufen der Jobangebote auftritt.
     */
    suspend fun getJobOffers(was: String, arbeitsort: String): Result<List<Pair<String, String>>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getJobOffers(was, wo = arbeitsort)

            if (response.isSuccessful && response.body() != null) {

                val jobPairs = response.body()!!.stellenangebote.mapNotNull {
                    if (it.titel != null) it.titel to it.refnr else null
                }
                Result.success(jobPairs)
            } else {

                Result.failure(Exception("Fehler beim Abrufen der Jobs: ${response.message()}"))
            }
        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    /**
     * Ruft die Jobdetails von der ProMigrate-API ab, basierend auf der gegebenen codierten HashID.
     * Diese Funktion wird asynchron ausgeführt und gibt ein Result-Objekt zurück, das die Jobdetails enthält.
     * Im Erfolgsfall enthält das Result-Objekt die abgerufenen Jobdetails.
     * Im Fehlerfall enthält das Result-Objekt eine Ausnahme mit einer Fehlermeldung.
     *
     * @param encodedHashID: Die codierte HashID des Jobs, für den die Details abgerufen werden sollen.
     * @return Ein Result-Objekt, das entweder die Jobdetails oder eine Ausnahme enthält.
     * @throws Exception: Wenn ein Fehler beim Abrufen der Jobdetails auftritt.
     */
    suspend fun getJobDetails(encodedHashID: String): Result<JobDetailsResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Kodiert die RefNr in Base64
            val base64EncodedHashID =
                Base64.encodeToString(encodedHashID.toByteArray(charset("UTF-8")), Base64.NO_WRAP)

            val response = apiService.getJobDetails(base64EncodedHashID)

            if (response.isSuccessful && response.body() != null) {
                val jobDetail = response.body()
                jobDetail?.let {
                    Result.success(it)
                }
                    ?: Result.failure(Exception("Jobdetails nicht gefunden für RefNr: $encodedHashID"))
            } else {

                Result.failure(Exception("Fehler beim Abrufen der Jobdetails für RefNr: $encodedHashID, Fehler: ${response.message()}"))
            }
        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    /**
     * Ruft die Bildungsangebote von der ProMigrateCourse-API ab, basierend auf den gegebenen Parametern.
     * Diese Funktion wird asynchron ausgeführt und gibt ein Result-Objekt zurück, das eine Liste von TerminResponse-Objekten enthält.
     * Im Erfolgsfall enthält das Result-Objekt eine Liste der abgerufenen Bildungsangebote.
     * Im Fehlerfall enthält das Result-Objekt eine Ausnahme mit einer Fehlermeldung.
     *
     * @param systematiken: Die Systematiken, für die die Bildungsangebote abgerufen werden sollen.
     * @param orte: Die Orte, für die die Bildungsangebote abgerufen werden sollen.
     * @param sprachniveau: Das Sprachniveau, für das die Bildungsangebote abgerufen werden sollen.
     * @param beginntermine: Die Beginntermine, für die die Bildungsangebote abgerufen werden sollen.
     * @return Ein Result-Objekt, das entweder eine Liste von TerminResponse-Objekten oder eine Ausnahme enthält.
     * @throws Exception: Wenn ein Fehler beim Abrufen der Bildungsangebote auftritt.
     */
    suspend fun getEducationalOffers(
        systematiken: String?,
        orte: String,
        sprachniveau: String,
        beginntermine: Int
    ): Result<List<TerminResponse>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = courseAPIService.getEducationalOffer(
                systematiken,
                orte,
                sprachniveau,
                beginntermine,
                "basc"
            )
            if (response.isSuccessful) {
                val termine = response.body()?._embedded?.termine ?: listOf()
                Result.success(termine)
            } else {

                Result.failure(Exception("Fehler beim Abrufen der Bildungsangebote: ${response.message()}"))
            }
        } catch (e: Exception) {

            Result.failure(e)
        }
    }


}
