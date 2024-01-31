package com.example.promigrate

import android.app.Application
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.promigrate.data.model.Profile
import com.example.promigrate.data.model.UserProfile
import com.example.promigrate.data.repository.Repository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


const val TAG = "MainViewModel"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var repository = Repository.getInstance(application)


    private val _localeList = MutableLiveData<LocaleListCompat>()
    val localeList: LiveData<LocaleListCompat> = _localeList

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user
    //Das profile Document enthält ein einzelnes Profil(das des eingeloggten Users)
    //Document ist wie ein Objekt
    private lateinit var profileRef: DocumentReference
    //Die note Collection enthält beliebig viele Notes(alle notes des eingeloggten Users
    //Collection ist wie eine Liste
    private lateinit var notesRef: CollectionReference





    init {
        loadLanguageSetting()
        setupUserEnv()
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


    private fun setupUserEnv() {
        try {
            _user.value = auth.currentUser

            // Alternative Schreibweise um auf null Werte zu überprüfen
            auth.currentUser?.let { firebaseUser ->
                profileRef = firestore.collection("user").document(firebaseUser.uid)
                notesRef = firestore.collection("user").document(firebaseUser.uid).collection("notes")
            }
            Log.d("setupUserEnv", "Benutzerumgebungseinrichtung erfolgreich")
        } catch (e: Exception) {
            Log.e("setupUserEnv", "Fehler beim Einrichten der Benutzerumgebung", e)
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            // Handle mismatch of passwords
            Log.e("register", "Passwörter stimmen nicht überein")
            return
        }

        try {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    // User wurde erstellt
                    setupUserEnv()

                    val newProfile = Profile()
                    profileRef.set(newProfile)
                    Log.d("register", "Benutzer erfolgreich registriert")
                } else {
                    // Fehler aufgetreten
                    Log.e("register", "Fehler beim Registrieren des Benutzers", it.exception)
                }
            }
        } catch (e: Exception) {
            Log.e("register", "Fehler in der Registrationsmethode", e)
        }
    }


    fun login(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    // User wurde eingeloggt
                    setupUserEnv()
                    Log.d("login", "Benutzer erfolgreich eingeloggt")
                } else {
                    // Fehler aufgetreten
                    Log.e("login", "Fehler beim einloggen des Benutzers")
                }
            }
        } catch (e: Exception) {
            Log.e("login", "Fehler in der LogIn-Methode", e)
        }
    }

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


