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
import com.example.promigrate.databinding.FragmentRegisterBinding


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

        viewModel.registrationStatus.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                // Navigiere zum CreateYourProfileFragment
                findNavController().navigate(R.id.createYourProfileFragment)
            } else {
                // Zeige eine Fehlermeldung an
                Toast.makeText(context, "Registrierung fehlgeschlagen", Toast.LENGTH_SHORT).show()
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
        // Hier kannst du die Validierung implementieren (z.B. überprüfen, ob die Felder nicht leer sind und ob die Passwörter übereinstimmen)
        return true
    }


    private fun updateUI(localeList: LocaleList) {
        val configuration = Configuration()
        configuration.setLocales(localeList)
        val context = requireContext().createConfigurationContext(configuration)
        val resources = context.resources

        binding.registerBTN.text = resources?.getString(R.string.register)

        // Weitere UI-Elemente aktualisieren
    }
}
