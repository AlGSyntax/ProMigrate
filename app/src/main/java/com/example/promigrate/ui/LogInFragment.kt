package com.example.promigrate.ui


import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentLogInBinding


class LogInFragment : Fragment() {

    private  val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentLogInBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
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
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"

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
}
