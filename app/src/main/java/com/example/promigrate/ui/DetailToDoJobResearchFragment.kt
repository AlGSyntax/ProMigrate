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


    private val viewModel: MainViewModel by activityViewModels()
    private  var binding: FragmentDetailToDoJobResearchBinding? = null
    private val args: DetailToDoJobResearchFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailToDoJobResearchBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DetailToDoJobResearchAdapter { refnr ->
            viewModel.fetchJobDetails(refnr)
        }

        binding!!.rvJobs.layoutManager = LinearLayoutManager(context)
        binding!!.rvJobs.adapter = adapter

        args.selectedJobRefNrs.let { refNrs ->
            adapter.submitList(args.selectedJobTitles.zip(refNrs))
        }

        viewModel.jobDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { jobDetails ->
                viewModel.translateJobDetails(jobDetails) { translatedJobDetails ->
                    translatedJobDetails.refnr?.let { refnr ->
                        adapter.setJobDetails(refnr, translatedJobDetails)
                    }// Datenfluss nachverfolgen , um zu gucken wo es nicht richtig geladen wird und dann die Fehler beheben
                }
            }.onFailure { exception ->
                Log.e(TAG, "Fehler beim Laden der Jobdetails", exception)
            }
        }


        binding!!.backtodashbtn2.setOnClickListener {
            findNavController().navigate(DetailToDoJobResearchFragmentDirections.actionDetailToDoJobResearchFragmentToDashboardFragment())
        }
    }

    companion object {
        private const val TAG = "DetailToDoJobResearchFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

