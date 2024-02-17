package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.R.id.rvJobs
import com.example.promigrate.R.id.tvJobsForYou
import com.example.promigrate.adapter.DetailToDoJobApplicationAdapter

class DetailToDoJobApplicationFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: DetailToDoJobApplicationFragmentArgs by navArgs()

    // Erstellen Sie eine Instanz des Adapters
    private val adapter = DetailToDoJobApplicationAdapter{ jobTitle, isChecked ->
        if (isChecked) {
            viewModel.toggleJobSelection(jobTitle)
        } else {
            viewModel.toggleJobSelection(jobTitle)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail_to_do_job_application, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val jobTitleTextView: TextView = view.findViewById(tvJobsForYou)
        jobTitleTextView.text = args.selectedJobs.joinToString(", ")



        // Binden Sie den Adapter an die RecyclerView
        val recyclerView: RecyclerView = view.findViewById(rvJobs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Beobachten Sie die Daten und reichen Sie sie an den Adapter weiter, wenn sie sich ändern
        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            if (jobs != null) {
                Log.d(TAG, "Jobs erfolgreich abgerufen.")
                adapter.submitList(jobs)
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobs.")
            }
        }
    }
}