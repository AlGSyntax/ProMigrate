package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.adapter.DetailToDoJobResearchAdapter
import com.example.promigrate.databinding.FragmentDetailToDoJobResearchBinding

class DetailToDoJobResearchFragment : Fragment() {

    private lateinit var binding: FragmentDetailToDoJobResearchBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailToDoJobResearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter-Initialisierung mit Klick-Listener
        val adapter = DetailToDoJobResearchAdapter { encodedHashID ->
            viewModel.fetchJobDetails(encodedHashID) // Ruft die Methode im ViewModel auf
        }
        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = adapter

        // Beobachte die userProfileData für die Liste der ausgewählten Jobs
        viewModel.userProfileData.observe(viewLifecycleOwner) { profile ->
            val selectedJobsList = profile?.selectedJobs?.toList() ?: emptyList()
            val arbeitsort = profile?.desiredLocation ?: ""

            // Rufe fetchJobOffers einmalig mit allen ausgewählten Jobs auf
            if (selectedJobsList.isNotEmpty()) {
                viewModel.fetchJobOffers(selectedJobsList.joinToString(","), arbeitsort)
            }
        }

        // Beobachte die jobOffers LiveData im ViewModel, um die Liste der Angebote zu aktualisieren
        viewModel.jobOffers.observe(viewLifecycleOwner) {
            // Die jobOffers LiveData sollte eine Liste von JobOfferDetail-Objekten sein

        }

        // Beobachte die jobDetails LiveData im ViewModel, um die Details anzuzeigen
        viewModel.jobDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { jobDetails ->
                // Aktualisiere die UI mit den Jobdetails
                Log.d(TAG, "Jobdetails geladen: $jobDetails")
            }.onFailure { exception ->
                // Zeige eine Fehlermeldung an
                Log.e(TAG, "Fehler beim Laden der Jobdetails", exception)
            }
        }
    }

    companion object {
        private const val TAG = "DetailToDoJobResearchFragment"
    }
}

