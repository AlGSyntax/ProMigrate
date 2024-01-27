package com.example.promigrate.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.promigrate.R
import java.util.Locale


class LanguageSelectionFragment : Fragment() {
    private lateinit var welcomeTextView: TextView
    private lateinit var languageSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_language_selection, container, false)
        welcomeTextView = view.findViewById(R.id.tv_welcome_message)
        languageSpinner = view.findViewById(R.id.spinner_language_selection)

        setupLanguageSpinner()

        return view
    }

    private fun setupLanguageSpinner() {
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Hier die Sprache ändern basierend auf der Auswahl
                val languageCode = when (position) {
                    0 -> "en" // Englisch
                    1 -> "de" // Deutsch
                    2 -> "ja" // Japanisch
                    3 -> "zh" // Chinesisch
                    4 -> "es" // Spanisch
                    5 -> "uk"//Ukrainisch
                    6 -> "ru"// Russisch
                    else -> "en"
                }
                changeLanguage(languageCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)

        context?.let { context ->
            context.createConfigurationContext(config)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }


    private fun saveLanguageSetting(languageCode: String) {
        val sharedPrefs = activity?.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPrefs?.edit()?.putString("SelectedLanguage", languageCode)?.apply()
    }

    private fun loadLanguageSetting(): String? {
        val sharedPrefs = activity?.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPrefs?.getString("SelectedLanguage", Locale.getDefault().language)
    }

    private fun updateTextViews() {
        // Aktualisiere die Willkommensnachricht, nachdem die Spracheinstellung geändert wurde
        welcomeTextView.text = getString(R.string.languageselectionmessage)
        // Aktualisiere hier weitere Textansichten, falls erforderlich
    }

    // Rufe diese Methode auf, wenn eine Sprache ausgewählt wurde
    fun changeLanguage(languageCode: String) {
        setLocale(languageCode)
        saveLanguageSetting(languageCode)
        updateTextViews()
    }
}