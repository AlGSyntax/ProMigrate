package com.example.promigrate.ui


import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        viewModel.user.observe(viewLifecycleOwner) {
            it?.let {
                findNavController().navigate(R.id.action_loginFragment_to_createYourProfileFragment)
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

        binding.loginBTN.text = resources?.getString(R.string.login)
        binding.toregister.text =resources?.getString(R.string.registrationinvitation)
        // Weitere UI-Elemente aktualisieren
    }
}
