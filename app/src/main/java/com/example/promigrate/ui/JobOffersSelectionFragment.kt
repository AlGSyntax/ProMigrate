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
import com.example.promigrate.adapter.JobOffersSelectionAdapter
import com.example.promigrate.databinding.FragmentJobOffersSelectionBinding

class JobOffersSelectionFragment : Fragment() {

    private val TAG = "JobOffersSelectionFragment"

    private lateinit var binding: FragmentJobOffersSelectionBinding
    private val viewModel: MainViewModel by activityViewModels()
    private var selectedJobs = mutableMapOf<String, String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentJobOffersSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvJobs.layoutManager = LinearLayoutManager(context)

        val adapter = JobOffersSelectionAdapter { jobTitle, hashId, isChecked ->
            if (isChecked) {
                selectedJobs[jobTitle] = hashId
            } else {
                selectedJobs.remove(jobTitle)
            }
        }

        binding.rvJobs.adapter = adapter

        viewModel.jobOffers.observe(viewLifecycleOwner) { jobOffers ->
            if (jobOffers != null) {
                viewModel.translateJobOffers(jobOffers) { translatedJobOffers ->
                    adapter.submitList(translatedJobOffers)
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobangebote.")
            }
        }


        binding.backtodashbtn.setOnClickListener {
            viewModel.updateSelectedJobsAndPersist(selectedJobs)
            findNavController().navigateUp()
        }
    }
}
