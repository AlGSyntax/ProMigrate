package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.promigrate.databinding.FragmentJobsForYouBinding

class JobsForYouFragment : Fragment() {

    private var _binding: FragmentJobsForYouBinding? = null
    // Diese Property wird nur zwischen onCreateView und onDestroyView initialisiert.
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobsForYouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

