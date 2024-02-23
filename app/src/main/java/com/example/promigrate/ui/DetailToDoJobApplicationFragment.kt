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
import com.example.promigrate.adapter.DetailToDoJobApplicationAdapter
import com.example.promigrate.databinding.FragmentDetailToDoJobApplicationBinding

const val TAG2 = "DetailToDoJobApplicationFragment"

class DetailToDoJobApplicationFragment : Fragment() {

    private lateinit var binding: FragmentDetailToDoJobApplicationBinding
    private val viewModel: MainViewModel by activityViewModels()

    private val adapter = DetailToDoJobApplicationAdapter { jobTitle, isChecked ->
        // Hole die Hash-ID für den gewählten Jobtitel
        val hashId = viewModel.userProfileData.value?.selectedJobs?.get(jobTitle) ?: ""
        if (isChecked) {
            viewModel.toggleJobSelection(jobTitle, hashId)
        } else {
            viewModel.toggleJobSelection(jobTitle, "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailToDoJobApplicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = adapter

        viewModel.userProfileData.observe(viewLifecycleOwner) { profile ->
            val selectedJobsList = profile?.selectedJobs?.keys?.toList() ?: emptyList()
            adapter.submitList(selectedJobsList)
            Log.d(TAG2, "Aktualisierte Liste der ausgewählten Jobs: $selectedJobsList")
        }


        binding.restartOnboardingButton.setOnClickListener {
            findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_viewPagerFragment)
        }

        binding.backtodashbtn.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.detailToDoJobApplicationFragment) {
                findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_dashboardFragment)
            }
            Log.d(TAG2, "Navigation back to dashboard")
        }
    }
}
