package com.example.promigrate.ui


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

/**
 * LogInFragment ist verantwortlich für die Darstellung und Handhabung des Login-Bereichs.
 * In diesem Fragment kann sich der Benutzer mit E-Mail und Passwort oder über Google anmelden.
 */
class LogInFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt für das LogInFragment, ermöglicht den einfachen Zugriff auf
    // die UI-Komponenten.
    private lateinit var binding: FragmentLogInBinding

    // Der GoogleSignInClient wird für die Authentifizierung mit Google benötigt.
    private lateinit var googleSignInClient: GoogleSignInClient


    /**
     * Initialisiert die Benutzeroberfläche des Fragments, indem das entsprechende Layout aufgeblasen wird und
     * die Google-Anmeldekonfiguration vorgenommen wird.
     *
     * @param inflater: Das LayoutInflater-Objekt, das zum Aufblasen des Layouts des Fragments verwendet wird.
     * @param container: Die übergeordnete ViewGroup, in den die neue Ansicht eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte View für das Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialisiert das Binding für das Fragment, um auf die UI-Elemente zugreifen zu können.
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        // Konfiguriert die Google-Anmeldung, indem die Google-Anmeldedaten festgelegt werden.
        configureGoogleSignIn()
        // Gibt die Wurzelansicht des Fragments zurück.
        return binding.root
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
         * Initialisiert den Click-Listener für den Login-Button.
         * Beim Klick wird zunächst über die ViewModel-Methode geprüft,
         * ob die E-Mail existiert. Wenn die Eingaben gemäß der Methode 'isValidInput()'
         * gültig sind, wird ein Login-Versuch über das ViewModel gestartet.
         */
        binding.loginBTN.setOnClickListener {
            // Überprüfung, ob die eingegebene E-Mail existiert
            viewModel.doesEmailExist(binding.emailET.text.toString())
            // Überprüfung der Eingabefelder auf Gültigkeit
            if (isValidInput()) {
                // Versuch, sich mit den eingegebenen Anmeldedaten einzuloggen
                viewModel.login(binding.emailET.text.toString(), binding.passwordET.text.toString())
            }
        }

        /**
         * Initialisiert den Click-Listener für den Google-Login-Button.
         */
        binding.googleloginButton.setOnClickListener {
            signIn()
        }

        /**
         * Ein OnClickListener für den TextView, der es dem Benutzer ermöglicht, sein Passwort zurückzusetzen.
         * Der Benutzer gibt seine E-Mail-Adresse ein, und wenn sie gültig ist, wird ein Link zum Zurücksetzen des Passworts gesendet.
         */
        binding.resetPasswordTV.setOnClickListener {
            val email = binding.emailET.text.toString().trim()

            // Funktion zum Anzeigen eines Snackbars mit einer Nachricht.
            fun showSnackbar(message: Int) {
                val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
                snackbar.view.setBackgroundResource(R.drawable.snackbar_custom_background)
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
                snackbar.setAction(R.string.close) {
                    snackbar.dismiss()
                }
                snackbar.show()
            }

            // Überprüft, ob die E-Mail-Adresse leer ist. Wenn nicht, wird ein Link zum Zurücksetzen des Passworts gesendet.
            if (email.isEmpty()) {
                showSnackbar(R.string.pleaseputemailin)
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        val message =
                            if (task.isSuccessful) R.string.resetpasswordsuccess else R.string.resetpasswordfail
                        showSnackbar(message)
                    }
            }
        }


        /**
         * Reagiert auf Klickereignisse des 'toRegister'-Buttons.
         * Leitet den Benutzer zum 'Registerfragment' weiter, wo ein neues Benutzerkonto erstellt werden kann.
         */
        binding.toregister.setOnClickListener {
            // Navigiert zum RegisterFragment, damit der Benutzer ein neues Konto erstellen kann.
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }


        /**
         * Beobachtet den Anmeldestatus, der vom ViewModel verwaltet wird.
         * Abhängig vom Anmeldeergebnis und ob der Onboarding-Prozess bereits abgeschlossen wurde,
         * leitet diese Methode den Benutzer entweder zum DashboardFragment oder zum CreateYourProfileFragment weiter.
         */
        viewModel.loginStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                // Überprüft, ob der Onboarding-Prozess abgeschlossen wurde.
                if (isOnboardingComplete()) {
                    // Wenn ja, navigiert es zum DashboardFragment.
                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                } else {
                    // Wenn nicht, navigiert zum CreateYourProfileFragment, um das Profil zu vervollständigen.
                    findNavController().navigate(R.id.action_loginFragment_to_createYourProfileFragment)
                }
            }
        }

        /**
         * Beobachtet die LiveData emailExists aus dem ViewModel, um auf Änderungen der Existenz einer E-Mail zu reagieren.
         * Wenn die E-Mail nicht existiert (exists == false), wird ein Fehler im Email-Input-Layout angezeigt,
         * um den Benutzer darauf hinzuweisen, dass die eingegebene E-Mail-Adresse nicht mit dem Passwort
         * übereinstimmt, um Bots zu verwirren.
         *
         * Diese Beobachtung ermöglicht es dem UI, dynamisch auf Änderungen des Zustands der E-Mail-Existenz zu reagieren,
         * und bietet dem Benutzer direktes Feedback zur Gültigkeit seiner Eingaben.
         */
        viewModel.emailExists.observe(viewLifecycleOwner) { exists ->
            if (!exists) {
                binding.emailInputLayout.error = getString(R.string.emailnotexistserror)
            }
        }

        /**
         * Beobachtet die LiveData userProfileData aus dem ViewModel, um auf Änderungen der Benutzerprofildaten zu reagieren.
         * Wenn userProfileData aktualisiert wird, navigiert diese Methode basierend auf den neuen Daten zum DashboardFragment.
         *
         * Diese Implementierung extrahiert spezifische Daten aus dem Benutzerprofil (wie ausgewählte Jobs und Arbeitsort),
         * packt diese in ein Navigations-Action-Bundle und leitet den Benutzer entsprechend weiter.
         * Das ermöglicht eine nahtlose Benutzererfahrung, bei der die UI auf Änderungen im Benutzerprofil dynamisch reagiert.
         */
        viewModel.userProfileData.observe(viewLifecycleOwner) { userProfile ->
            if (userProfile != null) {
                // Extrahiere Jobtitel ,Referenznummern und dem gewünschten Arbeitsort aus dem Benutzerprofil.
                val jobTitlesArray =
                    userProfile.selectedJobs?.keys?.toList()?.toTypedArray() ?: arrayOf()
                val refNrsArray =
                    userProfile.selectedJobs?.values?.toList()?.toTypedArray() ?: arrayOf()
                val arbeitsort = userProfile.desiredLocation ?: ""

                // Erstellt eine Navigationsaktion mit den extrahierten Daten und leitet den Benutzer zum DashboardFragment weiter.
                val action = LogInFragmentDirections.actionLoginFragmentToDashboardFragment(
                    selectedJobTitles = jobTitlesArray,
                    selectedJobRefNrs = refNrsArray,
                    arbeitsort = arbeitsort
                )
                findNavController().navigate(action)
            }
        }

        /**
         * Fügt einen OnClickListener zum Button "exitapp" hinzu.
         * Wenn der Benutzer auf diesen Button klickt, wird die aktuelle Aktivität beendet,
         * was zur Folge hat, dass die App geschlossen wird.
         */
        binding.exitapp.setOnClickListener {
            activity?.finish()
        }
    }

    /**
     * Überprüft, ob die eingegebenen Benutzerdaten gültig sind.
     * Die Methode prüft, ob die E-Mail-Adresse und das Passwort korrekt eingegeben wurden.
     *
     * @return true, wenn sowohl die E-Mail-Adresse als auch das Passwort gültige Eingaben sind, sonst false.
     */
    private fun isValidInput(): Boolean {

        // E-Mail aus dem entsprechenden Eingabefeld extrahieren und prüfen ob sie gültig ist.
        val email = binding.emailET.text.toString()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Setzt eine Fehlermeldung, wenn die E-Mail ungültig ist.
            binding.emailInputLayout.error = getString(R.string.invalid_email_error)
            return false
        }

        // Passwort aus dem entsprechenden Eingabefeld extrahieren und prüfen ob es gültig ist.
        val password = binding.passwordET.text.toString()
        if (password.isEmpty()) {
            // Setzt eine Fehlermeldung, wenn das Passwort ungültig ist.
            binding.passwordInputLayout.error = getString(R.string.invalid_password_error_login)
            return false
        }

        // Entfernt die Fehlermeldungen, wenn beide Eingaben gültig sind.
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null

        // Gültige Eingaben führen dazu, dass true zurückgegeben wird
        return true
    }


    /**
     * ActivityResultLauncher, um das Ergebnis des Google-SignIn-Intents zu verarbeiten.
     */
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    /**
     * Konfiguriert GoogleSignInOptionen und initialisiert den GoogleSignInClient.
     * Die Konfiguration umfasst die Anforderung einer Benutzer-ID und E-Mail-Adresse.
     */
    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }


    /**
     * Startet den Google SignIn Intent, damit der Benutzer sein Google-Konto auswählen kann.
     */
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

  /**
     * Verarbeitet das Ergebnis des Google-Anmeldevorgangs.
     * Wenn die Anmeldung erfolgreich war, wird geprüft, ob es sich um die erste Anmeldung des Benutzers handelt.
     * Wenn dies der Fall ist, wird der Benutzer registriert und zum CreateYourProfileFragment navigiert.
     * Wenn es sich nicht um die erste Anmeldung des Benutzers handelt, navigiert er zum DashboardFragment.
     * Wenn die Anmeldung nicht erfolgreich war, wird ein Toast mit einer Fehlermeldung angezeigt.
     *
     * @param completedTask: Die Aufgabe, die das Ergebnis des Google-Anmeldevorgangs enthält.
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // Den GoogleSignInAccount aus der abgeschlossenen Aufgabe abrufen.
            val account = completedTask.getResult(ApiException::class.java)
           // Übergibt das ID-Token an das ViewModel.
            viewModel.onGoogleLoginClicked(account.idToken!!)

           // Überprüft, ob es sich um die erste Anmeldung des Benutzers handelt.
            val sharedPref = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val isFirstLogin = sharedPref?.getBoolean(account.id, true) ?: true

            if (isFirstLogin) {
                // Wenn es sich um die erste Anmeldung des Benutzers handelt, registrieren Sie den Benutzer und navigieren Sie zum CreateYourProfileFragment.
                val languageCode = viewModel.selectedLanguageCode.value ?: "de" // Use a default value if no value is set.
                viewModel.registerGoogleUser(account.idToken!!, languageCode)
                findNavController().navigate(R.id.action_loginFragment_to_createYourProfileFragment)
                sharedPref?.edit()?.putBoolean(account.id, false)?.apply()
            } else {
                // Wenn es sich nicht um die erste Anmeldung des Benutzers handelt, navigiert es zum DashboardFragment.
                findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
            }
        } catch (e: ApiException) {
            // Wenn die Anmeldung nicht erfolgreich war, zeigt es einen Toast mit einer Fehlermeldung.
            Toast.makeText(
                context,
                getString(R.string.googlesignupfailed, e.statusCode),
                Toast.LENGTH_LONG
            ).show()
        }
    }


    /**
     * Überprüft, ob der Onboarding-Prozess abgeschlossen wurde.
     * Nutzt SharedPreferences, um den Zustand des Onboardings zu speichern und zu überprüfen.
     *
     * @return: True, wenn der Onboarding-Prozess abgeschlossen wurde, sonst false.
     */
    private fun isOnboardingComplete(): Boolean {
        // Zugriff auf die SharedPreferences mit dem Kontext der zugehörigen Aktivität.
        val sharedPref = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        // Abfrage des gespeicherten Wahrheitswerts unter dem Schlüssel "OnboardingComplete".
        // Falls dieser Wert nicht gefunden wird, wird als Standardwert 'false' zurückgegeben.
        return sharedPref?.getBoolean("OnboardingComplete", false) ?: false
    }


}
