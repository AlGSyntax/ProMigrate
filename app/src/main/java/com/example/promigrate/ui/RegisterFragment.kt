package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar

/**
 * RegisterFragment ist verantwortlich für die Darstellung und Handhabung des
 * Registrierungsprozesses des Benutzers.
 */
class RegisterFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentRegisterBinding? = null


    /**
     * Initialisiert die Benutzeroberfläche des Fragments, indem das entsprechende Layout aufgeblasen wird.
     *
     * @param inflater: Das LayoutInflater-Objekt, das zum Aufblasen des Layouts des Fragments verwendet wird.
     * @param container: Die übergeordnete ViewGroup, in den die neue Ansicht eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte View für das Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
        Bundle?
    ): View {
        // Initialisiert das Binding für das Fragment, um auf die UI-Elemente zugreifen zu können.
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        // Gibt die Wurzelansicht des Fragments zurück.
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

        /**
         * Beobachtet den Registrierungsstatus. Wenn der Registrierungsprozess erfolgreich ist, navigiert
         * der Benutzer zum Profilerstellungsfragment. Im Fehlerfall wird ein Snackbar mit der entsprechenden
         * Fehlermeldung angezeigt.
         */
        viewModel.registrationStatus.observe(viewLifecycleOwner) { registrationStatus ->
            registrationStatus?.let { status ->
                if (status.success) {
                    // Navigiert zum CreateYourProfileFragment, wenn die Registrierung erfolgreich war.
                    findNavController().navigate(R.id.createYourProfileFragment)
                } else {
                    // Erstellt und zeigt einen Snackbar bei einem Registrierungsfehler.
                    val message = getString(status.message as Int)
                    val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
                    val snackbarLayout = snackbar.view
                    snackbarLayout.setBackgroundResource(R.drawable.snackbar_custom_background)
                    // Stilisiert die Snackbar, um ihn an das Design der App anzupassen.
                    snackbar.setActionTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorError
                        )
                    )
                    snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.titles))
                    snackbar.setBackgroundTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.elevation
                        )
                    )
                    // Fügt eine Aktion zum Schließen des Snackbar hinzu.
                    snackbar.setAction(R.string.close) {
                        snackbar.dismiss()

                    }
                    snackbar.show()
                }
            }
        }


        /**
         * Beobachtet den vom Benutzer ausgewählten Sprachcode und setzt das Verhalten des Registrierungsbuttons fest.
         * Wenn der Button gedrückt wird, werden die eingegebenen Daten überprüft und, falls gültig, wird die Registrierungsmethode aufgerufen.
         */
        viewModel.selectedLanguageCode.observe(viewLifecycleOwner) { selectedLanguageCode ->
            // Setzt den OnClickListener für den Registrierungsbutton.
            binding!!.registerBTN.setOnClickListener {
                // Extrahiert die Eingaben vom Benutzer aus den entsprechenden EditText-Feldern.
                val email = binding!!.emailEditText.text.toString()
                val password = binding!!.passwordEditText.text.toString()
                val confirmPassword = binding!!.confirmPasswordEditText.text.toString()

                // Prüft, ob die Eingaben gültig sind. Falls ja, wird der Registrierungsvorgang mit
                // dem ausgewählten Sprachcode initiiert
                if (isValidInput(email, password, confirmPassword)) {
                    viewModel.register(email, password, confirmPassword, selectedLanguageCode)
                }
            }
        }
    }


    /**
     * Überprüft, ob die eingegebenen Registrierungsinformationen gültig sind.
     *
     * Diese Methode validiert die E-Mail-Adresse und Passwörter, die vom Benutzer eingegeben wurden.
     * Sie überprüft, ob die E-Mail-Adresse leer ist oder dem E-Mail-Format entspricht, ob das Passwort die
     * Mindestlänge erfüllt und ob die beiden eingegebenen Passwörter übereinstimmen. Fehlermeldungen werden
     * entsprechend in der Benutzeroberfläche angezeigt.
     *
     * @param email: Die eingegebene E-Mail-Adresse.
     * @param password: Das eingegebene Passwort.
     * @param confirmPassword: Das zur Bestätigung eingegebene Passwort.
     * @return True, wenn alle Eingaben gültig sind, andernfalls False.
     */
    private fun isValidInput(email: String, password: String, confirmPassword: String): Boolean {
        // Zurücksetzen der Fehlermeldungen.
        binding!!.emailTextInputLayout.error = null
        binding!!.passwordTextInputLayout.error = null
        binding!!.confirmPasswordTextInputLayout.error = null

        return when {
            // Validierung der E-Mail-Adresse.
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding!!.emailTextInputLayout.error = getString(R.string.invalid_email_error)
                false
            }
            // Validierung des Passworts auf Mindestlänge.
            password.isEmpty() || password.length < 8 -> {
                binding!!.passwordTextInputLayout.error = getString(R.string.invalid_password_error)
                false
            }
            // Überprüfung auf Passwortübereinstimmung.
            confirmPassword != password -> {
                binding!!.confirmPasswordTextInputLayout.error =
                    getString(R.string.password_mismatch_error)
                false
            }
            // Wenn alle Validierungen erfolgreich sind, ist 'isValidInput()' true.
            else -> true
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
