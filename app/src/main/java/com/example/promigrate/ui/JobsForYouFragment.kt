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

        binding.tvJobsForYou.text
        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = jobsAdapter
        binding.saveandnextbtn.setOnClickListener {
            viewModel.selectedJobs.observe(viewLifecycleOwner) { selectedJobs ->
                val selectedJobsArray = selectedJobs.toTypedArray()
                val arbeitsort = arguments?.getString("wo") ?: ""
                val action = JobsForYouFragmentDirections.actionJobsForYouFragmentToJobOportunitiesFragment(selectedJobsArray,
                    arrayOf(arbeitsort)
                )
                findNavController().navigate(action)
            }
        }

        val berufsfeld = arguments?.getString("berufsfeld") ?: ""
        val arbeitsort = arguments?.getString("wo") ?: ""

        viewModel.translateToGerman(berufsfeld) { translatedBerufsfeld ->
            viewModel.translateToGerman(arbeitsort) { translatedArbeitsort ->
                viewModel.fetchJobs(berufsfeld = translatedBerufsfeld, arbeitsort = translatedArbeitsort)
            }
        }

        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            if (jobs != null) {
                Log.d(TAG, "Jobs erfolgreich abgerufen.")
                viewModel.translateJobTitles(jobs) { translatedJobs ->
                    jobsAdapter.submitList(translatedJobs)
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobs.")
            }
        }
    }
}