package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.adapter.JobsAdapter
import com.example.promigrate.databinding.FragmentJobsForYouBinding

class JobsForYouFragment : Fragment() {

    private lateinit var binding: FragmentJobsForYouBinding
    private val viewModel: MainViewModel by activityViewModels()

    // Erstelle eine Instanz deines Adapters
    private val jobsAdapter = JobsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJobsForYouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Angenommene Parameter aus dem Bundle
        val berufsfeld = arguments?.getString("berufsfeld") ?: ""
        val arbeitsort = arguments?.getString("wo") ?: ""


// Aufruf der angepassten Methode
        viewModel.fetchJobs(berufsfeld, arbeitsort)


        // Beobachte die Jobliste im ViewModel und reiche die Daten an den Adapter weiter
        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            jobsAdapter.submitList(jobs)
        }
    }


    private fun setupRecyclerView() {
        // Setze den LayoutManager und den Adapter für den RecyclerView
        binding.rvJobs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = jobsAdapter
        }
    }
}


