package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.adapter.JobsAdapter
import com.example.promigrate.databinding.FragmentJobsForYouBinding

class JobsForYouFragment : Fragment() {

    private lateinit var binding: FragmentJobsForYouBinding
    private val viewModel: MainViewModel by activityViewModels()

    // Erstelle eine Instanz deines Adapters
    private val jobsAdapter = JobsAdapter { jobTitle, isChecked ->
        if (isChecked) {
            viewModel.toggleJobSelection(jobTitle)
        } else {
            viewModel.toggleJobSelection(jobTitle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJobsForYouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = jobsAdapter
        binding.floatingActionButton.setOnClickListener {
            // Beobachte die ausgewählten Jobs und navigiere zum nächsten Fragment
            viewModel.selectedJobs.observe(viewLifecycleOwner) { selectedJobs ->
                // Konvertiere das Set oder die Liste von Strings in ein Array, da Safe Args Arrays erfordern
                val selectedJobsArray = selectedJobs.toTypedArray()

                // Verwende Safe Args, um zur JobOpportunitiesFragment zu navigieren und die ausgewählten Jobs zu übergeben
                val action = JobsForYouFragmentDirections.actionJobsForYouFragmentToJobOportunitiesFragment(selectedJobsArray)
                findNavController().navigate(action)
            }
        }


        // Angenommene Parameter aus dem Bundle
        val berufsfeld = arguments?.getString("berufsfeld") ?: ""
        val arbeitsort = arguments?.getString("wo") ?: ""

        // Translate the arguments to German
        viewModel.translateToGerman(berufsfeld) { translatedBerufsfeld ->
            viewModel.translateToGerman(arbeitsort) { translatedArbeitsort ->
                // Verwende die übersetzten Werte direkt in den Parametern für fetchJobs
                // unter den spezifischen Namen berufsfeld und arbeitsort
                viewModel.fetchJobs(berufsfeld = translatedBerufsfeld, arbeitsort = translatedArbeitsort)
            }
        }

        // Beobachte die Jobliste im ViewModel und reiche die Daten an den Adapter weiter
        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            if (jobs != null) {
                Log.d(TAG, "Jobs erfolgreich abgerufen.")
                viewModel.translateJobTitles(jobs) { translatedJobs ->
                    // Aktualisiere den Adapter mit den übersetzten Jobtiteln
                    jobsAdapter.submitList(translatedJobs)
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobs.")
            }
        }
    }
}

