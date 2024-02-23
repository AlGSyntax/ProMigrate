package com.example.promigrate.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.promigrate.MainViewModel
import com.example.promigrate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: MainViewModel by activityViewModels()

    private val args: DashboardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        markOnboardingComplete()

        viewModel.userProfileData.observe(viewLifecycleOwner) { userProfile ->
            if (userProfile != null) {
                binding.cardTopLeft.visibility = View.VISIBLE
                binding.cardTopRight.visibility = View.VISIBLE
                binding.cardBottomLeft.visibility = View.VISIBLE
                binding.cardBottomRight.visibility = View.VISIBLE
            }
        }

        binding.cardBottomLeft.setOnClickListener {
            // Check if userProfileData is already loaded
            val userProfile = viewModel.userProfileData.value
            if (userProfile?.selectedJobs != null) {
                // If userProfileData is loaded, use it
                val jobTitlesArray = userProfile.selectedJobs!!.keys.toTypedArray()
                val hashIdsArray = userProfile.selectedJobs!!.values.toTypedArray()
                val arbeitsort = userProfile.desiredLocation ?: ""

                val action = DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobApplicationFragment(
                    selectedJobTitles = jobTitlesArray,
                    selectedJobHashIds = hashIdsArray,
                    arbeitsort = arbeitsort
                )
                findNavController().navigate(action)
            }else {
                // Im 'else'-Teil: Verwende die Argumente aus den FragmentArgs
                val selectedJobsArray = args.selectedJobTitles ?: arrayOf()
                val hashIdsArray = args.selectedJobHashIds ?: arrayOf()

                val arbeitsort = args.arbeitsort ?: ""

                val action = DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobApplicationFragment(
                    selectedJobTitles = selectedJobsArray,
                    selectedJobHashIds = hashIdsArray,
                    arbeitsort = arbeitsort
                )
                findNavController().navigate(action)
            }
        }


        binding.cardTopLeft.setOnClickListener {
            val userProfile = viewModel.userProfileData.value
            if (userProfile?.selectedJobs != null) {
                // If userProfileData is loaded, use it
                val jobTitlesArray = userProfile.selectedJobs!!.keys.toTypedArray()
                val hashIdsArray = userProfile.selectedJobs!!.values.toTypedArray()
                val arbeitsort = userProfile.desiredLocation ?: ""

                val action = DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobResearchFragment(
                    selectedJobTitles = jobTitlesArray,
                    selectedJobHashIds = hashIdsArray,
                    arbeitsort = arbeitsort
                )
                findNavController().navigate(action)
            } else {
                // Im 'else'-Teil: Verwende die Argumente aus den FragmentArgs
                val selectedJobsArray = args.selectedJobTitles ?: arrayOf()
                val hashIdsArray = args.selectedJobHashIds ?: arrayOf()

                val arbeitsort = args.arbeitsort ?: ""

                val action = DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobResearchFragment(
                    selectedJobTitles = selectedJobsArray,
                    selectedJobHashIds = hashIdsArray,
                    arbeitsort = arbeitsort
                )
                findNavController().navigate(action)
            }
        }



        return binding.root
    }


    private fun markOnboardingComplete() {
        val sharedPref = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean("OnboardingComplete", true)
            apply()
        }
    }
}