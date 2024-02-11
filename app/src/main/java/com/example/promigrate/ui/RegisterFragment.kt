package com.example.promigrate.ui

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
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


class RegisterFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.registrationStatus.observe(viewLifecycleOwner) { registrationStatus ->
            registrationStatus?.let { status ->
                if (status.success) {
                    // Navigiere zum CreateYourProfileFragment
                    findNavController().navigate(R.id.createYourProfileFragment)
                } else {
                    // Holt den String basierend auf der Ressourcen-ID
                    val message = getString(status.message as Int) // Konvertiert die message in eine Ressourcen-ID und holt den String
                    val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
                    val snackbarLayout = snackbar.view
                    snackbarLayout.setBackgroundResource(R.drawable.snackbar_custom_background)
                    // Anpassen der Snackbar (optional)
                    snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
                    snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.titles))

                    snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.elevation))

                    snackbar.setAction(R.string.close) {
                        // Die Aktion dient dazu, die Snackbar zu schließen.
                    }
                    snackbar.show()
                }
            }
        }




        viewModel.localeList.observe(viewLifecycleOwner) { localeListCompat ->
            updateUI(localeListCompat.unwrap() as LocaleList)
        }

        viewModel.selectedLanguageCode.observe(viewLifecycleOwner) { selectedLanguageCode ->
            binding.registerBTN.setOnClickListener {
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                val confirmPassword = binding.confirmPasswordEditText.text.toString()

                // Überprüfung der Eingaben und Registrierung mit dem ausgewählten Sprachcode
                if (isValidInput(email, password, confirmPassword)) {
                    viewModel.register(email, password, confirmPassword, selectedLanguageCode)
                }
            }
        }
    }


    private fun isValidInput(email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Reset errors
        binding.emailTextInputLayout.error = null
        binding.passwordTextInputLayout.error = null
        binding.confirmPasswordTextInputLayout.error = null

        // E-Mail Validierung
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTextInputLayout.error = getString(R.string.invalid_email_error)
            isValid = false
        }

        // Passwort Validierung
        if (password.isEmpty() || password.length < 8) {
            binding.passwordTextInputLayout.error = getString(R.string.invalid_password_error)
            isValid = false
        }

        // Überprüfung, ob die Passwörter übereinstimmen
        if (confirmPassword != password) {
            binding.confirmPasswordTextInputLayout.error = getString(R.string.password_mismatch_error)
            isValid = false
        }

        return isValid
    }




    private fun updateUI(localeList: LocaleList) {
        val configuration = Configuration()
        configuration.setLocales(localeList)
        val context = requireContext().createConfigurationContext(configuration)
        val resources = context.resources

        binding.registerBTN.text = resources?.getString(R.string.register)
        binding.headerTextView.text = resources?.getString(R.string.signup)
    }
}
