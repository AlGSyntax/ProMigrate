package com.example.promigrate.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.DetailToDoJobApplicationAdapter
import com.example.promigrate.databinding.FragmentDetailToDoJobApplicationBinding

const val TAG2 = "DetailToDoJobApplicationFragment"

class DetailToDoJobApplicationFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var binding: FragmentDetailToDoJobApplicationBinding
    private val viewModel: MainViewModel by activityViewModels()


    private val args: DetailToDoJobApplicationFragmentArgs by navArgs()


    private val adapter = DetailToDoJobApplicationAdapter { jobTitle, _ ->
        viewModel.toggleJobSelection(jobTitle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Korrekte Verwendung von View Binding
        sharedPreferences = requireActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        binding = FragmentDetailToDoJobApplicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView mit View Binding
        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = adapter

        val savedJobs = viewModel.getSelectedJobs()
        adapter.submitList(savedJobs.toList())
        savedJobs.toList().let { viewModel.updateInitialSelectedJobs(it) }

        adapter.submitList(args.selectedJobs.toList())
        viewModel.updateInitialSelectedJobs(args.selectedJobs.toList())

        viewModel.combineJobSelections().observe(viewLifecycleOwner) { selectedJobs ->
            adapter.submitList(selectedJobs)
        }

        binding.restartOnboardingButton.setOnClickListener {
            findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_viewPagerFragment)
        }

        fun updateCombinedSelectedJobs() {
            val combinedSelectedJobs = viewModel.combineJobSelections().value ?: listOf()
            viewModel.updateCombinedSelectedJobs(combinedSelectedJobs)
        }

        // Korrekter OnClickListener für den Speichern-und-Zurück-Button
        binding.backtodashbtn.setOnClickListener {
            updateCombinedSelectedJobs()
            viewModel.combineJobSelections().observe(viewLifecycleOwner) { combinedSelectedJobs ->
                if (combinedSelectedJobs.isNotEmpty()) {
                    viewModel.saveCombinedSelectedJobs(combinedSelectedJobs)
                    if (findNavController().currentDestination?.id == R.id.detailToDoJobApplicationFragment) {
                        findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_dashboardFragment)
                    }
                } else {
                    Log.d(TAG2, "Keine kombinierten Jobs zum Speichern")
                }
            }
        }
    }
}