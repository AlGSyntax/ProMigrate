package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

            viewModel.arbeitsorte.observe(viewLifecycleOwner) { arbeitsorte ->
                // Stelle sicher, dass die Liste nicht null ist
                if (arbeitsorte != null) {
                    // Erstelle einen ArrayAdapter mit den Arbeitsorten
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        arbeitsorte
                    )
                    // Setze den Adapter für arbeitsortEditText
                    arbeitsortEditText.setAdapter(adapter)
                } else {
                    Toast.makeText(context, "Fehler beim Laden der Arbeitsorte", Toast.LENGTH_SHORT).show()
                }
            }

            arbeitsortEditText.setOnItemClickListener { adapterView, _, position, _ ->
                val arbeitsort = adapterView.getItemAtPosition(position) as String
                // Hier rufst du das Berufsfeld aus dem Benutzerprofil ab
                val berufsfeld = viewModel.userProfileData.value?.fieldOfWork ?: ""

                if (berufsfeld.isNotBlank()) {
                    viewModel.fetchJobs(berufsfeld, arbeitsort)
                } else {
                    Toast.makeText(context, "Berufsfeld im Profil nicht gesetzt", Toast.LENGTH_SHORT).show()
                }
            }


            viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
                if (jobs.isNotEmpty()) {
                    // Erstelle einen ArrayAdapter mit den Jobangeboten
                    val jobsAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        jobs // Stelle sicher, dass 'jobs' eine Liste von Strings ist
                    )
                    // Setze den Adapter für jobsAutoCompleteTextView
                    berufEditText.setAdapter(jobsAdapter)
                } else {
                    Toast.makeText(context, "Keine Jobangebote gefunden", Toast.LENGTH_SHORT).show()
                }
            }


            submitButton.setOnClickListener {
                val arbeitsort = arbeitsortEditText.text.toString()
                val beruf = berufEditText.text.toString()

                if (arbeitsort.isNotBlank() && beruf.isNotBlank()) {
                    (parentFragment as? ViewPagerFragment)?.let {
                        it.viewModel.updateJobOffers(beruf, arbeitsort)
                        it.moveToNextPage()
                    }
                } else {
                    Toast.makeText(context, "Bitte füllen Sie alle Felder aus", Toast.LENGTH_SHORT).show()
                }
            }
        }
}
