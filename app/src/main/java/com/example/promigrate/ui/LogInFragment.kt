package com.example.promigrate.ui


import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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


class LogInFragment : Fragment() {

    private  val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentLogInBinding
    private lateinit var googleSignInClient: GoogleSignInClient





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        configureGoogleSignIn()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.localeList.observe(viewLifecycleOwner) { localeListCompat ->
            updateUI(localeListCompat.unwrap() as LocaleList)
        }

        binding.loginBTN.setOnClickListener {
            // Prüfe die Eingaben vor dem Login
            if (isValidInput()) {
                viewModel.login(binding.emailET.text.toString(), binding.passwordET.text.toString())
            }
        }

        binding.loginButton.setOnClickListener {
            signIn()
        }


        binding.toregister.setOnClickListener {
            // Hier führst du die Navigation zum RegisterFragment durch
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // User-Navigation

        viewModel.loginStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_loginFragment_to_createYourProfileFragment)
            } else {
                Toast.makeText(context, "Login fehlgeschlagen. Bitte überprüfe deine Eingaben.", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun isValidInput(): Boolean {
        var isValid = true

        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null

        // Überprüfung, ob die E-Mail-Adresse gültig ist
        if (binding.emailET.text.toString().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.emailET.text.toString()).matches()) {
            binding.emailInputLayout.error = getString(R.string.invalid_email_error)
            isValid = false
        }

        // Passwortkriterien
        val password = binding.passwordET.text.toString()
        val passwordPattern = getString(R.string.passwordpattern)

        // Überprüfung der Passwortkriterien
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = getString(R.string.invalid_password_error_login) // Nutze eine angepasste Fehlermeldung
            isValid = false
        } else if (!password.matches(passwordPattern.toRegex())) {
            binding.passwordInputLayout.error = getString(R.string.password_criteria_error)
            isValid = false
        }

        return isValid
    }



    private fun updateUI(localeList: LocaleList) {
        val configuration = Configuration()
        configuration.setLocales(localeList)
        val context = requireContext().createConfigurationContext(configuration)
        val resources = context.resources

        binding.loginBTN.text = resources?.getString(R.string.login)
        binding.toregister.text =resources?.getString(R.string.registrationinvitation)
        // Weitere UI-Elemente aktualisieren
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }


    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            viewModel.onGoogleLoginClicked(account.idToken!!)
        } catch (e: ApiException) {
            // Der Google Sign-In ist fehlgeschlagen, handle den Fehler
            Toast.makeText(context, "Google Sign-In fehlgeschlagen: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }
}
