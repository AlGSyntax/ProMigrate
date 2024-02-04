package com.example.promigrate

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.promigrate.data.model.Profile
import com.example.promigrate.data.repository.Repository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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
        FirebaseFirestore.getInstance()
    )


    private val _localeList = MutableLiveData<LocaleListCompat>()
    val localeList: LiveData<LocaleListCompat> = _localeList

    private val _selectedLanguageCode = MutableLiveData<String>()
    val selectedLanguageCode: LiveData<String> = _selectedLanguageCode


    private val _registrationStatus = MutableLiveData<Boolean>()
    val registrationStatus: LiveData<Boolean> = _registrationStatus

    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> = _loginStatus


    private val auth = Firebase.auth

    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user



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
            _registrationStatus.value = false
            Log.e(TAG, "Passwörter stimmen nicht überein")
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
                    _registrationStatus.value = true
                } else {
                    task.exception?.let {
                        Log.e(TAG, "Fehler beim Registrieren des Benutzers", it)
                    }
                    _registrationStatus.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ausnahme in der Registrationsmethode", e)
            _registrationStatus.value = false
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
    }


    private fun loadUserLanguageSetting(userId: String?) {
        userId?.let { uid ->
            viewModelScope.launch {
                try {
                    val userProfile = repository.getUserProfile(uid).value
                    userProfile?.let { profile ->
                        updateAppLocale(profile.languageCode)
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


