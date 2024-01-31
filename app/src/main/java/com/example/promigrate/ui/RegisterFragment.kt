package com.example.promigrate.ui

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel.localeList.observe(viewLifecycleOwner) { localeListCompat ->
            updateUI(localeListCompat.unwrap() as LocaleList)
        }

        binding.registerBTN.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            if (isValidInput(email, password, confirmPassword)) {
                viewModel.register(email, password, confirmPassword)
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
