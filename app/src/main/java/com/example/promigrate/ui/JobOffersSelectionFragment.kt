package com.example.promigrate.ui

import android.os.Bundle
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

    private lateinit var binding: FragmentJobOffersSelectionBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentJobOffersSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvJobs.layoutManager = LinearLayoutManager(context)

        val adapter = JobOffersSelectionAdapter { jobTitle, isSelected ->
            // This method is called when a job is selected or deselected
            viewModel.toggleJobSelection(jobTitle)
        }
        binding.rvJobs.adapter = adapter

        viewModel.jobOffers.observe(viewLifecycleOwner) { jobOffers ->
            adapter.submitList(jobOffers)
        }

        binding.backtodashbtn.setOnClickListener {
            viewModel.saveSelectedJobs()
            // Navigate back in the navigation stack
            findNavController().navigateUp()
        }
    }
}
