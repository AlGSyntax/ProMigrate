package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.promigrate.MainViewModel
import com.example.promigrate.R



class ReOnboardingFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var arbeitsortEditText: AutoCompleteTextView
    private lateinit var berufEditText: AutoCompleteTextView
    private lateinit var submitButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_re_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arbeitsortEditText = view.findViewById(R.id.arbeitsortEditText)
        berufEditText = view.findViewById(R.id.berufEditText)
        submitButton = view.findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            val arbeitsort = arbeitsortEditText.text.toString()
            val beruf = berufEditText.text.toString()

            if (arbeitsort.isNotBlank() && beruf.isNotBlank()) {
                // Hier kannst du die Daten an das ViewModel weitergeben oder direkt handeln
                // Beispiel für das direkte Weitergeben der Eingaben an das JobOffersFragment
                (parentFragment as? ViewPagerFragment)?.let {
                    it.viewModel.updateJobOffers(beruf, arbeitsort)
                    it.moveToNextPage() // Ensure this method exists in your ViewPagerFragment
                }
            } else {
                Toast.makeText(context, "Bitte füllen Sie alle Felder aus", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
