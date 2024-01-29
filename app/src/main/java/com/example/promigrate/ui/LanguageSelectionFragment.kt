package com.example.promigrate.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.data.repository.Repository
import com.example.promigrate.databinding.FragmentLanguageSelectionBinding
import java.util.Locale

class LanguageSelectionFragment : Fragment() {
    private lateinit var welcomeTextView: TextView
    private lateinit var languageSpinner: Spinner
    private lateinit var viewModel: MainViewModel

    private lateinit var confirmButton: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLanguageSelectionBinding.inflate(inflater,
            container, false)

        welcomeTextView = binding.tvWelcomeMessage
        languageSpinner = binding.spinnerLanguageSelection
        confirmButton = binding.btnConfirmLanguage

        val repository = Repository.getInstance(requireContext())
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.repository = repository
        viewModel.loadLanguageSetting()

        viewModel.locale.observe(viewLifecycleOwner) { locale ->
            updateTextViews(locale)
        }
        setupLanguageSpinner()
        setupConfirmButton()

        return binding.root
    }





    private fun setupLanguageSpinner() {
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                val languageCode = when (position) {
                    0 -> "en"
                    1 -> "de"
                    2 -> "ja"
                    3 -> "zh"
                    4 -> "es"
                    5 -> "uk"
                    6 -> "ru"
                    else -> "en"
                }
                viewModel.changeLanguage(languageCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }



    private fun updateTextViews(locale: Locale) {
        // Aktualisiere die Willkommensnachricht, nachdem die Spracheinstellung geändert wurde
        // Anstatt hier direkt die Locale zu setzen, benutze die vom ViewModel beobachtete Locale
        val config = Configuration()
        config.setLocale(locale)
        val context = context?.createConfigurationContext(config)
        val resources = context?.resources
        welcomeTextView.text = resources?.getString(R.string.languageselectionmessage)
        // Aktualisiere hier weitere Textansichten, falls erforderlich
    }

    private fun setupConfirmButton() {
        confirmButton.setOnClickListener {
            navigateToLoginFragment()
        }
    }

    private fun navigateToLoginFragment() {
        val action = LanguageSelectionFragmentDirections.
            actionLanguageSelectionFragmentToLoginFragment()
        findNavController().navigate(action)
    }

}