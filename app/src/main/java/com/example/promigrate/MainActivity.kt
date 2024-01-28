package com.example.promigrate

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val sharedPrefs = newBase?.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs?.getString("SelectedLanguage", Locale.getDefault().language)
        val locale = languageCode?.let { Locale(it) }
        if (locale != null) {
            Locale.setDefault(locale)
        }

        val config = newBase?.resources?.configuration
        config?.setLocale(locale)

        val context = newBase?.createConfigurationContext(config!!)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
