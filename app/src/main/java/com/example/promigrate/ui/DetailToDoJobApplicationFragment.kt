package com.example.promigrate.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.button.MaterialButton

const val TAG2 = "DetailToDoJobApplicationFragment"

class DetailToDoJobApplicationFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: DetailToDoJobApplicationFragmentArgs by navArgs()

    // Declare ArrayAdapter at class level
    private lateinit var arbeitsortAdapter: ArrayAdapter<String>
    private lateinit var berufAdapter: ArrayAdapter<String>

    private val adapter = DetailToDoJobApplicationAdapter { jobTitle, isChecked ->
        Log.d(TAG2, "Job selection toggled for jobTitle: $jobTitle, isChecked: $isChecked")
        if (isChecked) {
            viewModel.toggleJobSelection(jobTitle)
        } else {
            viewModel.toggleJobSelection(jobTitle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG2, "onCreateView called")
        return inflater.inflate(R.layout.fragment_detail_to_do_job_application, container, false).also {
            Log.d(TAG2, "View inflated")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG2, "onViewCreated called")

        // Initialize ArrayAdapter
        arbeitsortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        berufAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())

        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            if (jobs != null) {
                Log.d(TAG2, "Jobs erfolgreich abgerufen. Jobs count: ${jobs.size}")
                adapter.submitList(jobs)
            } else {
                Log.e(TAG2, "Fehler beim Abrufen der Jobs.")
            }
        }

        viewModel.jobs.observe(viewLifecycleOwner) { berufsfelder ->

            berufAdapter.addAll(berufsfelder)
            berufAdapter.notifyDataSetChanged()
        }

        viewModel.arbeitsorte.observe(viewLifecycleOwner) { arbeitsorte ->
            arbeitsortAdapter.addAll(arbeitsorte)
            arbeitsortAdapter.notifyDataSetChanged()
        }

        val jobTitleTextView: TextView = view.findViewById(tvJobsForYou)
        jobTitleTextView.text = args.selectedJobs.joinToString(", ")

        val recyclerView: RecyclerView = view.findViewById(rvJobs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val restartOnboardingButton: MaterialButton = view.findViewById(R.id.restartOnboardingButton)

        restartOnboardingButton.setOnClickListener {
            Log.d(TAG2, "restartOnboardingButton clicked")
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_onboarding, null)

            val arbeitsortEditText = dialogView.findViewById<AutoCompleteTextView>(R.id.arbeitsortEditText)
            val berufEditText = dialogView.findViewById<AutoCompleteTextView>(R.id.berufEditText)

            // Set ArrayAdapter to AutoCompleteTextView
            arbeitsortEditText.setAdapter(arbeitsortAdapter)
            berufEditText.setAdapter(berufAdapter)

            val builder = AlertDialog.Builder(context).setView(dialogView)
            val alertDialog = builder.show()

            viewModel.jobOffers.observe(viewLifecycleOwner) { jobOffers ->
                if (jobOffers != null) {
                    Log.d(TAG2, "Job offers successfully retrieved. Job offers count: ${jobOffers.size}")
                    adapter.submitList(jobOffers)
                } else {
                    Log.e(TAG2, "Error retrieving job offers.")
                }
            }

            dialogView.findViewById<Button>(R.id.submitButton).setOnClickListener {
                val arbeitsort = arbeitsortEditText.text.toString()
                val beruf = berufEditText.text.toString()

                if (arbeitsort.isNotBlank() && beruf.isNotBlank()) {
                    Log.d(TAG2, "submitButton clicked with arbeitsort: $arbeitsort, beruf: $beruf")
                    viewModel.updateJobOffers(was = beruf, arbeitsort = arbeitsort)
                    // Fetch jobs data again with new inputs
                    viewModel.fetchJobOffers(beruf, arbeitsort)
                    alertDialog.dismiss()
                } else {
                    Log.e(TAG2, "submitButton clicked but arbeitsort or beruf is blank")
                    Toast.makeText(context, "Bitte füllen Sie alle Felder aus", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}