package com.example.promigrate.ui

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentLanguageSelectionBinding


/**
 * LanguageSelectionFragment ist ein Fragment, das dem Benutzer erlaubt, eine Sprache auszuwählen.
 * Nach der Auswahl wird die ausgewählte Sprache im ViewModel gespeichert und für die Anwendung übernommen.
 * Dieses Fragment verwendet Data Binding, um UI-Komponenten zu initialisieren und zu verwalten.
 */
class LanguageSelectionFragment : Fragment() {

    // ViewModel, das von allen Activity-Fragments geteilt wird. Es wird verwendet,
    // um Daten und Aktionen zentral zu verwalten.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentLanguageSelectionBinding? = null


    /**
     * Erstellt und gibt die Ansicht für das Fragment zurück, initialisiert die Datenbindung und
     * konfiguriert den ViewModel-Beobachter für die Spracheinstellungen.
     *
     * @param inflater: Der LayoutInflater-Objekt, das verwendet wird, um die XML-Layouts in entsprechende View-Objekte umzuwandeln.
     * @param container: Der übergeordnete Container, in dem das Fragment-Layout eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments speichert.
     * @return: Die erstellte View des Fragments.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Initialisiert das Binding für dieses Fragment.
        binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false)


        // Beobachtet Änderungen in der Spracheinstellung und aktualisiert die Konfiguration entsprechend.
        viewModel.localeList.observe(viewLifecycleOwner) { localeListCompat ->
            val configuration = Configuration(resources.configuration)
            configuration.setLocales(localeListCompat.unwrap() as LocaleList?)
        }
        // Gibt die Root-View des Fragments zurück.
        return binding!!.root
    }


    /**
     * Wird aufgerufen, nachdem die Ansicht des Fragments und seine hierarchische Struktur instanziiert wurden.
     * In dieser Methode werden weitere UI-Initialisierungen vorgenommen und Listener für UI-Elemente eingerichtet.
     *
     * @param view: Die erstellte Ansicht des Fragments.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Konfiguriert den Sprachauswahl-Spinner.
        // Diese Methode weist dem Spinner einen `OnItemSelectedListener` zu, der auf Benutzereingaben reagiert.
        // Bei Auswahl einer Sprache wird die Sprachcode-Logik ausgeführt und die App-Sprache entsprechend geändert.
        binding!!.spinnerLanguageSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Setzt die Textfarbe und -größe der ausgewählten Ansicht
                    if (view is TextView) {
                        view.setTextColor(resources.getColor(R.color.white, null))
                        view.textSize = 22f
                    }
                    // Ermittelt den Sprachcode basierend auf der ausgewählten Position im Spinner
                    val languageCode = when (position) {
                        0 -> "en"
                        1 -> "de"
                        2 -> "es"
                        else -> "en"
                    }
                    // Speichert die ausgewählte Sprache.
                    viewModel.setSelectedLanguageCode(languageCode)
                    // Ändert die App-Sprache entsprechend.
                    viewModel.changeLanguage(languageCode)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        // Setzt einen OnClickListener auf den btnConfirmLanguage Button.
        binding!!.btnConfirmLanguageSelection.setOnClickListener {
            // Erstellt eine Aktion für die Navigation basierend auf den NavDirections.
            val action =
                LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentToLoginFragment()
            // Verwendet den NavController, um zur Zieldestination gemäß der Aktion zu navigieren.
            findNavController().navigate(action)
        }
    }


    /**
     * Wird aufgerufen, wenn die View-Hierarchie des Fragments zerstört wird.
     * Hier wird das Binding-Objekt auf null gesetzt, um Memory Leaks zu vermeiden,
     * da das Binding-Objekt eine Referenz auf die View hält, welche nicht länger existiert.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}
