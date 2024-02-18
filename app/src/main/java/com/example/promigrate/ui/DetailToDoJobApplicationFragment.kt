package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.DetailToDoJobApplicationAdapter
import com.google.android.material.button.MaterialButton

const val TAG2 = "DetailToDoJobApplicationFragment"

class DetailToDoJobApplicationFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: DetailToDoJobApplicationFragmentArgs by navArgs()

    private val adapter = DetailToDoJobApplicationAdapter { jobTitle, isChecked ->
        // Logik zur Behandlung der Jobauswahl
        viewModel.toggleJobSelection(jobTitle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Layout des Fragments aufblasen
        return inflater.inflate(R.layout.fragment_detail_to_do_job_application, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView Setup
        val recyclerView: RecyclerView = view.findViewById(R.id.rvJobs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Übergeben Sie die ausgewählten Jobs direkt an den Adapter
        adapter.submitList(args.selectedJobs.toList())

        // Beim Laden des Fragments oder sobald die initialen Daten verfügbar sind
        viewModel.updateInitialSelectedJobs(args.selectedJobs.toList())

        // Beobachte die neuen ausgewählten Jobs
        val combinedSelectedJobs = viewModel.combineJobSelections()
        combinedSelectedJobs.observe(viewLifecycleOwner) { selectedJobs ->
            adapter.submitList(selectedJobs)
        }

        // Button zur Neustartung des Onboardings, ohne AlertDialog Logik hier
        val restartOnboardingButton: MaterialButton = view.findViewById(R.id.restartOnboardingButton)
        restartOnboardingButton.setOnClickListener {
            findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_viewPagerFragment)
        }
    }
}