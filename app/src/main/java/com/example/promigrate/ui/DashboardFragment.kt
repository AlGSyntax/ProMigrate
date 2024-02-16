package com.example.promigrate.ui

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

    // Verwende die generierten Safe Args, um auf die Argumente zuzugreifen
    private val args: DashboardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.cardBottomLeft.setOnClickListener {
            // Verwende die Argumente, die bereits im DashboardFragment vorhanden sind
            val selectedJobsArray = args.selectedJobs
            val arbeitsort = args.arbeitsort

            // Richtige Navigationsaktion, die zum DetailToDoJobApplicationFragment führt
            val action = DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobApplicationFragment(
                selectedJobs = selectedJobsArray,
                arbeitsort = arbeitsort
            )
            findNavController().navigate(action)
        }


        return binding.root
    }
}