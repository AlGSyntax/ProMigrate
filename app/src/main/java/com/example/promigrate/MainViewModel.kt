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
import com.example.promigrate.data.model.FlashCard
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


/**
 * MainViewModel ist eine Klasse, die AndroidViewModel erweitert und für die Vorbereitung und Verwaltung der Daten für eine Aktivität oder ein Fragment verantwortlich ist.
 * Sie behandelt auch die Kommunikation der Aktivität / des Fragments mit dem Rest der Anwendung (z.B. Aufruf der Geschäftslogikklassen).
 *
 * @param application: Die Anwendung, die dieses ViewModel besitzt.
 *
 * @property repository: Die Repository-Instanz, die für Datenoperationen verantwortlich ist. Es handelt sich um eine Referenz auf das Repository-Singleton.
 * Das Repository wird mit dem Anwendungskontext, der FirebaseAuth-Instanz, der FirebaseFirestore-Instanz, dem ProMigrateAPI-Service, dem DeepLApiService und dem ProMigrateCourseAPI-Service initialisiert.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var repository = Repository.getInstance(
        application, FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance(), ProMigrateAPI.retrofitService, DeepLApiService.create(),
        ProMigrateCourseAPI.retrofitService

    )


    /**
     * _localeList ist eine private MutableLiveData, die eine LocaleListCompat enthält.
     * LocaleListCompat ist eine Hilfsklasse, die eine Liste von Locales darstellt und mit älteren Versionen von Android kompatibel ist.
     * Diese Variable wird verwendet, um Änderungen an der Spracheinstellung der App zu verfolgen und zu speichern.
     */
    private val _localeList = MutableLiveData<LocaleListCompat>()

    /**
     * localeList ist eine öffentliche LiveData, die eine LocaleListCompat enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtbar ist. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen an der Spracheinstellung der App zu informieren.
     */
    val localeList: LiveData<LocaleListCompat> = _localeList


    /**
     * _selectedLanguageCode ist eine private MutableLiveData, die einen String enthält.
     * Dieser String repräsentiert den ausgewählten Sprachcode in der Anwendung.
     * Diese Variable wird verwendet, um Änderungen an der ausgewählten Spracheinstellung der App zu verfolgen und zu speichern.
     */
    private val _selectedLanguageCode = MutableLiveData<String>()

    /**
     * selectedLanguageCode ist eine öffentliche LiveData, die einen String enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen an der ausgewählten Spracheinstellung der App zu informieren.
     */
    val selectedLanguageCode: LiveData<String> = _selectedLanguageCode


    /**
     * Die MutableLiveData _loginStatus wird verwendet, um den Anmeldestatus intern im ViewModel zu halten.
     * Diese Variable ist privat, um die Datenkapselung zu gewährleisten.
     * Änderungen an _loginStatus sollen ausschließlich innerhalb des ViewModels erfolgen.
     */
    private val _loginStatus = MutableLiveData<Boolean>()

    /**
     * Die LiveData loginStatus wird verwendet, um den Anmeldestatus an die UI-Komponenten zu übermitteln.
     * Diese Variable ist öffentlich und kann von UI-Komponenten beobachtet werden.
     */
    val loginStatus: LiveData<Boolean> = _loginStatus

    /**
     * _emailExists ist eine private MutableLiveData, die einen Boolean enthält.
     * Diese Variable wird verwendet, um zu verfolgen, ob eine E-Mail-Adresse bereits in der Firestore-Datenbank existiert.
     */
    private val _emailExists = MutableLiveData<Boolean>()

    /**
     * emailExists ist eine öffentliche LiveData, die einen Boolean enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über die Existenz einer E-Mail-Adresse in der Firestore-Datenbank zu informieren.
     */
    val emailExists: LiveData<Boolean> = _emailExists


    /**
     * `auth` ist eine private Variable, die eine Instanz von FirebaseAuth enthält.
     * FirebaseAuth ist eine Firebase-Klasse, die für die Authentifizierung von Benutzern in Firebase verantwortlich ist.
     * Diese Variable wird verwendet, um die Authentifizierungsfunktionen von Firebase in dieser Klasse zu nutzen.
     */
    private val auth = Firebase.auth


    /**
     * _registrationStatus ist eine private MutableLiveData, die einen RegistrationStatus enthält.
     * Diese Variable wird verwendet, um Änderungen am Registrierungsstatus der App zu verfolgen und zu speichern.
     */
    private val _registrationStatus = MutableLiveData<RegistrationStatus>()

    /**
     * registrationStatus ist eine öffentliche LiveData, die einen RegistrationStatus enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen am Registrierungsstatus der App zu informieren.
     */
    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus


    /**
     * _userProfileData ist eine private MutableLiveData, die ein optionales Profil-Objekt enthält.
     * Diese Variable wird verwendet, um Änderungen am Benutzerprofil in der App zu verfolgen und zu speichern.
     */
    private var _userProfileData = MutableLiveData<Profile?>()

    /**
     * userProfileData ist eine öffentliche LiveData, die ein optionales Profil-Objekt enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen am Benutzerprofil zu informieren.
     */
    val userProfileData: LiveData<Profile?> = _userProfileData

    /**
     * _occupationalfields ist eine private MutableLiveData, die eine Liste von Strings enthält.
     * Diese Liste von Strings repräsentiert die Berufsfelder, die in der Anwendung verwendet werden.
     * Diese Variable wird verwendet, um Änderungen an den Berufsfeldern in der App zu verfolgen und zu speichern.
     */
    private val _occupationalfields = MutableLiveData<List<String>>()

    /**
     * occupationalfields ist eine öffentliche LiveData, die eine Liste von Strings enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen an den Berufsfeldern in der App zu informieren.
     */
    val occupationalfields: LiveData<List<String>> = _occupationalfields

    /**
     * _worklocations ist eine private MutableLiveData, die eine Liste von Strings enthält.
     * Diese Liste von Strings repräsentiert die Arbeitsorte, die in der Anwendung verwendet werden.
     * Diese Variable wird verwendet, um Änderungen an den Arbeitsorten in der App zu verfolgen und zu speichern.
     */
    private val _worklocations = MutableLiveData<List<String>>()

    /**
     * worklocations ist eine öffentliche LiveData, die eine Liste von Strings enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen an den Arbeitsorten in der App zu informieren.
     */
    val worklocations: LiveData<List<String>> = _worklocations


    /**
     * _jobs ist eine private MutableLiveData, die eine Liste von Strings enthält.
     * Diese Liste von Strings repräsentiert die Jobs, die in der Anwendung verwendet werden.
     * Diese Variable wird verwendet, um Änderungen an den Jobs in der App zu verfolgen und zu speichern.
     */
    private val _jobs = MutableLiveData<List<String>>()

    /**
     * jobs ist eine öffentliche LiveData, die eine Liste von Strings enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen an den Jobs in der App zu informieren.
     */
    val jobs: LiveData<List<String>> = _jobs


    /**
     * _jobOffers ist eine private MutableLiveData, die eine Liste von Paaren von Strings enthält.
     * Jedes Paar repräsentiert ein Jobangebot, wobei der erste String den Jobtitel und der zweite String den Arbeitsort darstellt.
     * Diese Variable wird verwendet, um Änderungen an den Jobangeboten in der App zu verfolgen und zu speichern.
     */
    private val _jobOffers = MutableLiveData<List<Pair<String, String>>>()

    /**
     * jobOffers ist eine öffentliche LiveData, die eine Liste von Paaren von Strings enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen an den Jobangeboten in der App zu informieren.
     */
    val jobOffers: LiveData<List<Pair<String, String>>> = _jobOffers


    /**
     * _jobDetails ist eine private MutableLiveData, die ein Result von JobDetailsResponse enthält.
     * Result ist eine generische Klasse, die entweder einen Erfolgswert mit dem angegebenen Typ oder einen Fehler enthält.
     * JobDetailsResponse ist eine Datenklasse, die die Antwort des Jobdetails-API-Aufrufs repräsentiert.
     * Diese Variable wird verwendet, um Änderungen an den Jobdetails in der App zu verfolgen und zu speichern.
     */
    private val _jobDetails = MutableLiveData<Result<JobDetailsResponse>>()

    /**
     * jobDetails ist eine öffentliche LiveData, die ein Result von JobDetailsResponse enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen an den Jobdetails in der App zu informieren.
     */
    val jobDetails: LiveData<Result<JobDetailsResponse>> = _jobDetails


    /**
     * _educationaloffers ist eine private MutableLiveData, die eine Liste von TerminResponse enthält.
     * TerminResponse ist eine Datenklasse, die die Antwort des Bildungsangebots-API-Aufrufs repräsentiert.
     * Diese Variable wird verwendet, um Änderungen an den Bildungsangeboten in der App zu verfolgen und zu speichern.
     */
    private val _educationaloffers = MutableLiveData<List<TerminResponse>>()

    /**
     * educationaloffers ist eine öffentliche LiveData, die eine Liste von TerminResponse enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über Änderungen an den Bildungsangeboten in der App zu informieren.
     */
    val educationaloffers: LiveData<List<TerminResponse>> = _educationaloffers

    /**
     * _deleteAccountStatus ist eine private MutableLiveData, die einen optionalen Boolean enthält.
     * Diese Variable wird verwendet, um den Status der Kontolöschung zu verfolgen und zu speichern.
     */
    private val _deleteAccountStatus = MutableLiveData<Boolean?>()

    /**
     * deleteAccountStatus ist eine öffentliche LiveData, die einen optionalen Boolean enthält.
     * LiveData ist eine Datenhalterklasse, die beobachtet werden kann. Im Gegensatz zu einer regulären Observable,
     * ist LiveData Lifecycle-bewusst, was bedeutet, dass sie den Lifecycle-Status ihrer Beobachter respektiert.
     * Diese Variable wird verwendet, um UI-Komponenten über den Status der Kontolöschung zu informieren.
     */
    val deleteAccountStatus: LiveData<Boolean?> = _deleteAccountStatus


    /**
     * Initialisierungsbereich für die MainViewModel-Klasse.
     * Hier wird die Methode loadUserLanguageSetting() aufgerufen, die die Benutzerspracheinstellung lädt.
     * Diese Methode wird beim Erstellen einer Instanz dieser Klasse aufgerufen.
     */
    init {
        loadUserLanguageSetting()
    }


    /**
     * Lädt die Benutzerspracheinstellung asynchron.
     * Diese Methode versucht zuerst, die Benutzerspracheinstellung zu laden, wenn eine Benutzer-ID vorhanden ist.
     * Falls keine Benutzerspracheinstellung vorhanden ist, wird die System- oder App-Einstellung verwendet.
     * Nachdem die Spracheinstellung gefunden wurde, wird die App-Locale aktualisiert und die neue Locale in einer LiveData-Variable gespeichert.
     *
     * @param userId: Die Benutzer-ID, für die die Spracheinstellung geladen werden soll. Wenn null, wird die System- oder App-Einstellung verwendet.
     */
    private fun loadUserLanguageSetting(userId: String? = null) {
        viewModelScope.launch {
            try {
                // Versucht zuerst, die Benutzerspracheinstellung zu laden, wenn eine Benutzer-ID vorhanden ist
                val userLanguageCode = userId?.let { uid ->
                    repository.getUserProfile(uid).value?.languageCode
                }

                // Falls keine Benutzerspracheinstellung vorhanden ist, verwendet es die System- oder App-Einstellung
                val languageCode = userLanguageCode ?: withContext(Dispatchers.IO) {
                    repository.loadLanguageSetting()
                }

                // Aktualisiert die App-Locale mit der gefundenen Spracheinstellung
                val localeListCompat = LocaleListCompat.forLanguageTags(languageCode)
                AppCompatDelegate.setApplicationLocales(localeListCompat)
                _localeList.postValue(localeListCompat)


            } catch (_: Exception) {

            }
        }
    }

    /**
     * Setzt den ausgewählten Sprachcode.
     * Diese Methode wird asynchron ausgeführt und setzt den Wert der LiveData-Variable _selectedLanguageCode.
     * Im Falle eines Fehlers wird die Ausnahme ignoriert und die Methode beendet.
     *
     * @param languageCode: Der Sprachcode, der als ausgewählter Sprachcode gesetzt werden soll.
     */
    fun setSelectedLanguageCode(languageCode: String) {
        viewModelScope.launch {
            try {
                _selectedLanguageCode.value = languageCode
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Ändert die Sprache der Anwendung.
     * Diese Methode wird asynchron ausgeführt und speichert die neue Spracheinstellung in der Datenquelle (Repository).
     * Anschließend wird die App-Locale aktualisiert und die neue Locale in einer LiveData-Variable gespeichert.
     *
     * @param languageCode: Der Sprachcode, der als neue Spracheinstellung gesetzt werden soll.
     */
    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Speichert die Spracheinstellung
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
                _emailExists.value = false
            }
        }
    }





    /**
     * Diese Methode wird verwendet, um das Benutzerprofil asynchron abzurufen.
     * Sie wird innerhalb einer Coroutine ausgeführt, um asynchrone Operationen zu ermöglichen.
     * Zunächst wird die Benutzer-ID von der aktuellen Firebase-Authentifizierung abgerufen.
     * Wenn eine Benutzer-ID vorhanden ist, wird ein Dokumentenverweis auf das Benutzerprofil in Firestore erstellt.
     * Anschließend wird ein erfolgreicher Abruf des Dokuments versucht.
     * Bei Erfolg wird das Dokument in ein Profil-Objekt umgewandelt und in der LiveData-Variable _userProfileData gespeichert.
     * Bei einem Fehler oder wenn keine Benutzer-ID vorhanden ist, wird _userProfileData auf null gesetzt.
     */
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

                _loginStatus.value = false
            }
        }
    }


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
                            viewModelScope.launch {
                                repository.createUserProfile(userId, userProfile)
                            }
                        }
                        _registrationStatus.value = RegistrationStatus(success = true)
                    } else {
                        // Registrierung fehlgeschlagen: Setzt den Registrierungsstatus entsprechend.
                        val message = when (task.exception) {
                            is FirebaseAuthUserCollisionException -> R.string.emailinuse
                            else -> R.string.unkownregistererror
                        }
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

   /**
     * Diese Funktion wird verwendet, um einen neuen Benutzer mithilfe der Google-Authentifizierung zu registrieren.
     * Wird aufgerufen, wenn ein Nutzer versucht, sich mit seinem Google-Konto zu registrieren.
     *
     * @param authToken Das von Google während des Anmeldevorgangs erhaltene ID-Token.
     * @param languageCode Die Sprachpräferenz des Benutzers.
     */
    fun registerGoogleUser(authToken: String, languageCode: String) {
       // Starten Sie eine neue Coroutine, um den Registrierungsprozess durchzuführen.
        viewModelScope.launch {
            // Erstellt mit dem bereitgestellten ID-Token eine Google-Anmeldeinformation.
            val credential = GoogleAuthProvider.getCredential(authToken, null)
            // Versucht, sich mit den erstellten Anmeldeinformationen anzumelden.
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                // Überprüft, ob der Anmeldevorgang erfolgreich war.
                if (task.isSuccessful) {
                   // Überprüft, ob der Benutzer neu ist.
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true
                    if (isNewUser) {
                       // Wenn der Benutzer neu ist, wird ein neues Benutzerprofil erstellt und gespeichert.
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let { user ->
                            val userId = user.uid
                            val userProfile = Profile(
                                isPremium = false,
                                username = user.email ?: "",
                                languageCode = languageCode
                            )
                            viewModelScope.launch {
                                repository.createUserProfile(userId, userProfile)
                            }
                        }
                        // Der Registrierungsstatus wird aktualisiert, um den Erfolg anzuzeigen.
                        _registrationStatus.value = RegistrationStatus(success = true)
                    } else {
                       // Wenn der Benutzer nicht neu ist, ist keine zusätzliche Profilerstellung erforderlich.
                        // Aktualisiert den Registrierungsstatus, um den Erfolg anzuzeigen.
                        _registrationStatus.value = RegistrationStatus(success = true)
                    }
                } else {
                    // Wenn der Anmeldevorgang nicht erfolgreich war, aktualisiert es den Registrierungsstatus, um einen Fehler anzuzeigen.
                    _registrationStatus.value = RegistrationStatus(success = false, message = R.string.authentication_failed)
                }
            }
        }
    }


    /**
     * Diese Methode wird verwendet, um einen gegebenen Text von Englisch nach Deutsch zu übersetzen.
     * Sie prüft zunächst, ob die aktuelle Sprache bereits Deutsch ist. Ist dies der Fall, wird der ursprüngliche Text zurückgegeben, ohne eine Übersetzung durchzuführen.
     * Ansonsten wird eine Coroutine gestartet, um die Übersetzung asynchron durchzuführen.
     * Die Methode versucht, den gegebenen Text mithilfe des Repositorys zu übersetzen und führt dann spezifische Korrekturen an der Übersetzung durch.
     * Bei einem Fehler während der Übersetzung wird der ursprüngliche Text zurückgegeben.
     *
     * @param inputText: Der zu übersetzende Text.
     * @param onComplete: Ein Callback, der aufgerufen wird, wenn die Übersetzung abgeschlossen ist. Der übersetzte Text wird als Parameter übergeben.
     */
    fun translateToGerman(inputText: String, onComplete: (String) -> Unit) {

        val currentLanguageCode = _selectedLanguageCode.value ?: "EN"
        // Prüft, ob die aktuelle Sprache bereits Deutsch ist
        if (currentLanguageCode == "de") {
            onComplete(inputText) // Gibt den ursprünglichen Text zurück, ohne Übersetzung
            return // Beendet die Methode vorzeitig
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


                    else -> translatedText
                }
                onComplete(correctedTranslation)
            } catch (e: Exception) {
                onComplete(inputText) // Gibt im Fehlerfall den Originaltext zurück
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

                    // Bei Erfolg werden die Berufsfelder in der LiveData-Variable gespeichert.
                    _occupationalfields.value = response.getOrNull()!!
                }
            } catch (e: Exception) {
                // Fängt jegliche Ausnahmen beim Abrufen der Berufsfelder ab und setzt die LiveData-Variable auf eine leere Liste.
                _occupationalfields.value = listOf()

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
                    }
                } catch (_: Exception) {

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

                    _worklocations.value = response.getOrNull()!!
                }
            } catch (e: Exception) {
                // Bei einer Ausnahme wird ebenfalls eine leere Liste gesetzt
                _worklocations.value = listOf()

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
                    }
                } catch (_: Exception) {
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


    /**
     * Diese Methode wird verwendet, um asynchron Job-Nominierungen basierend auf dem gegebenen Berufsfeld und Arbeitsort abzurufen.
     * Sie startet eine Coroutine und versucht, die Job-Nominierungen vom Repository zu erhalten.
     * Wenn die Anfrage erfolgreich ist, wird der Wert der LiveData-Variable _jobs auf die erhaltenen Job-Nominierungen gesetzt.
     * Wenn die Anfrage fehlschlägt oder eine Ausnahme auftritt, wird der Wert der LiveData-Variable _jobs auf eine leere Liste gesetzt.
     *
     * @param berufsfeld: Das Berufsfeld, für das Job-Nominierungen abgerufen werden sollen.
     * @param arbeitsort: Der Arbeitsort, für den Job-Nominierungen abgerufen werden sollen.
     */
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


    /**
     * Diese Methode wird verwendet, um eine Liste von Jobtiteln in die im ViewModel ausgewählte Sprache zu übersetzen.
     * Zunächst wird überprüft, ob eine Übersetzung notwendig ist (d.h. der aktuelle Sprachcode ist nicht "de").
     * Wenn eine Übersetzung erforderlich ist, wird jeder Jobtitel asynchron übersetzt.
     * Nachdem alle Jobtitel übersetzt wurden, wird eine Callback-Funktion aufgerufen.
     *
     * @param jobTitles: Die Liste der Jobtitel, die übersetzt werden sollen.
     * @param onComplete: Die Callback-Funktion, die aufgerufen wird, wenn die Übersetzung abgeschlossen ist. Die übersetzten Jobtitel werden als Parameter übergeben.
     */
    fun translateJobTitles(jobTitles: List<String>, onComplete: (List<String>) -> Unit) {
        // Erfasst den aktuellen Wert des Sprachcodes vor dem Start der Coroutine.
        val currentLanguageCode =
            _selectedLanguageCode.value ?: "EN" // Standardwert ist "EN", falls null

        // Prüft, ob der aktuelle Sprachcode "de" ist. Falls ja, führt sich die Methode nicht aus.
        if (currentLanguageCode == "de") {
            onComplete(jobTitles) // Gibt die ursprünglichen Jobtitel zurück, ohne Übersetzung
            return // Beendet die Methode vorzeitig
        }

        viewModelScope.launch {// Startet eine Coroutine, um asynchrone Operationen zu ermöglichen
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


    /**
     * Diese Methode wird verwendet, um asynchron Job-Angebote basierend auf dem gegebenen Berufsfeld und Arbeitsort abzurufen.
     * Sie startet eine Coroutine und versucht, die Job-Angebote vom Repository zu erhalten.
     * Wenn die Anfrage erfolgreich ist, wird der Wert der LiveData-Variable _jobOffers auf die erhaltenen Job-Angebote gesetzt.
     * Wenn die Anfrage fehlschlägt oder eine Ausnahme auftritt, wird der Wert der LiveData-Variable _jobOffers auf eine leere Liste gesetzt.
     *
     * @param was: Das Berufsfeld, für das Job-Angebote abgerufen werden sollen.
     * @param arbeitsort: Der Arbeitsort, für den Job-Angebote abgerufen werden sollen.
     */
    fun fetchJobOffers(was: String, arbeitsort: String) {
        viewModelScope.launch {
            val response = repository.getJobOffers(was, arbeitsort)
            if (response.isSuccess) {
                val jobPairs = response.getOrNull()?.map { it }?.toList() ?: listOf()
                _jobOffers.postValue(jobPairs)
            } else {
                _jobOffers.postValue(listOf())

            }
        }
    }


    /**
     * Übersetzt eine Liste von Job-Angeboten in die im ViewModel ausgewählte Sprache.
     * Die Methode prüft zuerst, ob eine Übersetzung notwendig ist (d.h. der aktuelle Sprachcode ist nicht "de").
     * Wenn eine Übersetzung erforderlich ist, wird jedes Job-Angebot asynchron übersetzt.
     * Nachdem alle Job-Angebote übersetzt wurden, wird eine Callback-Funktion aufgerufen.
     *
     * @param jobOffers: Die Liste der Job-Angebote, die übersetzt werden sollen.
     * @param onComplete: Die Callback-Funktion, die aufgerufen wird, wenn die Übersetzung abgeschlossen ist. Die übersetzten Job-Angebote werden als Parameter übergeben.
     */
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
                        result?.text ?: jobTitle // Verwendet den Originaltitel als Fallback
                    translatedJobOffers.add(Pair(translatedTitle, refNr))
                } catch (e: Exception) {
                    Log.e("translateJobOffers", "Fehler bei der Übersetzung von $jobTitle", e)
                    translatedJobOffers.add(
                        Pair(
                            jobTitle,
                            refNr
                        )
                    )
                }
            }
            onComplete(translatedJobOffers)
        }
    }

    /**
     * Aktualisiert die Job-Angebote asynchron basierend auf dem gegebenen Berufsfeld und Arbeitsort.
     * Diese Methode startet eine Coroutine und versucht, die Job-Angebote zu aktualisieren.
     * Wenn die Anfrage erfolgreich ist, wird die Methode beendet.
     * Wenn die Anfrage fehlschlägt oder eine Ausnahme auftritt, wird der Fehler protokolliert.
     *
     * @param was: Das Berufsfeld, für das Job-Angebote aktualisiert werden sollen.
     * @param arbeitsort: Der Arbeitsort, für den Job-Angebote aktualisiert werden sollen.
     */
    fun updateJobOffers(was: String, arbeitsort: String) {
        viewModelScope.launch {
            try {
                fetchJobOffers(was, arbeitsort)
            } catch (_: Exception) {
            }
        }
    }


    /**
     * Diese Methode wird verwendet, um die ausgewählten Jobs zu aktualisieren und in Firebase zu speichern.
     * Sie startet eine Coroutine und versucht, die ausgewählten Jobs zu aktualisieren.
     * Zunächst wird das aktuelle Benutzerprofil abgerufen. Wenn kein Benutzerprofil vorhanden ist, wird ein neues Profil erstellt.
     * Dann wird die Liste der ausgewählten Jobs mit den neuen ausgewählten Jobs aktualisiert.
     * Das aktualisierte Benutzerprofil wird dann in der LiveData-Variable _userProfileData gespeichert.
     * Schließlich werden die aktualisierten ausgewählten Jobs in Firebase gespeichert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param newSelectedJobs: Eine Map, die die neuen ausgewählten Jobs enthält. Der Schlüssel ist der Jobtitel und der Wert ist die Job-ID.
     */
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
            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um die ausgewählten Jobs asynchron in Firebase zu speichern.
     * Sie startet eine Coroutine und versucht, die ausgewählten Jobs zu speichern.
     * Zunächst wird die Benutzer-ID von der aktuellen Firebase-Authentifizierung abgerufen.
     * Wenn eine Benutzer-ID vorhanden ist, wird die Methode 'updateUserProfileField' des Repositorys aufgerufen, um die ausgewählten Jobs zu speichern.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param selectedJobs: Eine Map, die die ausgewählten Jobs enthält. Der Schlüssel ist der Jobtitel und der Wert ist die Job-ID.
     */
    private fun saveSelectedJobsToFirebase(selectedJobs: Map<String, String>) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                repository.updateUserProfileField(userId, "selectedJobs", selectedJobs)
            } catch (_: Exception) {
            }
        }
    }


    /**
     * Diese Methode wird verwendet, um ein To-Do-Element für einen bestimmten Job zu aktualisieren.
     * Sie startet eine Coroutine und versucht, das To-Do-Element zu aktualisieren.
     * Zunächst wird die Job-ID bereinigt und Dokumentenverweise auf das Benutzerprofil und das To-Do-Dokument erstellt.
     * Dann wird ein Map-Objekt mit den aktualisierten To-Do-Daten erstellt.
     * Anschließend wird versucht, das To-Do-Dokument zu erhalten.
     * Wenn das Dokument existiert, wird das spezifische To-Do-Element aktualisiert.
     * Wenn das Dokument nicht existiert, wird ein neues Dokument mit dem To-Do-Element erstellt.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param userId: Die Benutzer-ID, für die das To-Do-Element aktualisiert werden soll.
     * @param rawJobId: Die rohe Job-ID, für die das To-Do-Element aktualisiert werden soll.
     * @param todoId: Die ID des zu aktualisierenden To-Do-Elements.
     * @param isCompleted: Der aktualisierte Abschlussstatus des To-Do-Elements.
     * @param text: Der aktualisierte Text des To-Do-Elements.
     */
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
                        // Wenn das Dokument existiert, aktualisiert es das spezifische To-Do-Item
                        todoDocRef.update("todos.$todoId", toDoData)

                    } else {
                        // Wenn das Dokument nicht existiert, erstellt ein neues mit dem To-Do-Item
                        val newTodo = mapOf("todos" to mapOf(todoId to toDoData))
                        todoDocRef.set(newTodo)

                    }
                }
            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um To-Do-Elemente für einen bestimmten Job asynchron abzurufen.
     * Sie startet eine Coroutine und versucht, die To-Do-Elemente abzurufen.
     * Zunächst wird die Job-ID bereinigt und ein Dokumentenverweis auf die To-Do-Elemente für den Job erstellt.
     * Anschließend wird ein SnapshotListener hinzugefügt, um Änderungen an den To-Do-Elementen zu verfolgen.
     * Wenn ein Fehler auftritt oder das Snapshot null ist, wird die LiveData-Variable auf eine leere Liste gesetzt.
     * Andernfalls wird die Liste der To-Do-Elemente aus dem Snapshot extrahiert und in der LiveData-Variable gespeichert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird die LiveData-Variable auf eine leere Liste gesetzt.
     *
     * @param userId: Die Benutzer-ID, für die die To-Do-Elemente abgerufen werden sollen.
     * @param jobId: Die Job-ID, für die die To-Do-Elemente abgerufen werden sollen.
     * @return: Eine LiveData-Liste von To-Do-Elementen für den gegebenen Job.
     */
    @Suppress("UNCHECKED_CAST")
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

                        liveData.value = emptyList()
                    } else {
                        val todos =//JobID -> ToDoID -> ToDoData
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


    /**
     * Diese Methode wird verwendet, um den Text eines To-Do-Elements zu aktualisieren.
     * Sie startet eine Coroutine und versucht, den Text des To-Do-Elements zu aktualisieren.
     * Zunächst wird die Job-ID bereinigt und ein Dokumentenverweis auf das To-Do-Element erstellt.
     * Anschließend wird der Pfad zum Text des To-Do-Elements erstellt und der Text des To-Do-Elements aktualisiert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     * Eine mögliche Verbesserung wäre den Dokumentenpfad über eine Helfer-Funktion aufzurufen.
     *
     * @param userId: Die Benutzer-ID, für die das To-Do-Element aktualisiert werden soll.
     * @param jobId: Die Job-ID, für die das To-Do-Element aktualisiert werden soll.
     * @param todoId: Die ID des zu aktualisierenden To-Do-Elements.
     * @param newText: Der neue Text, der für das To-Do-Element gesetzt werden soll.
     */
    fun updateToDoText(userId: String, jobId: String, todoId: String, newText: String) {
        viewModelScope.launch {
            try {
                val sanitizedJobId = sanitizeJobId(jobId)
                val todoDocRef = FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(userId)
                    .collection("todos")
                    .document(sanitizedJobId)


                val todoItemPath = "todos.$todoId.text"
                todoDocRef.update(todoItemPath, newText)

            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um eine ausgewählte Jobauswahl zu löschen.
     * Sie startet eine Coroutine und versucht, die ausgewählte Jobauswahl zu löschen.
     * Zunächst wird das aktuelle Benutzerprofil abgerufen. Wenn kein Benutzerprofil vorhanden ist, wird die Methode beendet.
     * Dann wird die Map der ausgewählten Jobs in eine veränderbare Map umgewandelt oder eine leere veränderbare Map erstellt, falls keine ausgewählten Jobs vorhanden sind.
     * Anschließend wird der Eintrag mit dem gegebenen Jobtitel aus der Map entfernt.
     * Das aktualisierte Benutzerprofil wird dann in der LiveData-Variable _userProfileData gespeichert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param jobTitle: Der Titel des Jobs, der aus der Auswahl entfernt werden soll.
     */
    fun deleteJobSelection(jobTitle: String) {
        viewModelScope.launch {
            try {
                val currentProfile = _userProfileData.value
                    ?: return@launch // Beendet die Methode, falls kein Profil vorhanden ist.
                val updatedJobs = currentProfile.selectedJobs?.toMutableMap() ?: mutableMapOf()

                updatedJobs.remove(jobTitle) // Entfernt den Eintrag sicher aus der Map.

                currentProfile.selectedJobs = updatedJobs // Aktualisiert die Map im Profil.
                _userProfileData.value = currentProfile // Setzt das aktualisierte Profil.
            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um den gewünschten Arbeitsort des Benutzers in Firebase zu speichern.
     * Sie startet eine Coroutine und versucht, den gewünschten Arbeitsort zu speichern.
     * Zunächst wird die Benutzer-ID von der aktuellen Firebase-Authentifizierung abgerufen.
     * Wenn eine Benutzer-ID vorhanden ist, wird die Methode 'updateUserProfileField' des Repositorys aufgerufen, um den gewünschten Arbeitsort zu speichern.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param desiredLocation: Der gewünschte Arbeitsort, der in Firebase gespeichert werden soll.
     */
    fun savedesiredLocationToFirebase(desiredLocation: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                repository.updateUserProfileField(userId, "desiredLocation", desiredLocation)
            } catch (_: Exception) {

            }
        }
    }


    /**
     * Diese Methode wird verwendet, um Jobdetails basierend auf der bereitgestellten codierten Hash-ID asynchron abzurufen.
     * Sie startet eine Coroutine und versucht, die Jobdetails aus dem Repository abzurufen.
     * Wenn die Anforderung erfolgreich ist, wird die LiveData-Variable _jobDetails mit dem Ergebnis aktualisiert.
     * Wenn die Anforderung fehlschlägt oder eine Ausnahme auftritt, wird _jobDetails mit dem Fehlerergebnis aktualisiert.
     *
     * @param encodedHashID: Die codierte Referenznummer, für die Jobdetails abgerufen werden sollen.
     */
    fun fetchJobDetails(encodedHashID: String) {
        viewModelScope.launch {
            try {
                val result = repository.getJobDetails(encodedHashID)
                _jobDetails.value = result
            } catch (e: Exception) {
                _jobDetails.value = Result.failure(e)
            }
        }
    }

    /**
     * Diese Methode wird verwendet, um Jobdetails in die im ViewModel ausgewählte Sprache zu übersetzen.
     * Zunächst wird überprüft, ob eine Übersetzung notwendig ist (d.h. der aktuelle Sprachcode ist nicht "de").
     * Wenn eine Übersetzung erforderlich ist, wird jedes Feld der Jobdetails asynchron übersetzt.
     * Nachdem alle Felder übersetzt wurden, wird eine Callback-Funktion aufgerufen.
     *
     * @param jobDetails: Das JobDetailsResponse-Objekt, das übersetzt werden soll.
     * @param onComplete: Die Callback-Funktion, die aufgerufen wird, wenn die Übersetzung abgeschlossen ist. Das übersetzte JobDetailsResponse-Objekt wird als Parameter übergeben.
     */
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

                // Erstellt ein neues JobDetailsResponse-Objekt mit den übersetzten Werten
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

                onComplete(jobDetails)
            }
        }
    }


    //TODO Mehrere Beginntermine laden können !

    /**
     * Diese Methode wird verwendet, um Bildungsangebote asynchron abzurufen.
     * Sie startet eine Coroutine und versucht, die Bildungsangebote aus dem Repository abzurufen.
     * Wenn die Anforderung erfolgreich ist, wird die LiveData-Variable _educationaloffers mit den erhaltenen Bildungsangeboten aktualisiert.
     * Wenn die Anforderung fehlschlägt oder eine Ausnahme auftritt, wird _educationaloffers mit einer leeren Liste aktualisiert.
     *
     * @param systematiken: Die Systematiken, für die Bildungsangebote abgerufen werden sollen.
     * @param orte: Die Orte, für die Bildungsangebote abgerufen werden sollen.
     * @param sprachniveau: Das Sprachniveau, für das Bildungsangebote abgerufen werden sollen.
     * @param beginntermine: Die Beginntermine, für die Bildungsangebote abgerufen werden sollen.
     */
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
                }
            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um Bildungsangebote in die im ViewModel ausgewählte Sprache zu übersetzen.
     * Zunächst wird überprüft, ob eine Übersetzung notwendig ist (d.h. der aktuelle Sprachcode ist nicht "de").
     * Wenn eine Übersetzung erforderlich ist, wird jedes Bildungsangebot asynchron übersetzt.
     * Nachdem alle Bildungsangebote übersetzt wurden, wird eine Callback-Funktion aufgerufen.
     *
     * @param bildungsangebote: Die Liste der Bildungsangebote, die übersetzt werden sollen.
     * @param onComplete: Die Callback-Funktion, die aufgerufen wird, wenn die Übersetzung abgeschlossen ist. Die übersetzten Bildungsangebote werden als Parameter übergeben.
     */
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

                onComplete(bildungsangebote) // Gibt die ursprünglichen Bildungsangebote zurück, falls ein Fehler auftritt
            }
        }
    }


    /**
     * Diese Methode wird verwendet, um das Profilbild des Benutzers zu aktualisieren.
     * Sie startet eine Coroutine und versucht, das Profilbild zu aktualisieren.
     * Zunächst wird die Benutzer-ID von der aktuellen Firebase-Authentifizierung abgerufen. Wenn keine Benutzer-ID vorhanden ist, wird eine Ausnahme ausgelöst.
     * Dann werden Referenzen auf Firebase Storage und Firestore erstellt.
     * Anschließend wird eine Referenz auf das Profilbild im Storage erstellt und das neue Bild wird hochgeladen.
     * Nach dem erfolgreichen Hochladen wird die URL des Bildes abgerufen und das Profilbild in Firestore aktualisiert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param uri: Die Uri des neuen Profilbildes.
     */
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


    /**
     * Diese Methode wird verwendet, um eine neue Lernkarte hinzuzufügen.
     * Sie startet eine Coroutine und versucht, die Lernkarte zu erstellen und in Firestore zu speichern.
     * Zunächst wird eine neue Lernkarte mit dem gegebenen Vorder- und Rücktext erstellt.
     * Dann wird eine Referenz auf das Firestore-Dokument für die neue Lernkarte erstellt.
     * Die ID des Firestore-Dokuments wird als ID der Lernkarte gesetzt.
     * Schließlich wird die Lernkarte im Firestore-Dokument gespeichert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param userId: Die Benutzer-ID, für die die Lernkarte hinzugefügt werden soll.
     * @param frontText: Der Text, der auf der Vorderseite der Lernkarte angezeigt werden soll.
     * @param backText: Der Text, der auf der Rückseite der Lernkarte angezeigt werden soll.
     */
    fun addFlashcard(userId: String, frontText: String, backText: String) {
        viewModelScope.launch {
            try {
                val newCard = FlashCard(frontText = frontText, backText = backText)
                val docRef = FirebaseFirestore.getInstance().collection("user").document(userId)
                    .collection("flashcards").document()
                newCard.id = docRef.id  // Setzt die Firestore-ID als die ID der Karte
                docRef.set(newCard)
            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um eine Flashcard zu aktualisieren.
     * Sie startet eine Coroutine und versucht, die Flashcard zu aktualisieren.
     * Zunächst wird eine Referenz auf das Firestore-Dokument für die Flashcard erstellt.
     * Dann wird eine Map erstellt, die die aktualisierten Werte enthält.
     * Die Map wird dann verwendet, um das Firestore-Dokument zu aktualisieren.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param userId: Die Benutzer-ID, für die die Flashcard aktualisiert werden soll.
     * @param cardId: Die ID der zu aktualisierenden Flashcard.
     * @param frontText: Der neue Text, der auf der Vorderseite der Flashcard angezeigt werden soll. Wenn null, wird der Text nicht aktualisiert.
     * @param backText: Der neue Text, der auf der Rückseite der Flashcard angezeigt werden soll. Wenn null, wird der Text nicht aktualisiert.
     */
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

    /**
     * Diese Methode wird verwendet, um Flashcards für einen bestimmten Benutzer asynchron abzurufen.
     * Sie startet eine Coroutine und versucht, die Flashcards aus Firestore abzurufen.
     * Zunächst wird eine MutableLiveData für die Flashcards erstellt.
     * Dann wird ein SnapshotListener hinzugefügt, um Änderungen an den Flashcards zu verfolgen.
     * Wenn ein Fehler auftritt oder das Snapshot null ist, wird die LiveData-Variable auf eine leere Liste gesetzt.
     * Andernfalls wird die Liste der Flashcards aus dem Snapshot extrahiert und in der LiveData-Variable gespeichert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird die LiveData-Variable auf eine leere Liste gesetzt.
     *
     * @param userId: Die Benutzer-ID, für die die Flashcards abgerufen werden sollen.
     * @return: Eine LiveData-Liste von Flashcards für den gegebenen Benutzer.
     */
    fun getFlashcards(userId: String): LiveData<List<FlashCard>> {
        val liveData = MutableLiveData<List<FlashCard>>()
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance().collection("user").document(userId)
                    .collection("flashcards")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            // Handle the error
                            liveData.postValue(emptyList())
                        } else {
                            val flashcards = snapshot?.toObjects(FlashCard::class.java)
                            liveData.postValue(flashcards ?: emptyList())
                        }
                    }
            } catch (_: Exception) {
            }
        }
        return liveData
    }

    /**
     * Diese Methode wird verwendet, um ein To-Do-Element zu aktualisieren.
     * Sie startet eine Coroutine und versucht, das To-Do-Element zu aktualisieren.
     * Zunächst wird eine Referenz auf das Firestore-Dokument für das To-Do-Element erstellt.
     * Dann wird eine Map erstellt, die die aktualisierten Werte enthält.
     * Die Map wird dann verwendet, um das Firestore-Dokument zu aktualisieren.
     * Wenn das Dokument existiert, wird das spezifische To-Do-Element aktualisiert.
     * Wenn das Dokument nicht existiert, wird ein neues Dokument mit dem To-Do-Element erstellt.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param userId: Die Benutzer-ID, für die das To-Do-Element aktualisiert werden soll.
     * @param todoId: Die ID des zu aktualisierenden To-Do-Elements.
     * @param isCompleted: Der aktualisierte Abschlussstatus des To-Do-Elements.
     * @param text: Der aktualisierte Text des To-Do-Elements.
     */
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

                        } else {
                            todoDocRef.set(toDoData)

                        }
                    }
                }
            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um To-Do-Elemente für einen bestimmten Benutzer asynchron abzurufen.
     * Sie startet eine Coroutine und versucht, die To-Do-Elemente aus Firestore abzurufen.
     * Zunächst wird eine MutableLiveData für die To-Do-Elemente erstellt.
     * Dann wird ein SnapshotListener hinzugefügt, um Änderungen an den To-Do-Elementen zu verfolgen.
     * Wenn ein Fehler auftritt oder das Snapshot null ist, wird die LiveData-Variable auf eine leere Liste gesetzt.
     * Andernfalls wird die Liste der To-Do-Elemente aus dem Snapshot extrahiert und in der LiveData-Variable gespeichert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird die LiveData-Variable auf eine leere Liste gesetzt.
     *
     * @param userId: Die Benutzer-ID, für die die To-Do-Elemente abgerufen werden sollen.
     * @return: Eine LiveData-Liste von To-Do-Elementen für den gegebenen Benutzer.
     */
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

                        liveData.postValue(emptyList())
                    } else {
                        val toDoItems = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(ToDoItemRelocation::class.java)?.apply { id = doc.id }
                        } ?: emptyList()
                        liveData.postValue(toDoItems)
                    }
                }
            } catch (_: Exception) {

            }
        }

        return liveData
    }

    /**
     * Diese Methode wird verwendet, um den Text eines To-Do-Elements zu aktualisieren.
     * Sie startet eine Coroutine und versucht, den Text des To-Do-Elements zu aktualisieren.
     * Zunächst wird eine Referenz auf das Firestore-Dokument für das To-Do-Element erstellt.
     * Anschließend wird der Text des To-Do-Elements aktualisiert.
     * Wenn das Dokument existiert, wird der Text des spezifischen To-Do-Elements aktualisiert.
     * Wenn das Dokument nicht existiert, wird ein neues Dokument mit dem To-Do-Element erstellt.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param userId: Die Benutzer-ID, für die das To-Do-Element aktualisiert werden soll.
     * @param todoId: Die ID des zu aktualisierenden To-Do-Elements.
     * @param newText: Der neue Text, der für das To-Do-Element gesetzt werden soll.
     */
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

                        } else {
                            val toDoData = mapOf(
                                "erledigt" to false,
                                "text" to newText
                            )
                            todoDocRef.set(toDoData)

                        }
                    }
                }
            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um ein To-Do-Element zu löschen.
     * Sie startet eine Coroutine und versucht, das To-Do-Element zu löschen.
     * Zunächst wird eine Referenz auf das Firestore-Dokument für das To-Do-Element erstellt.
     * Dann wird überprüft, ob das Dokument existiert. Wenn es existiert, wird das Dokument gelöscht.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param userId: Die Benutzer-ID, für die das To-Do-Element gelöscht werden soll.
     * @param todoId: Die ID des zu löschenden To-Do-Elements.
     */
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

                    }
                }

            } catch (_: Exception) {

            }
        }
    }


    /**
     * Ruft den Benutzernamen für die gegebene Benutzer-ID aus Firestore ab.
     *
     * Diese Funktion erstellt eine LiveData-Instanz, um den Benutzernamen zu speichern. Dann startet sie eine Coroutine,
     * um den Benutzernamen aus Firestore abzurufen. Die Funktion fügt einen SnapshotListener zum Firestore-Dokument hinzu,
     * das mit der Benutzer-ID verknüpft ist. Wenn beim Abrufen des Dokuments ein Fehler auftritt, postet die Funktion einen
     * leeren String in die LiveData. Wenn das Dokument erfolgreich abgerufen wird, holt die Funktion den Benutzernamen aus
     * dem Dokument und postet ihn in die LiveData.
     *
     * @param userId: Die ID des Benutzers, dessen Benutzername abgerufen werden soll.
     * @return: Eine LiveData-Instanz, die den abgerufenen Benutzernamen enthält. Wenn beim Abrufen des Benutzernamens ein
     * Fehler auftritt, enthält die LiveData einen leeren String.
     */
    fun getUserName(userId: String): LiveData<String> {
        val liveData = MutableLiveData<String>()
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("user").document(userId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        // Handle the error
                        liveData.postValue("")
                    } else {
                        val userName = documentSnapshot?.getString("name")
                        liveData.postValue(userName ?: "")
                    }
                }
        }
        return liveData
    }


    /**
     * Ruft das Sprachniveau eines Benutzers basierend auf der bereitgestellten Benutzer-ID aus Firestore ab.
     *
     * Diese Funktion erstellt eine LiveData-Instanz, um das Sprachniveau zu speichern. Sie startet dann eine Coroutine,
     * um das Sprachniveau aus Firestore abzurufen. Die Funktion fügt einen SnapshotListener zum Firestore-Dokument hinzu,
     * das mit der Benutzer-ID verknüpft ist. Wenn beim Abrufen des Dokuments ein Fehler auftritt, postet die Funktion einen
     * leeren String in die LiveData. Wenn das Dokument erfolgreich abgerufen wird, holt die Funktion das Sprachniveau aus
     * dem Dokument und postet es in die LiveData.
     *
     * @param userId: Die ID des Benutzers, dessen Sprachniveau abgerufen werden soll.
     * @return: Eine LiveData-Instanz, die das abgerufene Sprachniveau enthält. Wenn beim Abrufen des Sprachniveaus ein
     * Fehler auftritt, enthält die LiveData einen leeren String.
     */
    fun getUserLanguageLevel(userId: String): LiveData<String> {
        val liveData = MutableLiveData<String>()
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("user").document(userId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        // Handle the error
                        liveData.postValue("")
                    } else {
                        val languageLevel = documentSnapshot?.getString("languageLevel")
                        liveData.postValue(languageLevel ?: "")
                    }
                }
        }
        return liveData
    }


    /**
     * Ruft die Profilbild-URL eines Benutzers basierend auf der bereitgestellten Benutzer-ID aus Firestore ab.
     *
     * Diese Funktion erstellt eine LiveData-Instanz, um die Profilbild-URL zu speichern. Sie startet dann eine Coroutine,
     * um die Profilbild-URL aus Firestore abzurufen. Die Funktion fügt einen SnapshotListener zum Firestore-Dokument hinzu,
     * das mit der Benutzer-ID verknüpft ist. Wenn beim Abrufen des Dokuments ein Fehler auftritt, postet die Funktion einen
     * leeren String in die LiveData. Wenn das Dokument erfolgreich abgerufen wird, holt die Funktion die Profilbild-URL aus
     * dem Dokument und postet sie in die LiveData.
     *
     * @param userId: Die ID des Benutzers, dessen Profilbild-URL abgerufen werden soll.
     * @return: Eine LiveData-Instanz, die die abgerufene Profilbild-URL enthält. Wenn beim Abrufen der Profilbild-URL ein
     * Fehler auftritt, enthält die LiveData einen leeren String.
     */
    fun getUserProfileImageUrl(userId: String): LiveData<String> {
        val liveData = MutableLiveData<String>()
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("user").document(userId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        // Handle the error
                        liveData.postValue("")
                    } else {
                        val imageUrl = documentSnapshot?.getString("profilePicture")
                        liveData.postValue(imageUrl ?: "")
                    }
                }
        }
        return liveData
    }


    /**
     * Diese Methode wird verwendet, um Feedback zu speichern.
     * Sie startet eine Coroutine und versucht, das Feedback in Firestore zu speichern.
     * Zunächst wird eine Referenz auf das Firestore-Dokument für das Feedback erstellt.
     * Dann wird eine Map erstellt, die die Feedback-Daten enthält.
     * Die Map wird dann verwendet, um das Firestore-Dokument zu erstellen.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param userId: Die Benutzer-ID, für die das Feedback gespeichert werden soll.
     * @param designRating: Die Bewertung des Designs.
     * @param functionalityRating: Die Bewertung der Funktionalität.
     * @param overallRating: Die Gesamtbewertung.
     * @param generalFeedback: Allgemeines Feedback.
     */
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

            } catch (_: Exception) {

            }
        }
    }


    /**
     * Diese Methode wird verwendet, um das Sprachniveau des Benutzers in Firebase zu speichern.
     * Sie startet eine Coroutine und versucht, das Sprachniveau in Firestore zu speichern.
     * Zunächst wird die Benutzer-ID von der aktuellen Firebase-Authentifizierung abgerufen. Wenn keine Benutzer-ID vorhanden ist, wird die Coroutine beendet.
     * Dann wird die Methode 'updateUserProfileField' des Repositorys aufgerufen, um das Sprachniveau zu speichern.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     *
     * @param languageLevel: Das Sprachniveau, das in Firebase gespeichert werden soll.
     */
    fun saveLanguageLevelToFirebase(languageLevel: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                repository.updateUserProfileField(userId, "languageLevel", languageLevel)
            } catch (_: Exception) {

            }
        }
    }

    /**
     * Diese Methode wird verwendet, um das Benutzerkonto zu löschen.
     * Sie startet eine Coroutine und versucht, das Benutzerkonto zu löschen.
     * Zunächst wird die aktuelle Benutzerinstanz von FirebaseAuth abgerufen.
     * Wenn eine Benutzerinstanz vorhanden ist, wird die Methode 'delete' aufgerufen, um das Benutzerkonto zu löschen.
     * Nachdem der Löschvorgang abgeschlossen ist, wird der Erfolgsstatus des Vorgangs in der LiveData-Variable _deleteAccountStatus gespeichert.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                user?.delete()?.addOnCompleteListener { task ->
                    _deleteAccountStatus.value = task.isSuccessful
                }
            } catch (_: Exception) {

            }
        }
    }


    /**
     * Diese Methode wird verwendet, um den Benutzer auszuloggen.
     * Sie startet eine Coroutine und versucht, den Benutzer auszuloggen.
     * Zunächst wird die Methode 'signOut' aufgerufen, um den Benutzer auszuloggen.
     * Dann wird der Wert der LiveData-Variable _userProfileData auf null gesetzt, um das Benutzerprofil zu löschen.
     * Schließlich wird der Wert der LiveData-Variable _loginStatus auf false gesetzt, um den Anmeldestatus zu aktualisieren.
     * Wenn während des Prozesses eine Ausnahme auftritt, wird diese ignoriert.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _userProfileData.value = null
                _loginStatus.value = false

            } catch (_: Exception) {

            }
        }
    }


}


