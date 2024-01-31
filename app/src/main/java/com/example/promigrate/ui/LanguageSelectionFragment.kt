package com.example.promigrate.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentLanguageSelectionBinding

class LanguageSelectionFragment : Fragment() {

    private val  viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentLanguageSelectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLanguageSelectionBinding.inflate(inflater, container, false)


        setupLanguageSpinner()
        setupConfirmButton()
        observeViewModel()

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.localeList.observe(viewLifecycleOwner) { localeListCompat ->
            val configuration = Configuration(resources.configuration)
            configuration.setLocales(localeListCompat.unwrap() as LocaleList?)
            val context = requireContext().createConfigurationContext(configuration)
            updateUI(context)
        }
    }

    private fun setupLanguageSpinner() {
        binding.spinnerLanguageSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val languageCode = getLanguageCode(position)
                viewModel.setSelectedLanguageCode(languageCode)
                viewModel.changeLanguage(languageCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun getLanguageCode(position: Int): String {
        return when (position) {
            0 -> "en"
            1 -> "de"
            2 -> "es"
            3 -> "ja"
            4 -> "ru"
            5 -> "uk"
            else -> "en"
        }
    }// Nachdenken , wie Ich diese Logik in den SetupLanguageSpinner verschieben kann .

    private fun setupConfirmButton() {
        binding.btnConfirmLanguage.setOnClickListener {
            navigateToLoginFragment()
        }
    }

    private fun navigateToLoginFragment() {
        val action = LanguageSelectionFragmentDirections.actionLanguageSelectionFragmentToLoginFragment()
        findNavController().navigate(action)
    }// Sollte ich die Logik verschieben in die obere Funktion "set"

    private fun updateUI(context: Context) {
        binding.tvWelcomeMessage.text = context.resources.getString(R.string.languageselectionmessage)
        // Aktualisiere weitere UI-Elemente hier
    }
}
// Wann macht es sinn sachen in funktionen auszulagern ? Wann nicht ?