package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.JobOffersSelectionAdapter

class JobOffersSelectionFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_job_offers_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvJobs)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = JobOffersSelectionAdapter { jobTitle, isSelected ->
            // Diese Methode wird aufgerufen, wenn ein Job ausgewählt wird
            if (isSelected) {
                viewModel.addJobSelection(jobTitle)
            } else {
            }
        }
        recyclerView.adapter = adapter

        viewModel.jobOffers.observe(viewLifecycleOwner) { jobOffers ->
            adapter.submitList(jobOffers)
        }

        view.findViewById<Button>(R.id.backtodashbtn).setOnClickListener {
            viewModel.saveSelectedJobs()
            // Navigiere zurück im Navigationsstack
            findNavController().navigateUp()
        }
    }
}
