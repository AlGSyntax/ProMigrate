package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.adapter.JobOpportunitiesAdapter
import com.example.promigrate.databinding.FragmentJobOportunitiesBinding


class JobOpportunitiesFragment : Fragment() {

    private lateinit var binding: FragmentJobOportunitiesBinding
    private val viewModel: MainViewModel by activityViewModels()

    private val jobsAdapter = JobOpportunitiesAdapter { jobTitle, isChecked ->
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
        binding = FragmentJobOportunitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = jobsAdapter

        // Annahme: Du hast den Arbeitsort und die ausgewählten Berufe als Argumente erhalten
        val args = JobOpportunitiesFragmentArgs.fromBundle(requireArguments())
        val selectedJobs = args.selectedJobs.toList() // oder wie auch immer du die Daten übergeben hast
        val arbeitsort = args.arbeitsort // Stelle sicher, dass du diesen Parameter übergeben und im NavGraph definiert hast

        // Hier musst du eine Logik implementieren, um für jeden ausgewählten Beruf die Jobangebote abzurufen
        // Dies könnte eine Schleife sein, die `viewModel.fetchJobOffers` für jeden Beruf aufruft, oder eine Anpassung deiner Backend-Logik,
        // um mehrere Berufe auf einmal zu berücksichtigen
        selectedJobs.forEach { selectedJob ->
            viewModel.fetchJobOffers(selectedJob, arbeitsort)
        }

        viewModel.jobOffers.observe(viewLifecycleOwner) { jobOffers ->
            if (jobOffers != null) {
                Log.d(TAG, "Jobangebote erfolgreich abgerufen.")
                jobsAdapter.submitList(jobOffers.toList())
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobangebote.")
            }
        }
    }
}