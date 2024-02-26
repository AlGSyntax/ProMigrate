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
import com.example.promigrate.adapter.DetailToDoJobResearchAdapter
import com.example.promigrate.databinding.FragmentDetailToDoJobResearchBinding

class DetailToDoJobResearchFragment : Fragment() {

    private lateinit var binding: FragmentDetailToDoJobResearchBinding
    private val viewModel: MainViewModel by activityViewModels()
    private val args: DetailToDoJobResearchFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailToDoJobResearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DetailToDoJobResearchAdapter { hashId ->
            viewModel.fetchJobDetails(hashId) // Ruft die Methode im ViewModel auf, um Details zu holen
        }

        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = adapter

        // Verwende die ausgewählten Jobtitel und Hash-IDs aus den Argumenten
        args.selectedJobHashIds.let { hashIds ->
            args.selectedJobTitles.forEachIndexed { index, jobTitle ->
                if (index < hashIds.size) {
                    adapter.submitList(hashIds.map { jobTitle to it })
                }
            }
        }

        viewModel.jobDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { jobDetails ->
                // Aktualisiere die UI mit den Jobdetails
                // Hier könntest du ein Dialogfenster oder eine erweiterte Ansicht verwenden, um die Details anzuzeigen
                Log.d(TAG, "Jobdetails geladen: $jobDetails")
            }.onFailure { exception ->
                Log.e(TAG, "Fehler beim Laden der Jobdetails", exception)
            }
        }

        binding.backtodashbtn2.setOnClickListener {
            findNavController().navigate(DetailToDoJobResearchFragmentDirections.actionDetailToDoJobResearchFragmentToDashboardFragment())
        }
    }

    companion object {
        private const val TAG = "DetailToDoJobResearchFragment"
    }
}

