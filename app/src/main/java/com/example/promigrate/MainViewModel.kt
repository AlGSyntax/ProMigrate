package com.example.promigrate

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.promigrate.data.model.UserProfile
import com.example.promigrate.data.repository.Repository
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


const val TAG = "MainViewModel"

class MainViewModel : ViewModel() {

    lateinit var repository:Repository
    private val _locale = MutableLiveData<Locale>()
    val locale: LiveData<Locale> = _locale

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
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Ändern der Sprache", e)
            }
        }
    }

    private fun setLocale(languageCode: String) {
        try {
            val newLocale = Locale(languageCode)
            Locale.setDefault(newLocale)
            _locale.value = newLocale
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Setzen der Sprache", e)
        }
    }
}


