package com.example.promigrate

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.promigrate.data.model.IndexCard
import com.example.promigrate.data.model.JobDetailsResponse
import com.example.promigrate.data.model.Profile
import com.example.promigrate.data.model.RegistrationStatus
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.data.model.ToDoItemRelocation
import com.example.promigrate.data.remote.DeepLApiService
import com.example.promigrate.data.remote.ProMigrateAPI
import com.example.promigrate.data.remote.ProMigrateCourseAPI
import com.example.promigrate.data.repository.Repository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var repository = Repository.getInstance(
        application, FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance(), ProMigrateAPI.retrofitService, DeepLApiService.create(),
        ProMigrateCourseAPI.retrofitService

    )

    private val TAG = "MainViewModel"

    private val _localeList = MutableLiveData<LocaleListCompat>()
    val localeList: LiveData<LocaleListCompat> = _localeList

    private val _selectedLanguageCode = MutableLiveData<String>()
    val selectedLanguageCode: LiveData<String> = _selectedLanguageCode


    /**
     * Die MutableLiveData _loginStatus wird verwendet, um den Anmeldestatus intern im ViewModel zu halten.
     * Diese Variable ist privat, um die Datenkapselung zu gewährleisten.
     * Änderungen an _loginStatus sollen ausschließlich innerhalb des ViewModels erfolgen.
     *
     * Die LiveData loginStatus ist eine öffentlich zugängliche, unveränderliche Version von _loginStatus.
     * Sie erlaubt es den UI-Komponenten, auf Änderungen des Anmeldestatus zu reagieren, ohne den Status direkt ändern zu können.
     * Dies fördert eine klare Trennung von Zuständigkeiten zwischen dem ViewModel und der UI.
     */
    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> = _loginStatus

    private val _emailExists = MutableLiveData<Boolean>()
    val emailExists: LiveData<Boolean> = _emailExists

    private val auth = Firebase.auth


    private val _registrationStatus = MutableLiveData<RegistrationStatus>()
    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus


    private val _occupationalfields = MutableLiveData<List<String>>()
    val occupationalfields: LiveData<List<String>> = _occupationalfields

    private val _worklocations = MutableLiveData<List<String>>()
    val worklocations: LiveData<List<String>> = _worklocations


    private var _userProfileData = MutableLiveData<Profile?>()
    val userProfileData: LiveData<Profile?> = _userProfileData


    private val _jobs = MutableLiveData<List<String>>()
    val jobs: LiveData<List<String>> = _jobs


    private val _jobOffers = MutableLiveData<List<Pair<String, String>>>()
    val jobOffers: LiveData<List<Pair<String, String>>> = _jobOffers


    private val _jobDetails = MutableLiveData<Result<JobDetailsResponse>>()
    val jobDetails: LiveData<Result<JobDetailsResponse>> = _jobDetails

    private val _educationaloffers = MutableLiveData<List<TerminResponse>>()
    val educationaloffers: LiveData<List<TerminResponse>> = _educationaloffers

    private val _deleteAccountStatus = MutableLiveData<Boolean?>()
    val deleteAccountStatus: LiveData<Boolean?> = _deleteAccountStatus


    //TODO LiveData = UI-Aktualisierungen ,Berechnungen = lokale Variablen, slider Int lokal speichern


    init {
        loadUserLanguageSetting()
    }


    private fun loadUserLanguageSetting(userId: String? = null) {
        viewModelScope.launch {
            try {
                // Versuche zuerst, die Benutzerspracheinstellung zu laden, wenn eine Benutzer-ID vorhanden ist
                val userLanguageCode = userId?.let { uid ->
                    repository.getUserProfile(uid).value?.languageCode
                }

                // Falls keine Benutzerspracheinstellung vorhanden ist, verwende die System- oder App-Einstellung
                val languageCode = userLanguageCode ?: withContext(Dispatchers.IO) {
                    repository.loadLanguageSetting()
                }

                // Aktualisiere die App-Locale mit der gefundenen Spracheinstellung
                val localeListCompat = LocaleListCompat.forLanguageTags(languageCode)
                AppCompatDelegate.setApplicationLocales(localeListCompat)
                _localeList.postValue(localeListCompat)


            } catch (_: Exception) {

            }
        }
    }


    fun setSelectedLanguageCode(languageCode: String) {
        viewModelScope.launch {
            try {
                _selectedLanguageCode.value = languageCode
            } catch (_: Exception) {
            }
        }
    }

    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Speichere die Spracheinstellung
                    repository.saveLanguageSetting(languageCode)

                }
                val localeListCompat = LocaleListCompat.forLanguageTags(languageCode)
                AppCompatDelegate.setApplicationLocales(localeListCompat)
                _localeList.postValue(localeListCompat)

            } catch (_: Exception) {
            }
        }
    }


    /**
     * Versucht, den Benutzer mit E-Mail und Passwort anzumelden.
     * Bei erfolgreicher Anmeldung wird das Benutzerprofil geladen und die
     * Spracheinstellungen des Benutzers werden über 'loadUserLanguageSetting'.
     * Bei Misserfolg oder einem Fehler wird der Login-Status entsprechend aktualisiert.
     *
     * @param email: Die E-Mail-Adresse des Benutzers.
     * @param password: Das Passwort des Benutzers.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        fetchUserProfile()
                        loadUserLanguageSetting(auth.currentUser?.uid)
                        _loginStatus.value = true
                    } else {
                        _loginStatus.value = false
                    }
                }
            } catch (e: Exception) {
                _loginStatus.value = false
            }
        }
    }


    /**
     * Überprüft, ob eine E-Mail-Adresse bereits in der Firestore-Datenbank existiert.
     * Aktualisiert den LiveData-Status '_emailExists' basierend auf der Überprüfung.
     *
     * @param username: Die zu überprüfende E-Mail-Adresse.
     */
    fun doesEmailExist(username: String) {
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance().collection("user")
                    .whereEqualTo("username", username).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documents = task.result
                            _emailExists.value = documents != null && !documents.isEmpty
                        } else {
                            _emailExists.value = false
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in doesEmailExist", e)
                _emailExists.value = false
            }
        }
    }


    private fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val docRef = Firebase.firestore.collection("user").document(userId)
                    docRef.get().addOnSuccessListener { documentSnapshot ->
                        val userProfile = documentSnapshot.toObject<Profile>()
                        _userProfileData.value = userProfile
                    }.addOnFailureListener {
                        _userProfileData.value = null
                    }
                } else {
                    _userProfileData.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchUserProfile", e)
                _userProfileData.value = null
            }
        }
    }

    /**
     * Verarbeitet den Login-Vorgang mit Google. Diese Methode wird aufgerufen,
     * wenn der Benutzer erfolgreich ein Google-Konto ausgewählt hat und das ID-Token
     * vom Google-SignIn-Prozess erhalten wurde.
     *
     * @param idToken: Das ID-Token, das von Google beim SignIn erhalten wurde.
     */
    fun onGoogleLoginClicked(idToken: String) {
        viewModelScope.launch {
            try {
                // Erstellt ein Authentifizierungs-credential mit dem ID-Token von Google.
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                // Verwendet das credential, um sich mit Firebase zu authentifizieren.
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Wenn die Authentifizierung erfolgreich ist, setzt es den Login-Status auf true
                            // und lädt die Benutzerspracheinstellungen.
                            _loginStatus.value = true
                            loadUserLanguageSetting(auth.currentUser?.uid)
                        } else {
                            // Bei Misserfolg setzt es den Login-Status auf false.
                            _loginStatus.value = false
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onGoogleLoginClicked", e)
                _loginStatus.value = false
            }
        }
    }// TODO:wie kriege Ich das hin das wenn der Benutzer sich der Benutzer einmal über Google angemeldet hat direkt für immer eingeloggt bleibt und direkt zu einem hypotethischen DashboardFragment springt ?


    /**
     * Führt die Registrierung eines neuen Benutzers mit der angegebenen E-Mail und dem Passwort durch.
     * Überprüft zunächst, ob die Passwörter übereinstimmen. Ist dies der Fall, wird versucht,
     * einen neuen Benutzer mit Firebase Authentication zu erstellen.
     *
     * @param email: Die E-Mail-Adresse des Benutzers.
     * @param password: Das Passwort des Benutzers.
     * @param confirmPassword: Das zur Überprüfung eingegebene Passwort.
     * @param languageCode: Der Sprachcode für die Sprachpräferenz des Benutzers.
     */
    fun register(email: String, password: String, confirmPassword: String, languageCode: String) {
        viewModelScope.launch {
            // Überprüft, ob die Passwörter übereinstimmen.
            if (password != confirmPassword) {
                _registrationStatus.value =
                    RegistrationStatus(
                        success = false,
                        message = "Passwörter stimmen nicht überein"
                    )
                return@launch
            }
            // Versucht, den Benutzer mit der angegebenen E-Mail und dem Passwort zu registrieren.
            try {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    // Behandelt den Erfolg oder Misserfolg der Registrierung.
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let { user ->
                            // Registrierung erfolgreich: Erstellt und speichert das Benutzerprofil.
                            val userId = user.uid
                            val userProfile = Profile(
                                isPremium = false,
                                username = email,
                                languageCode = languageCode
                            )
                            repository.createUserProfile(
                                userId,
                                userProfile
                            ) // Rufe die Methode aus dem Repository auf
                        }
                        _registrationStatus.value = RegistrationStatus(success = true)
                    } else {
                        // Registrierung fehlgeschlagen: Setzt den Registrierungsstatus entsprechend.
                        val message = when (task.exception) {
                            is FirebaseAuthUserCollisionException -> R.string.emailinuse
                            else -> R.string.unkownregistererror
                        }
                        Log.e(TAG, message.toString(), task.exception)
                        _registrationStatus.value =
                            RegistrationStatus(success = false, message = message)
                    }
                }
            } catch (e: Exception) {
                // Bei einer Ausnahme wird der Registrierungsstatus entsprechend gesetzt.
                _registrationStatus.value = RegistrationStatus(
                    success = false,
                    message = "Ausnahme in der Registrationsmethode: ${e.localizedMessage}"
                )
            }
        }
    }


    // TODO:wie kriege Ich das hin das wenn der Benutzer sich der Benutzer einmal über Google angemeldet hat direkt für immer eingeloggt bleibt und direkt zu einem hypotethischen DashboardFragment springt ?


    // In MainViewModel.kt
    fun translateToGerman(inputText: String, onComplete: (String) -> Unit) {

        val currentLanguageCode = _selectedLanguageCode.value ?: "EN"
        // Prüfe, ob die aktuelle Sprache bereits Deutsch ist
        if (currentLanguageCode == "de") {
            onComplete(inputText) // Gebe den ursprünglichen Text zurück, ohne Übersetzung
            return // Beende die Methode vorzeitig
        }

        viewModelScope.launch {
            try {
                val response = repository.translateText(inputText, "DE")
                // Prüfe und korrigiere spezifische Übersetzungen
                val correctedTranslation = when (val translatedText = response?.text ?: inputText) {
                    "Computerwissenschaften" -> "Informatik"
                    "Angehörige der regulären Streitkräfte in anderen Dienstgraden" -> "Angehörige der regulären Streitkräfte in sonstigen Rängen"
                    "Ärztin und Praxisassistentin" -> "Arzt- und Praxishilfe"
                    "Innen- und Trockenbau, Isolierung, Tischlerei, Verglasung" -> "Aus- und Trockenbau, Isolierung, Zimmerei, Glaserei"
                    "Sozialwissenschaften" -> "Gesellschaftswissenschaften"
                    "Humanmedizin und Zahnmedizin" -> "Human- und Zahnmedizin"
                    "Krankenpflege, Notdienst und Hebammenwesen" -> "Krankenpflege, Rettungsdienst und Geburtshilfe"
                    "Datenverarbeitung" -> "Informatik"
                    "Altenpflegehelferin" -> "Altenpflegehelfer/in\""
                    "Verwaltung von Baumaschinen und Transportmitteln" -> "Bau- und Transportgeräteführung"
                    "Bühnen- und Kostümbild, Requisiten" -> "Bühnen- und Kostümbildnerei, Requisite"
                    "Einkauf und Verkauf" -> "Einkauf und Vertrieb"
                    "Elektroingenieurwesen" -> "Elektrotechnik"
                    "Ernährung und Gesundheitsberatung" -> "Ernährungs- und Gesundheitsberatung"
                    "Erziehung, Sozialarbeit, Heilpädagogik" -> "Erziehung, Sozialarbeit, Heilerziehungspflege"
                    "Fahrzeugführung im Schienenverkehr" -> "Fahrzeugführung im Eisenbahnverkehr"
                    "Fahrzeugrouting im Schiffsverkehr" -> "Fahrzeugführung im Schiffsverkehr"
                    "Farben- und Lacktechnologie" -> "Farb- und Lacktechnik"
                    "Feinmechanik und Werkzeugtechnik" -> "Feinwerk- und Werkzeugtechnik"
                    "Fischereiindustrie" -> "Fischwirtschaft"
                    "Forstwirtschaft, Jagd, Landschaftspflege" -> "Forstwirtschaft, Jagdwirtschaft, Landschaftspflege"
                    "Management und Vorstand" -> "Geschäftsführung und Vorstand"
                    "Handel, Gesundheitsüberwachung, Desinfektion" -> "Gewerbe, Gesundheitsaufsicht, Desinfektion"
                    "Bauwesen" -> "Hochbau"
                    "Holzbearbeitung und Holzverarbeitung" -> "Holzbe- und -verarbeitung"
                    "Hotelgewerbe" -> "Hotellerie"
                    "IT-Netzwerktechnik, Verwaltung, Organisation" -> "IT-Netzwerktechnik, -Administration, -Organisation"
                    "IT-Systemanalyse, Anwendungsberatung und Vertrieb" -> "IT-Systemanalyse, -Anwendungsberatung und -Vertrieb"
                    "Immobilien- und Gebäudemanagement" -> "Immobilienwirtschaft und Facility-Management"
                    "Industrielle Glasproduktion" -> "Industrielle Glasherstellung"
                    "Industrielle Keramikproduktion" -> "Industrielle Keramikherstellung"
                    "Innenarchitektur, Inneneinrichtung" -> "Innenarchitektur, Raumausstattung"
                    "Geschäftsleute - Transport und Logistik" -> "Kaufleute - Verkehr und Logistik"
                    "Sanitär-, Heizungs- und Klimatechnik" -> "Klempnerei, Sanitär-, Heizungs- und Klimatechnik"
                    "Kunsthandwerkliches Keramik- und Glasdesign" -> "Kunsthandwerkliche Keramik- und Glasgestaltung"
                    "Handwerkliches Metalldesign" -> "Kunsthandwerkliche Metallgestaltung"
                    "Herstellung von Kunststoffen und Gummi" -> "Kunststoff- und Kautschukherstellung"
                    "Lagerhaltung, Post und Zustellung, Warenumschlag" -> "Lagerwirtschaft, Post und Zustellung, Güterumschlag"
                    "Nahrungs- und Genussmittelproduktion" -> "Lebensmittel- und Genussmittelherstellung"
                    "Leder- und Pelzproduktion" -> "Leder- und Pelzherstellung"
                    "Lehr- und Forschungstätigkeiten an Universitäten" -> "Lehr- und Forschungstätigkeit an Hochschulen"
                    "Unterricht an allgemeinbildenden Schulen" -> "Lehrtätigkeit an allgemeinbildenden Schulen"
                    "Unterricht an außerschulischen Bildungseinrichtungen" -> "Lehrtätigkeit an außerschulischen Bildungseinrichtungen"
                    "Unterricht in beruflichen Fächern und betriebliche Ausbildung" -> "Lehrtätigkeit berufsbildender Fächer und betriebliche Ausbildung"
                    "Maschinenbau und Betriebstechnik" -> "Maschinenbau- und Betriebstechnik"
                    "Medien, Dokumentation und Informationsdienste" -> "Medien-, Dokumentations- und Informationsdienste"
                    "Medizinisches Labor" -> "Medizinisches Laboratorium"
                    "Oberflächenbehandlung von Metall" -> "Metalloberflächenbehandlung"
                    "Museumstechnik und -verwaltung" -> "Museumstechnik und -management"
                    "Musik, Gesang, Dirigentenarbeit" -> "Musik-, Gesang-, Dirigententätigkeiten"
                    "Naturstein- und Mineralienverarbeitung, Baumaterialproduktion" -> "Naturstein- und Mineralaufbereitung, Baustoffherstellung"
                    "Nichtmedizinische Therapie und Medizin" -> "Nichtärztliche Therapie und Heilkunde"
                    "Sach-, Personen- und Feuerschutz, Arbeitssicherheit" -> "Objekt-, Personen-, Brandschutz, Arbeitssicherheit"
                    "Beamte" -> "Offiziere"
                    "Verwaltung der Humanressourcen und Dienstleistungen" -> "Personalwesen und -dienstleistung"


                    // Füge hier weitere spezifische Korrekturen hinzu, falls notwendig
                    else -> translatedText
                }
                onComplete(correctedTranslation)
            } catch (e: Exception) {
                Log.e("translateToGerman", "Fehler bei der Übersetzung von $inputText", e)
                // Optional: Handle den Fehler angemessen
                onComplete(inputText) // Gebe im Fehlerfall den Originaltext zurück
            }
        }
    }


    /**
     * Holt die Liste der Berufsfelder asynchron aus der Datenquelle (Repository) und speichert sie in einer LiveData-Variable.
     * Diese Methode nutzt Coroutines, um den asynchronen Aufruf zu handhaben und Fehlertoleranz zu gewährleisten.
     */
    fun fetchOccupationalFields() {
        // Start einer Coroutine im ViewModelScope, um asynchrone Operationen zu ermöglichen.
        viewModelScope.launch {
            try {
                // Versucht, die Berufsfelder vom Repository zu erhalten.
                val response = repository.getOccupationalFields()
                if (response.isSuccess) {
                    Log.d(TAG, "Berufsfelder erfolgreich abgerufen.")
                    // Bei Erfolg werden die Berufsfelder in der LiveData-Variable gespeichert.
                    _occupationalfields.value = response.getOrNull()!!
                } else {
                    // Im Fehlerfall wird eine leere Liste gesetzt, um die Fehlerbehandlung in der UI zu ermöglichen.
                    _occupationalfields.value = listOf()
                    Log.e(
                        TAG,
                        "Fehler beim Abrufen der Berufsfelder: ${response.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                // Fängt jegliche Ausnahmen beim Abrufen der Berufsfelder ab und setzt die LiveData-Variable auf eine leere Liste.
                _occupationalfields.value = listOf()
                Log.e(TAG, "Fehler beim Abrufen der Berufsfelder: ${e.message}")
            }
        }
    }


    /**
     * Übersetzt eine Liste von Berufsfeldern in die im ViewModel ausgewählte Sprache.
     * Die Methode prüft zuerst, ob eine Übersetzung notwendig ist (d.h. der aktuelle Sprachcode ist nicht "de").
     * Wenn eine Übersetzung erforderlich ist, wird jedes Berufsfeld asynchron übersetzt.
     * Nachdem alle Berufsfelder übersetzt wurden, wird eine Callback-Funktion aufgerufen.
     *
     * @param berufsfelder: Die Liste der Berufsfelder, die übersetzt werden sollen.
     * @param onComplete: Die Callback-Funktion, die mit der Liste der übersetzten Berufsfelder aufgerufen wird.
     */
    fun translateOccupationalFields(
        berufsfelder: List<String>,
        onComplete: (List<String>) -> Unit
    ) {
        // Erfasst den aktuellen Wert des Sprachcodes vor dem Start der Coroutine
        val currentLanguageCode =
            _selectedLanguageCode.value ?: "EN" // Standardwert ist "EN", falls null

        // Prüft, ob der aktuelle Sprachcode "de" ist. Falls ja, führt es die Methode nicht aus.
        if (currentLanguageCode == "de") {
            onComplete(berufsfelder) // Gibt die ursprünglichen Berufsfelder zurück, ohne Übersetzung
            return // Beendet die Methode vorzeitig
        }

        viewModelScope.launch {
            val translatedOccupationalFields = mutableListOf<String>()
            berufsfelder.forEach { berufsfeld ->
                try {
                    // Verwendet currentLanguageCode als Ziel für die Übersetzung.
                    val result = repository.translateText(berufsfeld, currentLanguageCode)
                    result?.text?.let {
                        translatedOccupationalFields.add(it)
                        Log.d("translateBerufsfelder", "Übersetzt: $berufsfeld zu $it")
                    }
                } catch (e: Exception) {
                    Log.e("translateBerufsfelder", "Fehler bei der Übersetzung von $berufsfeld", e)
                }
            }
            onComplete(translatedOccupationalFields)
        }
    }

    /**
     * Holt die Liste der Arbeitsorte asynchron aus der Datenquelle (Repository) und speichert sie in einer LiveData-Variable.
     * Diese Methode nutzt Coroutines, um den asynchronen Aufruf zu handhaben und Fehlertoleranz zu gewährleisten.
     */
    fun fetchWorkLocations() {
        // Start einer Coroutine im ViewModelScope, um asynchrone Operationen zu ermöglichen.
        viewModelScope.launch {
            try {
                // Versuch, Arbeitsorte vom Repository zu erhalten
                val response = repository.getWorkLocations()
                if (response.isSuccess) {
                    // Bei Erfolg werden die erhaltenen Arbeitsorte in _arbeitsorte LiveData gesetzt.
                    Log.d(TAG, "Arbeitsorte erfolgreich abgerufen.")
                    _worklocations.value = response.getOrNull()!!
                } else {
                    // Bei Misserfolg wird eine leere Liste gesetzt..
                    _worklocations.value = listOf()
                    Log.e(
                        TAG,
                        "Fehler beim Abrufen der Arbeitsorte: ${response.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                // Bei einer Ausnahme wird ebenfalls eine leere Liste gesetzt
                _worklocations.value = listOf()
                Log.e(TAG, "Fehler beim Abrufen der Arbeitsorte: ${e.message}")
            }
        }
    }

    /**
     * Übersetzt eine Liste von Arbeitsorten in die im ViewModel ausgewählte Sprache.
     * Die Methode prüft zuerst, ob eine Übersetzung notwendig ist (d.h. der aktuelle Sprachcode ist nicht "de").
     * Wenn eine Übersetzung erforderlich ist, wird jedes Berufsfeld asynchron übersetzt.
     * Nachdem alle Berufsfelder übersetzt wurden, wird eine Callback-Funktion aufgerufen.
     *
     * @param arbeitsorte: Die Liste der Berufsfelder, die übersetzt werden sollen.
     * @param onComplete: Die Callback-Funktion, die mit der Liste der übersetzten Berufsfelder aufgerufen wird.
     */
    fun translateWorkLocations(arbeitsorte: List<String>, onComplete: (List<String>) -> Unit) {
        // Erfasst den aktuellen Wert des Sprachcodes vor dem Start der Coroutine
        val currentLanguageCode =
            _selectedLanguageCode.value ?: "EN" // Standardwert ist "EN", falls null

        // Prüft, ob der aktuelle Sprachcode "de" ist. Falls ja, führt es die Methode nicht aus.
        if (currentLanguageCode == "de") {
            onComplete(arbeitsorte) // Gibt die ursprünglichen Arbeitsorte zurück, ohne Übersetzung.
            return // Beendet die Methode vorzeitig.
        }

        viewModelScope.launch {
            val translatedWorkLocations = mutableListOf<String>()
            arbeitsorte.forEach { arbeitsort ->
                try {
                    // Verwendet currentLanguageCode als Ziel für die Übersetzung
                    val result = repository.translateText(arbeitsort, currentLanguageCode)
                    result?.text?.let {
                        translatedWorkLocations.add(it)
                        Log.d("translateArbeitsorte", "Übersetzt: $arbeitsort zu $it")
                    }
                } catch (e: Exception) {
                    Log.e("translateArbeitsorte", "Fehler bei der Übersetzung von $arbeitsort", e)
                }
            }
            onComplete(translatedWorkLocations)
        }
    }

    /**
     * Speichert das Benutzerprofil mit einem Bild asynchron.
     * Diese Methode lädt das Bild hoch, erhält dessen URL und aktualisiert dann das Benutzerprofil
     * mit den bereitgestellten Informationen.
     *
     * @param uri: Uri des Profilbildes.
     * @param name: Name des Benutzers.
     * @param age: Alter des Benutzers als String.
     * @param fieldOfWork: Berufsfeld des Benutzers.
     * @param isDataProtected: Datenschutzzustimmung des Benutzers.
     * @param languageLevel: Sprachniveau des Benutzers.
     * @param desiredLocation: Gewünschter Arbeitsort des Benutzers.
     * @param street: Straßenadresse des Benutzers.
     * @param birthplace: Geburtsort des Benutzers.
     * @param maidenname: Mädchenname des Benutzers.
     * @param firstname: Vorname des Benutzers.
     * @param lastname: Nachname des Benutzers.
     * @param phonenumber: Telefonnummer des Benutzers.
     */
    fun saveProfileWithImage(
        uri: Uri, name: String, age: String, fieldOfWork: String,
        isDataProtected: Boolean, languageLevel: String,
        desiredLocation: String, street: String, birthplace: String, maidenname: String,
        firstname: String, lastname: String, phonenumber: String
    ) {
        viewModelScope.launch {
            try {
                // Prüft, ob der Benutzer angemeldet ist und eine User-ID vorhanden ist.
                val userId =
                    repository.firebaseAuth.currentUser?.uid ?: throw Exception("Nicht angemeldet")
                // Hochladen des Bildes und Erhalten der URL.
                val imageUrl = repository.uploadAndUpdateProfilePicture(uri, userId)


                // Konvertiere das Alter in einen Integer. Bei ungültiger Eingabe wird 0 verwendet.
                val ageInt = age.toIntOrNull() ?: 0

                // Erstellen einer Map mit Profildaten.
                val profileData = mapOf(
                    "name" to name,
                    "age" to ageInt,
                    "fieldOfWork" to fieldOfWork,
                    "profilePicture" to imageUrl,
                    "dataProtection" to isDataProtected,
                    "languageLevel" to languageLevel,
                    "desiredLocation" to desiredLocation,
                    "street" to street,
                    "birthplace" to birthplace,
                    "maidenname" to maidenname,
                    "firstname" to firstname,
                    "lastname" to lastname,
                    "phonenumber" to phonenumber
                )
                // Aktualisiert das Benutzerprofil mit den neuen Daten.
                repository.updateUserProfile(userId, profileData)
            } catch (_: Exception) {

            }
        }
    }


    // Im ViewModel
    fun fetchJobNomination(berufsfeld: String, arbeitsort: String) {
        viewModelScope.launch {
            try {
                val response = repository.getJobs(berufsfeld, arbeitsort)
                if (response.isSuccess) {

                    _jobs.value = response.getOrNull() ?: listOf()
                } else {
                    _jobs.value = listOf()

                }
            } catch (e: Exception) {
                _jobs.value = listOf()

            }
        }
    }


    // translate german to english
    fun translateJobTitles(jobTitles: List<String>, onComplete: (List<String>) -> Unit) {
        // Erfasse den aktuellen Wert des Sprachcodes vor dem Start der Coroutine
        val currentLanguageCode =
            _selectedLanguageCode.value ?: "EN" // Standardwert ist "EN", falls null

        // Prüfe, ob der aktuelle Sprachcode "de" ist. Falls ja, führe die Methode nicht aus.
        if (currentLanguageCode == "de") {
            onComplete(jobTitles) // Gebe die ursprünglichen Jobtitel zurück, ohne Übersetzung
            return // Beende die Methode vorzeitig
        }

        viewModelScope.launch {
            val translatedJobTitles = mutableListOf<String>()
            jobTitles.forEach { jobTitle ->
                try {
                    // Verwende currentLanguageCode als Ziel für die Übersetzung
                    val result = repository.translateText(jobTitle, currentLanguageCode)
                    result?.text?.let {
                        translatedJobTitles.add(it)

                    }
                } catch (_: Exception) {

                }
            }
            onComplete(translatedJobTitles)
        }
    }

    // Überall wo ich toggeln kann , muss ich überprüfen ob sich die Live data wirklich verändert, anhand
    //Logs beispielsweise , baue einen observer (todoListe)

    fun fetchJobOffers(was: String, arbeitsort: String) {
        viewModelScope.launch {
            val response = repository.getJobOffers(was, arbeitsort)
            if (response.isSuccess) {
                // Angenommen, die API gibt eine Liste von Job-Objekten zurück
                val jobPairs = response.getOrNull()?.map { it }?.toList() ?: listOf()
                _jobOffers.postValue(jobPairs)
            } else {
                _jobOffers.postValue(listOf())
                Log.e(
                    TAG,
                    "Fehler beim Abrufen der Jobangebote: ${response.exceptionOrNull()?.message}"
                )
            }
        }
    }


    // translate job offers to the selected language
    fun translateJobOffers(
        jobOffers: List<Pair<String, String>>,
        onComplete: (List<Pair<String, String>>) -> Unit
    ) {
        val currentLanguageCode = _selectedLanguageCode.value ?: "EN"
        if (currentLanguageCode == "de") {
            onComplete(jobOffers)
            return
        }

        viewModelScope.launch {
            val translatedJobOffers = mutableListOf<Pair<String, String>>()
            jobOffers.forEach { (jobTitle, refNr) ->
                try {
                    val result = repository.translateText(jobTitle, currentLanguageCode)
                    val translatedTitle =
                        result?.text ?: jobTitle // Verwende den Originaltitel als Fallback
                    translatedJobOffers.add(Pair(translatedTitle, refNr))
                } catch (e: Exception) {
                    Log.e("translateJobOffers", "Fehler bei der Übersetzung von $jobTitle", e)
                    translatedJobOffers.add(
                        Pair(
                            jobTitle,
                            refNr
                        )
                    ) // Füge den Originaltitel im Fehlerfall hinzu
                }
            }
            onComplete(translatedJobOffers)
        }
    }

    fun updateJobOffers(was: String, arbeitsort: String) {
        viewModelScope.launch {
            try {
                fetchJobOffers(was, arbeitsort)
                Log.d("updateJobOffers", "Successfully updated job offers for $was in $arbeitsort")
            } catch (e: Exception) {
                Log.e("updateJobOffers", "Error updating job offers for $was in $arbeitsort", e)
            }
        }
    }


    fun updateSelectedJobsAndPersist(newSelectedJobs: Map<String, String>) {
        viewModelScope.launch {
            try {
                val currentProfile = _userProfileData.value ?: Profile()

                // Ergänze die aktuellen ausgewählten Jobs mit den neuen
                val updatedSelectedJobs =
                    currentProfile.selectedJobs?.toMutableMap() ?: mutableMapOf()
                updatedSelectedJobs.putAll(newSelectedJobs)

                currentProfile.selectedJobs = updatedSelectedJobs.toMap()

                _userProfileData.value = currentProfile

                saveSelectedJobsToFirebase(updatedSelectedJobs.toMap())
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateSelectedJobsAndPersist", e)
            }
        }
    }


    private fun saveSelectedJobsToFirebase(selectedJobs: Map<String, String>) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                repository.updateUserProfileField(userId, "selectedJobs", selectedJobs)
            } catch (e: Exception) {
                // Hier können Sie den Fehler loggen oder behandeln
                Log.e(TAG, "Fehler beim Speichern der ausgewählten Jobs in Firebase", e)
            }
        }
    }


    fun updateToDoItemForJob(
        userId: String,
        rawJobId: String,
        todoId: String,
        isCompleted: Boolean,
        text: String
    ) {
        viewModelScope.launch {
            try {
                val jobId = sanitizeJobId(rawJobId)
                val userDocRef = FirebaseFirestore.getInstance().collection("user").document(userId)
                val todoDocRef = userDocRef.collection("todos").document(jobId)

                val toDoData = mapOf(
                    "erledigt" to isCompleted,
                    "text" to text
                )

                todoDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Wenn das Dokument existiert, aktualisiere das spezifische To-Do-Item
                        todoDocRef.update("todos.$todoId", toDoData)
                            .addOnSuccessListener {
                                Log.d(TAG, "ToDo item updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error updating ToDo item", e)
                            }
                    } else {
                        // Wenn das Dokument nicht existiert, erstelle ein neues mit dem To-Do-Item
                        val newTodo = mapOf("todos" to mapOf(todoId to toDoData))
                        todoDocRef.set(newTodo)
                            .addOnSuccessListener {
                                Log.d(TAG, "New ToDo item created successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error creating new ToDo item", e)
                            }
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error checking if ToDo item exists", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateToDoItemForJob", e)
            }
        }
    }


    fun getToDoItemsForJob(userId: String, jobId: String): LiveData<List<ToDoItem>> {
        val liveData = MutableLiveData<List<ToDoItem>>()

        viewModelScope.launch {
            try {
                val sanitizedJobId = sanitizeJobId(jobId)
                val todoDocRef = FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .collection("todos")
                    .document(sanitizedJobId)

                todoDocRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Error fetching todo items", e)
                        liveData.value = emptyList()
                    } else {
                        val todos =
                            snapshot?.get("todos") as? Map<String, Map<String, Any>> ?: emptyMap()
                        val toDoItems = todos.map { (id, data) ->
                            ToDoItem(
                                id,
                                data["text"] as? String ?: "",
                                data["erledigt"] as? Boolean ?: false
                            )
                        }
                        liveData.value = toDoItems
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching todo items", e)
                liveData.value = emptyList()
            }
        }

        return liveData
    }


    /**
     * Bereinigt die jobId, indem sie alle nicht alphanumerischen Zeichen durch Unterstriche ersetzt.
     */
    private fun sanitizeJobId(jobId: String): String {
        return jobId.replace(Regex("[^A-Za-z0-9]"), "_")
    }


    fun updateToDoText(userId: String, jobId: String, todoId: String, newText: String) {
        viewModelScope.launch {
            try {
                val sanitizedJobId = sanitizeJobId(jobId)
                val todoDocRef = FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .collection("todos")
                    .document(sanitizedJobId)

                // Hier aktualisierst du nicht das ganze Dokument, sondern nur das 'text'-Feld des spezifischen ToDo
                val todoItemPath = "todos.$todoId.text"
                todoDocRef.update(todoItemPath, newText)
                    .addOnSuccessListener { Log.d(TAG, "ToDo text updated successfully") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating ToDo text", e) }
            } catch (e: Exception) {
                // Hier können Sie den Fehler loggen oder behandeln
                Log.e(TAG, "Fehler beim Aktualisieren des ToDo-Textes", e)
            }
        }
    }


    fun deleteJobSelection(jobTitle: String) {
        viewModelScope.launch {
            try {
                val currentProfile = _userProfileData.value
                    ?: return@launch // Beendet die Methode, falls kein Profil vorhanden ist.
                val updatedJobs = currentProfile.selectedJobs?.toMutableMap() ?: mutableMapOf()

                updatedJobs.remove(jobTitle) // Entfernt den Eintrag sicher aus der Map.

                currentProfile.selectedJobs = updatedJobs // Aktualisiert die Map im Profil.
                _userProfileData.value = currentProfile // Setzt das aktualisierte Profil.
            } catch (e: Exception) {
                // Hier können Sie den Fehler loggen oder behandeln
                Log.e(TAG, "Fehler beim Löschen der Jobauswahl", e)
            }
        }
    }

    fun savedesiredLocationToFirebase(desiredLocation: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                repository.updateUserProfileField(userId, "desiredLocation", desiredLocation)
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Speichern des gewünschten Standorts in Firebase", e)
            }
        }
    }


    fun fetchJobDetails(encodedHashID: String) {
        viewModelScope.launch {
            try {
                val result = repository.getJobDetails(encodedHashID)
                _jobDetails.value = result
            } catch (e: Exception) {
                // Du könntest auch eine spezifischere Fehlerbehandlung hier einbauen
                _jobDetails.value = Result.failure(e)
            }
        }
    }


    fun translateJobDetails(
        jobDetails: JobDetailsResponse,
        onComplete: (JobDetailsResponse) -> Unit
    ) {
        val currentLanguageCode = _selectedLanguageCode.value ?: "EN"
        if (currentLanguageCode == "de") {
            onComplete(jobDetails)
            return
        }

        viewModelScope.launch {
            try {
                val translatedEmployer = repository.translateText(
                    jobDetails.arbeitgeber ?: "",
                    currentLanguageCode
                )?.text ?: jobDetails.arbeitgeber
                val translatedJobOfferDescription = repository.translateText(
                    jobDetails.stellenbeschreibung ?: "",
                    currentLanguageCode
                )?.text ?: jobDetails.stellenbeschreibung
                val translatedBranch =
                    repository.translateText(jobDetails.branche ?: "", currentLanguageCode)?.text
                        ?: jobDetails.branche
                val translatedOfferType = repository.translateText(
                    jobDetails.angebotsart ?: "",
                    currentLanguageCode
                )?.text ?: jobDetails.angebotsart
                val translatedTitle =
                    repository.translateText(jobDetails.titel ?: "", currentLanguageCode)?.text
                        ?: jobDetails.titel
                val translatedProfession =
                    repository.translateText(jobDetails.beruf ?: "", currentLanguageCode)?.text
                        ?: jobDetails.beruf
                val translatedRemuneration =
                    repository.translateText(jobDetails.verguetung ?: "", currentLanguageCode)?.text
                        ?: jobDetails.verguetung


                val translatedWorkingTimeModels = jobDetails.arbeitszeitmodelle?.map { model ->
                    repository.translateText(model, currentLanguageCode)?.text ?: model
                } ?: listOf()

                // Übersetzung der Arbeitsorte, wenn nötig
                val translatedWorkLocations = jobDetails.arbeitsorte?.map { ort ->
                    ort.copy(
                        ort = repository.translateText(ort.ort ?: "", currentLanguageCode)?.text
                            ?: ort.ort
                    )
                }

                // Erstelle ein neues JobDetailsResponse-Objekt mit den übersetzten Werten
                val translatedJobDetails = jobDetails.copy(
                    arbeitgeber = translatedEmployer,
                    stellenbeschreibung = translatedJobOfferDescription,
                    branche = translatedBranch,
                    angebotsart = translatedOfferType,
                    titel = translatedTitle,
                    beruf = translatedProfession,
                    arbeitszeitmodelle = translatedWorkingTimeModels,
                    arbeitsorte = translatedWorkLocations,
                    verguetung = translatedRemuneration,
                )

                onComplete(translatedJobDetails)
            } catch (e: Exception) {

                onComplete(jobDetails) // Gebe die ursprünglichen Jobdetails zurück, falls ein Fehler auftritt
            }
        }
    }


    fun fetchEducationalOffers(
        systematiken: String,
        orte: String,
        sprachniveau: String,
        beginntermine: Int
    ) {
        viewModelScope.launch {
            try {
                val response =
                    repository.getEducationalOffers(systematiken, orte, sprachniveau, beginntermine)
                if (response.isSuccess) {
                    _educationaloffers.postValue(response.getOrNull() ?: listOf())
                } else {
                    _educationaloffers.postValue(listOf())
                    Log.e(
                        TAG,
                        "Fehler beim Abrufen der Bildungsangebote: ${response.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ausnahme beim Abrufen der Bildungsangebote", e)
            }
        }
    }


    fun translateEducationalOffers(
        bildungsangebote: List<TerminResponse>,
        onComplete: (List<TerminResponse>) -> Unit
    ) {
        val currentLanguageCode = _selectedLanguageCode.value ?: "EN"
        if (currentLanguageCode == "de") {
            onComplete(bildungsangebote)
            return
        }

        viewModelScope.launch {
            try {
                val translatedEducationalOffers = bildungsangebote.map { angebot ->
                    val translatedTitel = repository.translateText(
                        angebot.angebot?.titel ?: "",
                        currentLanguageCode
                    )?.text ?: angebot.angebot?.titel
                    val translatedInhalt = repository.translateText(
                        angebot.angebot?.inhalt ?: "",
                        currentLanguageCode
                    )?.text ?: angebot.angebot?.inhalt

                    angebot.copy(
                        angebot = angebot.angebot?.copy(
                            titel = translatedTitel,
                            inhalt = translatedInhalt
                        )
                    )
                }
                onComplete(translatedEducationalOffers)
            } catch (e: Exception) {

                onComplete(bildungsangebote) // Gebe die ursprünglichen Bildungsangebote zurück, falls ein Fehler auftritt
            }
        }
    }


    fun updateProfileImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Nicht angemeldet")
                val firebaseStorage = Firebase.storage
                val firebaseFirestore = Firebase.firestore

                val imageRef = firebaseStorage.reference.child("images/$userId/profilePicture")
                // Direktes Hochladen des Bildes ohne Zuweisung zu einer Variable
                imageRef.putFile(uri).await()
                val imageUrl = imageRef.downloadUrl.await().toString()
                firebaseFirestore.collection("user").document(userId)
                    .update("profilePicture", imageUrl).await()

            } catch (_: Exception) {

            }
        }
    }


    fun addFlashcard(userId: String, frontText: String, backText: String) {
        viewModelScope.launch {
            try {
                val newCard = IndexCard(frontText = frontText, backText = backText)
                val docRef = FirebaseFirestore.getInstance().collection("user").document(userId)
                    .collection("flashcards").document()
                newCard.id = docRef.id  // Setze die Firestore-ID als die ID der Karte
                docRef.set(newCard)
            } catch (_: Exception) {

            }
        }
    }


    fun updateFlashcard(userId: String, cardId: String, frontText: String?, backText: String?) {
        viewModelScope.launch {
            try {
                val cardRef = FirebaseFirestore.getInstance().collection("user").document(userId)
                    .collection("flashcards").document(cardId)
                val updateMap = mutableMapOf<String, Any>()
                frontText?.let { updateMap["frontText"] = it }
                backText?.let { updateMap["backText"] = it }

                cardRef.update(updateMap)
            } catch (_: Exception) {

            }
        }
    }


    fun getFlashcards(userId: String): LiveData<List<IndexCard>> {
        val liveData = MutableLiveData<List<IndexCard>>()
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance().collection("user").document(userId)
                    .collection("flashcards")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            // Handle the error
                            liveData.postValue(emptyList())
                        } else {
                            val flashcards = snapshot?.toObjects(IndexCard::class.java)
                            liveData.postValue(flashcards ?: emptyList())
                        }
                    }
            } catch (_: Exception) {
            }
        }
        return liveData
    }


    fun updateToDoItem(userId: String, todoId: String, isCompleted: Boolean, text: String) {
        viewModelScope.launch {
            try {
                val todoDocRef = FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .collection("relocationTodos")
                    .document(todoId)

                val toDoData = mapOf(
                    "erledigt" to isCompleted,
                    "text" to text
                )

                todoDocRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document.exists()) {
                            todoDocRef.update(toDoData)
                                .addOnSuccessListener {
                                    Log.d(
                                        TAG,
                                        "ToDo item updated successfully"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        TAG,
                                        "Error updating ToDo item",
                                        e
                                    )
                                }
                        } else {
                            todoDocRef.set(toDoData)
                                .addOnSuccessListener {
                                    Log.d(
                                        TAG,
                                        "ToDo item created successfully"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        TAG,
                                        "Error creating ToDo item",
                                        e
                                    )
                                }
                        }
                    } else {
                        Log.w(TAG, "Error getting document", task.exception)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in updateToDoItem", e)
            }
        }
    }


    fun getToDoItems(userId: String): LiveData<List<ToDoItemRelocation>> {
        val liveData = MutableLiveData<List<ToDoItemRelocation>>()

        viewModelScope.launch {
            try {
                val todoDocRef = FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .collection("relocationTodos")

                todoDocRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Error fetching todo items", e)
                        liveData.postValue(emptyList())
                    } else {
                        val toDoItems = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(ToDoItemRelocation::class.java)?.apply { id = doc.id }
                        } ?: emptyList()
                        liveData.postValue(toDoItems)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in getToDoItems", e)
            }
        }

        return liveData
    }

    fun updateToDoTextRelocation(userId: String, todoId: String, newText: String) {
        viewModelScope.launch {
            try {
                val todoDocRef = FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .collection("relocationTodos")
                    .document(todoId)

                todoDocRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document.exists()) {
                            todoDocRef.update("text", newText)
                                .addOnSuccessListener {
                                    Log.d(
                                        TAG,
                                        "ToDo text updated successfully"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        TAG,
                                        "Error updating ToDo text",
                                        e
                                    )
                                }
                        } else {
                            val toDoData = mapOf(
                                "erledigt" to false,
                                "text" to newText
                            )
                            todoDocRef.set(toDoData)
                                .addOnSuccessListener {
                                    Log.d(
                                        TAG,
                                        "ToDo item created successfully"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        TAG,
                                        "Error creating ToDo item",
                                        e
                                    )
                                }
                        }
                    } else {
                        Log.w(TAG, "Error getting document", task.exception)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateToDoTextRelocation", e)
            }
        }
    }

    fun deleteToDoItem(userId: String, todoId: String) {
        viewModelScope.launch {
            try {
                val todoDocRef = FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .collection("relocationTodos")
                    .document(todoId)

                todoDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        todoDocRef.delete()
                            .addOnSuccessListener { Log.d(TAG, "ToDo item deleted successfully") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error deleting ToDo item", e) }
                    } else {
                        Log.w(TAG, "ToDo item does not exist")
                    }
                }
                    .addOnFailureListener { e -> Log.w(TAG, "Error getting ToDo item", e) }
            } catch (e: Exception) {
                Log.e(TAG, "Error in deleteToDoItem", e)
            }
        }
    }


    fun saveFeedback(
        userId: String,
        designRating: Float,
        functionalityRating: Float,
        overallRating: Float,
        generalFeedback: String
    ) {
        viewModelScope.launch {
            try {
                val feedbackDocRef = FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .collection("feedback")
                    .document()  // Erstellt ein neues Dokument mit einer einzigartigen ID

                val feedbackData = mapOf(
                    "designRating" to designRating,
                    "functionalityRating" to functionalityRating,
                    "overallRating" to overallRating,
                    "generalFeedback" to generalFeedback,
                    "timestamp" to FieldValue.serverTimestamp()  // Speichert den Zeitstempel des Feedbacks
                )

                feedbackDocRef.set(feedbackData)
                    .addOnSuccessListener { Log.d(TAG, "Feedback successfully saved") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error saving feedback", e) }
            } catch (e: Exception) {
                Log.e(TAG, "Error in saveFeedback", e)
            }
        }
    }

    fun saveLanguageLevelToFirebase(languageLevel: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                repository.updateUserProfileField(userId, "languageLevel", languageLevel)
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Speichern des Sprachniveaus in Firebase", e)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                user?.delete()?.addOnCompleteListener { task ->
                    _deleteAccountStatus.value = task.isSuccessful
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Löschen des Kontos", e)
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _userProfileData.value = null
                _loginStatus.value = false
                Log.d("logout", "Benutzer erfolgreich ausgeloggt")
            } catch (e: Exception) {
                Log.e("logout", "Fehler in der LogOut-Methode", e)
            }
        }
    }


}


