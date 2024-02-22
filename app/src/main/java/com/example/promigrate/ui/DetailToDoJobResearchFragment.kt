package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

        // Adapter-Initialisierung mit Klick-Listener
        val adapter = DetailToDoJobResearchAdapter { encodedHashID ->
            viewModel.fetchJobDetails(encodedHashID) // Ruft die Methode im ViewModel auf
        }
        binding.rvJobs.layoutManager = LinearLayoutManager(context)
        binding.rvJobs.adapter = adapter

        // Beobachte die userProfileData für die Liste der ausgewählten Jobs
        viewModel.userProfileData.observe(viewLifecycleOwner) { profile ->
            val selectedJobsList = profile?.selectedJobs?.toList() ?: emptyList()
            adapter.submitList(selectedJobsList)
        }

        // Beobachte die jobDetails LiveData im ViewModel, um die Details anzuzeigen
        viewModel.jobDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { jobDetails ->
                // Aktualisiere die UI mit den Jobdetails
                // Du könntest zum Beispiel ein Dialogfenster oder eine erweiterte Ansicht in der CardView anzeigen
                Log.d(TAG3, "Jobdetails geladen: $jobDetails")
            }.onFailure { exception ->
                // Zeige eine Fehlermeldung an
                Log.e(TAG3, "Fehler beim Laden der Jobdetails", exception)
            }
        }
    }

    companion object {
        private const val TAG3 = "DetailToDoJobResearchFragment"
    }
}

