package com.example.promigrate

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class MainViewModel : ViewModel() {


    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user
    //Das profile Document enthält ein einzelnes Profil(das des eingeloggten Users)
    //Document ist wie ein Objekt
    lateinit var profileRef: DocumentReference
    //Die note Collection enthält beliebig viele Notes(alle notes des eingeloggten Users
    //Collection ist wie eine Liste
    lateinit var notesRef: CollectionReference
    lateinit var repository:Repository
    private val _locale = MutableLiveData<Locale>()
    val locale: LiveData<Locale> = _locale

    init {

        setupUserEnv()
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


    fun loadLanguageSetting() {
        viewModelScope.launch {
            try {
                val languageSetting = withContext(Dispatchers.IO) {
                    repository.loadLanguageSetting()
                }
                setLocale(languageSetting)
                Log.d(TAG, "Spracheinstellung erfolgreich geladen")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Laden der Spracheinstellung", e)
            }
        }
    }

    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.saveLanguageSetting(languageCode)
                }
                setLocale(languageCode)
                Log.d(TAG, "Sprache erfolgreich geändert zu $languageCode")
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Ändern der Sprache", e)
            }
        }
    }

    fun setupUserEnv() {
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

    fun register(email: String, password: String) {
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
                    Log.e("register", "Fehler beim registrieren des Benutzers,")
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


    private fun setLocale(languageCode: String) {
        try {
            val newLocale = Locale(languageCode)
            Locale.setDefault(newLocale)
            _locale.value = newLocale
            Log.d(TAG, "Sprache erfolgreich gesetzt zu $languageCode")
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Setzen der Sprache", e)
        }
    }
}


