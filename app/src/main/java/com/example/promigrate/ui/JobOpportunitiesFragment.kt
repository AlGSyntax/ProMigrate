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
        binding.finishbtn.setOnClickListener{


            // Hole die aktuell ausgewählten Jobs aus dem ViewModel
            val selectedJobsArray = viewModel.selectedJobs.value?.toTypedArray() ?: arrayOf()

            // Hole den Arbeitsort aus den Fragment-Argumenten
            val args = JobOpportunitiesFragmentArgs.fromBundle(requireArguments())
            val arbeitsort = args.arbeitsort

            // Navigiere zum DashboardFragment mit den gesammelten Daten
            val action = JobOpportunitiesFragmentDirections.actionJobOpportunitiesFragmentToDashboardFragment(
                arbeitsort = arbeitsort,
                selectedJobs = selectedJobsArray
            )
            viewModel.saveSelectedJobs()
            findNavController().navigate(action)
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
                jobsAdapter.submitList(jobOffers.toList())
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobangebote.")
            }
        }
    }
}