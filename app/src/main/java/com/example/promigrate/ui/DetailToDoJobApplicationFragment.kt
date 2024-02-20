package com.example.promigrate.ui

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

    private lateinit var binding: FragmentDetailToDoJobApplicationBinding
    private val viewModel: MainViewModel by activityViewModels()
    private val args: DetailToDoJobApplicationFragmentArgs by navArgs()

    private val adapter = DetailToDoJobApplicationAdapter { jobTitle, _ ->
        viewModel.toggleJobSelection(jobTitle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailToDoJobApplicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = adapter

        val selectedJobs = args.selectedJobs.toSet().toList()
        viewModel._selectedJobs.value = selectedJobs.toSet()
        //Live Data im viewModel überschreiben 'selectedJobs'



        viewModel.selectedJobs.observe(viewLifecycleOwner) { selectedJobsSet ->
            Log.d(TAG2, "Beobachte Änderungen in selectedJobs: $selectedJobsSet")
            // Konvertiere das Set in eine Liste, die vom Adapter verwendet werden kann
            val selectedJobsList = selectedJobsSet.toList()
            // Aktualisiere den Adapter mit der neuen Liste
            adapter.submitList(selectedJobsList)
            // Optional: Eine Log-Nachricht ausgeben
            Log.d(TAG2, "Aktualisierte Liste der ausgewählten Jobs: $selectedJobsList")
        }



        // Mit geupdateter Live Data befüllen

        //AUfjedenfall einen observer damit der Adapter neu befüllt wird .

        binding.restartOnboardingButton.setOnClickListener {
            findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_viewPagerFragment)
        }

        binding.backtodashbtn.setOnClickListener {
            viewModel.updateSelectedJobsAndPersist(selectedJobs.toSet())
            if (findNavController().currentDestination?.id == R.id.detailToDoJobApplicationFragment) {
                findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_dashboardFragment)
            }
            Log.d(TAG2, "No jobs selected")
        }
    }
}