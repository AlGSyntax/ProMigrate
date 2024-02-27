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
import com.example.promigrate.R
import com.example.promigrate.adapter.JobOpportunitiesAdapter
import com.example.promigrate.databinding.FragmentJobOportunitiesBinding


class JobOpportunitiesFragment : Fragment() {

    private lateinit var binding: FragmentJobOportunitiesBinding
    private val viewModel: MainViewModel by activityViewModels()

    private var selectedJobHashIds = mutableMapOf<String, String>()


    private val jobsAdapter = JobOpportunitiesAdapter { jobTitle, hashId, isChecked ->
        if (isChecked) {
            selectedJobHashIds[jobTitle] = hashId
        } else {
            selectedJobHashIds.remove(jobTitle)
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
        binding.finishbtn.setOnClickListener {
            // Get the currently selected jobs from the ViewModel
            // Get the workplace from the fragment arguments
            val args = JobOpportunitiesFragmentArgs.fromBundle(requireArguments())
            val arbeitsort = args.arbeitsort

            // Navigate to the DashboardFragment with the collected data
            val action = JobOpportunitiesFragmentDirections.actionJobOpportunitiesFragmentToDashboardFragment(
                selectedJobTitles = selectedJobHashIds.keys.toTypedArray(),
                selectedJobHashIds = selectedJobHashIds.values.toTypedArray(),
                arbeitsort = arbeitsort
            )

            viewModel.updateSelectedJobsAndPersist(selectedJobHashIds)

            // Check if the current destination is not the dashboardFragment before navigating
            if (findNavController().currentDestination?.id != R.id.dashboardFragment) {
                findNavController().navigate(action)
            }
        }

        val args = JobOpportunitiesFragmentArgs.fromBundle(requireArguments())
        val selectedJobs = args.selectedJobs.toList()
        val arbeitsort = args.arbeitsort

        viewModel.translateToGerman(arbeitsort) { translatedArbeitsort ->
            selectedJobs.forEach { selectedJob ->
                viewModel.translateToGerman(selectedJob) { translatedJobs ->
                    viewModel.fetchJobOffers(was =translatedJobs, arbeitsort = translatedArbeitsort)
                }
            }
        }

        viewModel.jobOffers.observe(viewLifecycleOwner) { jobOffers ->
            if (jobOffers != null) {
                Log.d(TAG, "Jobangebote erfolgreich abgerufen.")
                viewModel.translateJobOffers(jobOffers) { translatedJobOffers ->
                    jobsAdapter.submitList(translatedJobOffers)
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobangebote.")
            }
        }



    }
}