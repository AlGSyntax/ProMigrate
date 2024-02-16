package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.promigrate.databinding.FragmentDetailToDoJobApplicationBinding

class DetailToDoJobApplicationFragment : Fragment() {

    private var _binding: FragmentDetailToDoJobApplicationBinding? = null
    private val binding get() = _binding!!

    // Verwende navArgs, um auf die übergebenen Argumente zuzugreifen
    private val args: DetailToDoJobApplicationFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDetailToDoJobApplicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("DetailFragment", "Selected Jobs: ${args.selectedJobs.joinToString(", ")}")
        Log.d("DetailFragment", "Arbeitsort: ${args.arbeitsort}")

        binding.textViewSelectedJobs.text = args.selectedJobs.joinToString(", ")
        binding.textViewArbeitsort.text = args.arbeitsort
    }


}
