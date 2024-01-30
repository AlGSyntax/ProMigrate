package com.example.promigrate.ui


import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentLogInBinding


class LoginFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentLogInBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel.localeList.observe(viewLifecycleOwner) { localeListCompat ->
            updateUI(localeListCompat.unwrap() as LocaleList)
        }

        binding.loginBTN.setOnClickListener {
            // Prüfe die Eingaben vor dem Login
            if (isValidInput()) {
                viewModel.login(binding.emailET.text.toString(), binding.passwordET.text.toString())
            }
        }

        binding.registerBTN.setOnClickListener {
            // Prüfe die Eingaben vor der Registrierung
            if (isValidInput()) {
                viewModel.register(binding.emailET.text.toString(), binding.passwordET.text.toString())
            }
        }

        // User-Navigation
        viewModel.user.observe(viewLifecycleOwner) {
            it?.let {
                // Navigiere zum nächsten Screen
            }
        }
    }

    private fun isValidInput(): Boolean {
        // Implementiere die Überprüfung der Benutzereingaben
        return true
    }

    private fun updateUI(localeList: LocaleList) {
        val configuration = Configuration()
        configuration.setLocales(localeList)
        val context = requireContext().createConfigurationContext(configuration)
        val resources = context.resources

        binding.loginBTN.text = resources.getString(R.string.login)
        binding.registerBTN.text = resources.getString(R.string.register)
        // Weitere UI-Elemente aktualisieren
    }
}
