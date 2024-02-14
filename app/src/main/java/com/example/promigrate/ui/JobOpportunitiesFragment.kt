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

        // Setup RecyclerView
        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = jobsAdapter

        val selectedJob = "" // replace "SomeJob" with the actual job
        viewModel.fetchJobOffers(selectedJob)

        viewModel.selectedJobs.observe(viewLifecycleOwner) { jobDetails ->
            if (jobDetails != null) {
                Log.d(TAG, "Jobdetails erfolgreich abgerufen.")
                jobsAdapter.submitList(jobDetails.toList())
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobdetails.")
            }
        }
    }
}