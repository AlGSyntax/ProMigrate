package com.example.promigrate

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedLanguage()

        setContentView(R.layout.activity_main)
    }

    private fun applySavedLanguage() {
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("SelectedLanguage", Locale.getDefault().language)
        setLocale(languageCode)
    }

    private fun setLocale(languageCode: String?) {
        languageCode?.let {
            val locale = Locale(it)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}