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
import com.example.promigrate.data.model.Profile
import com.example.promigrate.data.model.RegistrationStatus
import com.example.promigrate.data.remote.DeepLApiService
import com.example.promigrate.data.remote.ProMigrateAPI
import com.example.promigrate.data.repository.Repository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val TAG = "MainViewModel"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var repository = Repository.getInstance(
        application, FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance(), ProMigrateAPI.retrofitService, DeepLApiService.create()
    )


    private val _localeList = MutableLiveData<LocaleListCompat>()
    val localeList: LiveData<LocaleListCompat> = _localeList

    private val _selectedLanguageCode = MutableLiveData<String>()
    val selectedLanguageCode: LiveData<String> = _selectedLanguageCode


    private val _registrationStatus = MutableLiveData<RegistrationStatus>()
    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus

    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> = _loginStatus


    private val auth = Firebase.auth

    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user

    private val _jobs = MutableLiveData<List<String>>()
    val jobs: LiveData<List<String>> = _jobs

    private val _berufsfelder = MutableLiveData<List<String>>()
    val berufsfelder: LiveData<List<String>> = _berufsfelder

    private val _arbeitsorte = MutableLiveData<List<String>>()
    val arbeitsorte: LiveData<List<String>> = _arbeitsorte

    private val translationResult = MutableLiveData<String>()





    //TODO LiveData = UI-Aktualisierungen ,Berechnungen = lokale Variablen, slider Int lokal speichern


    init {
        loadLanguageSetting()
        setupUserEnv()
    }

    /**
    fun loadUserProfile(userId: String): MutableLiveData<UserProfile?> {
    return repository.getUserProfile(userId)
    }
     */

    fun setSelectedLanguageCode(languageCode: String) {
        _selectedLanguageCode.value = languageCode
    }

    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Speichere die Spracheinstellung
                    repository.saveLanguageSetting(languageCode)
                    Log.d(TAG, "Spracheinstellung gespeichert: $languageCode")
                }
                val localeListCompat = LocaleListCompat.forLanguageTags(languageCode)
                AppCompatDelegate.setApplicationLocales(localeListCompat)
                _localeList.postValue(localeListCompat)
                Log.d(TAG, "Sprache geändert zu: $languageCode")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Ändern der Sprache", e)
            }
        }
    }

    private fun loadLanguageSetting() {
        viewModelScope.launch {
            try {
                val languageCode = withContext(Dispatchers.IO) {
                    // Lade die gespeicherte Spracheinstellung
                    repository.loadLanguageSetting().also {
                        Log.d(TAG, "Geladene Spracheinstellung: $it")
                    }
                }
                val localeListCompat = LocaleListCompat.forLanguageTags(languageCode)
                AppCompatDelegate.setApplicationLocales(localeListCompat)
                _localeList.postValue(localeListCompat)
                Log.d(TAG, "Sprache geladen: $languageCode")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Laden der Spracheinstellung", e)
            }
        }
    }

    private fun setupUserEnv() {
        try {
            _user.value = auth.currentUser
            auth.currentUser?.let { firebaseUser ->
                // Rufe die Methode aus dem Repository auf, um die Umgebung einzurichten
                repository.setupUserEnvironment(firebaseUser)
            }
            Log.d("setupUserEnv", "Benutzerumgebungseinrichtung erfolgreich")
        } catch (e: Exception) {
            Log.e("setupUserEnv", "Fehler beim Einrichten der Benutzerumgebung", e)
        }
    }


    fun register(email: String, password: String, confirmPassword: String, languageCode: String) {
        if (password != confirmPassword) {
            _registrationStatus.value = RegistrationStatus(success = false, message = "Passwörter stimmen nicht überein")
            return
        }
        Log.d(TAG, "Versuche, den Benutzer zu registrieren mit E-Mail: $email")
        try {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Benutzer erfolgreich registriert mit E-Mail: $email")
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        // Erstelle das Benutzerprofil mit der User-ID und dem Profilobjekt
                        val userId = user.uid
                        val userProfile = Profile(isPremium = false, username = email, languageCode = languageCode)
                        repository.createUserProfile(userId, userProfile) // Rufe die Methode aus dem Repository auf
                    }
                    _registrationStatus.value = RegistrationStatus(success = true)
                } else {
                    val message = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> R.string.emailinuse
                        else -> R.string.unkownregistererror
                    }
                    Log.e(TAG, message.toString(), task.exception)
                    _registrationStatus.value = RegistrationStatus(success = false, message = message)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme in der Registrationsmethode", e)
            _registrationStatus.value = RegistrationStatus(success = false, message = "Ausnahme in der Registrationsmethode: ${e.localizedMessage}")
        }
    }





    fun login(email: String, password: String) {
        Log.d(TAG, "Versuche, Benutzer einzuloggen mit E-Mail: $email")
        try {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Benutzer erfolgreich eingeloggt mit E-Mail: $email")
                    setupUserEnv()
                    loadUserLanguageSetting(auth.currentUser?.uid)
                    _loginStatus.value =
                        true // Du müsstest eine LiveData hinzufügen, ähnlich wie bei der Registrierung
                } else {
                    task.exception?.let {
                        Log.e(TAG, "Fehler beim Einloggen des Benutzers", it)
                    }
                    _loginStatus.value = false // Setze den Status entsprechend
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme in der LogIn-Methode", e)
            _loginStatus.value = false
        }
    }

    fun onGoogleLoginClicked(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Anmeldung erfolgreich
                    _loginStatus.value = true
                    loadUserLanguageSetting(auth.currentUser?.uid)
                } else {
                    // Anmeldung fehlgeschlagen
                    _loginStatus.value = false
                }
            }
    }// TODO:wie kriege Ich das hin das wenn der Benutzer sich der Benutzer einmal über Google angemeldet hat direkt für immer eingeloggt bleibt und direkt zu einem hypotethischen DashboardFragment springt ?


    private fun loadUserLanguageSetting(userId: String?) {
        userId?.let { uid ->
            viewModelScope.launch {
                try {
                    val userProfile = repository.getUserProfile(uid).value
                    userProfile?.let { profile ->
                        profile.languageCode?.let { updateAppLocale(it) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler beim Laden der Spracheinstellung", e)
                }
            }
        }
    }

    private fun updateAppLocale(languageCode: String) {
        val localeListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeListCompat)
        Log.d(TAG, "Sprache geändert zu: $languageCode")
    }



    fun saveProfileWithImage(uri: Uri, name: String, age: String, fieldOfWork: String,
                             isDataProtected: Boolean, languageLevel: Int,
                             desiredLocation:String,street:String,birthplace:String,maidenname:String,
                             firstname:String,lastname:String,phonenumber:String) {
        viewModelScope.launch {
            try {
                val userId = repository.firebaseAuth.currentUser?.uid ?: throw Exception("Nicht angemeldet")
                val imageUrl = repository.uploadAndUpdateProfilePicture(uri, userId)
                Log.d(TAG, "Profilbild erfolgreich aktualisiert: $imageUrl")

                val ageInt = age.toIntOrNull() ?: 0

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

                repository.updateUserProfile(userId, profileData)
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Speichern des Profils", e)
            }
        }
    }

/**
    fun getJobs(was: String?, wo: String?, berufsfeld: String?) {
        viewModelScope.launch {
            try {
                _jobs.value = repository.getJobs(was, wo, berufsfeld)
            } catch (e: Exception) {
                Log.e("JobFetch", "Fehler beim Abrufen der Jobs: ${e.message}")
            }
        }
    }
*/

    fun translateText(inputText: String) {
        viewModelScope.launch {
            Log.d("translateText", "Übersetzung startet: Eingabetext = $inputText, Zielsprache = DE")
            try {
                val result = repository.translateText(inputText, "DE") // DE für Deutsch
                if (result != null) {
                    Log.d("translateText", "Übersetzung erfolgreich, Ergebnis = ${result.text}")
                }
                if (result != null) {
                    translationResult.postValue(result.text)
                }
            } catch (e: Exception) {
                Log.e("translateText", "Fehler bei der Übersetzung", e)
            }
        }
    }

    // Im ViewModel
    fun translateBerufsfelder(berufsfelder: List<String>, onComplete: (List<String>) -> Unit) {
        // Erfasse den aktuellen Wert des Sprachcodes vor dem Start der Coroutine
        val currentLanguageCode = _selectedLanguageCode.value ?: "EN" // Standardwert ist "EN", falls null

        // Prüfe, ob der aktuelle Sprachcode "de" ist. Falls ja, führe die Methode nicht aus.
        if (currentLanguageCode == "de") {
            onComplete(berufsfelder) // Gebe die ursprünglichen Berufsfelder zurück, ohne Übersetzung
            return // Beende die Methode vorzeitig
        }

        viewModelScope.launch {
            val translatedBerufsfelder = mutableListOf<String>()
            berufsfelder.forEach { berufsfeld ->
                try {
                    // Verwende currentLanguageCode als Ziel für die Übersetzung
                    val result = repository.translateText(berufsfeld, currentLanguageCode)
                    result?.text?.let {
                        translatedBerufsfelder.add(it)
                        Log.d("translateBerufsfelder", "Übersetzt: $berufsfeld zu $it")
                    }
                } catch (e: Exception) {
                    Log.e("translateBerufsfelder", "Fehler bei der Übersetzung von $berufsfeld", e)
                }
            }
            onComplete(translatedBerufsfelder)
        }
    }

    fun translateArbeitsorte(arbeitsorte: List<String>, onComplete: (List<String>) -> Unit) {
        // Erfasse den aktuellen Wert des Sprachcodes vor dem Start der Coroutine
        val currentLanguageCode = _selectedLanguageCode.value ?: "EN" // Standardwert ist "EN", falls null

        // Prüfe, ob der aktuelle Sprachcode "de" ist. Falls ja, führe die Methode nicht aus.
        if (currentLanguageCode == "de") {
            onComplete(arbeitsorte) // Gebe die ursprünglichen Arbeitsorte zurück, ohne Übersetzung
            return // Beende die Methode vorzeitig
        }

        viewModelScope.launch {
            val translatedArbeitsorte = mutableListOf<String>()
            arbeitsorte.forEach { arbeitsort ->
                try {
                    // Verwende currentLanguageCode als Ziel für die Übersetzung
                    val result = repository.translateText(arbeitsort, currentLanguageCode)
                    result?.text?.let {
                        translatedArbeitsorte.add(it)
                        Log.d("translateArbeitsorte", "Übersetzt: $arbeitsort zu $it")
                    }
                } catch (e: Exception) {
                    Log.e("translateArbeitsorte", "Fehler bei der Übersetzung von $arbeitsort", e)
                }
            }
            onComplete(translatedArbeitsorte)
        }
    }






    fun fetchBerufsfelder() {
        viewModelScope.launch {
            try {
                val response = repository.getBerufsfelder()
                if (response.isSuccess) {
                    Log.d(TAG, "Berufsfelder erfolgreich abgerufen.")
                    _berufsfelder.value = response.getOrNull()
                } else {
                    // Im Fehlerfall könnte ein vordefinierter Fehlerwert oder eine leere Liste gesetzt werden
                    _berufsfelder.value = listOf()
                    Log.e(TAG, "Fehler beim Abrufen der Berufsfelder: ${response.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _berufsfelder.value = listOf()
                Log.e(TAG, "Fehler beim Abrufen der Berufsfelder: ${e.message}")
            }
        }
    }

    fun fetchArbeitsorte() {
        viewModelScope.launch {
            try {
                val response = repository.getArbeitsorte()
                if (response.isSuccess) {
                    Log.d(TAG, "Arbeitsorte erfolgreich abgerufen.")
                    _arbeitsorte.value = response.getOrNull()
                } else {
                    // Im Fehlerfall könnte ein vordefinierter Fehlerwert oder eine leere Liste gesetzt werden
                    _arbeitsorte.value = listOf()
                    Log.e(TAG, "Fehler beim Abrufen der Arbeitsorte: ${response.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _arbeitsorte.value = listOf()
                Log.e(TAG, "Fehler beim Abrufen der Arbeitsorte: ${e.message}")
            }
        }
    }

    // Im ViewModel
    fun fetchJobs(wo: String, arbeitsort: String) {
        viewModelScope.launch {
            try {
                val response = repository.getJobs(wo, arbeitsort)
                if (response.isSuccess) {
                    Log.d(TAG, "Jobs erfolgreich abgerufen.")
                    _jobs.value = response.getOrNull() ?: listOf()
                } else {
                    _jobs.value = listOf()
                    Log.e(TAG, "Fehler beim Abrufen der Jobs: ${response.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _jobs.value = listOf()
                Log.e(TAG, "Fehler beim Abrufen der Jobs: ${e.message}")
            }
        }
    }




    /**
    fun saveUserProfile(userId: String, languageCode: String) {
    viewModelScope.launch {
    try {
    repository.saveUserProfile(UserProfile(userId, languageCode))
    Log.d(TAG, "Benutzerprofil erfolgreich gespeichert: $userId")
    } catch (e: Exception) {
    Log.e(TAG, "Fehler beim Speichern des Benutzerprofils", e)
    }
    }
    }

    fun loadUserProfile(userId: String): LiveData<UserProfile> {
    return try {
    val userProfile = repository.getUserProfile(userId)
    Log.d(TAG, "Benutzerprofil erfolgreich geladen: $userId")
    userProfile
    } catch (e: Exception) {
    Log.e(TAG, "Fehler beim Laden des Benutzerprofils", e)
    MutableLiveData() // Rückgabe eines leeren LiveData-Objekts im Fehlerfall
    }
    }

     */


    fun logout() {
        try {
            auth.signOut()
            setupUserEnv()
            Log.d("logout", "Benutzer erfolgreich ausgeloggt")
        } catch (e: Exception) {
            Log.e("logout", "Fehler in der LogOut-Methode", e)
        }
    }


}


