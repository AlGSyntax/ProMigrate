package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.adapter.LanguageCourseAdapter
import com.example.promigrate.databinding.FragmentLanguageCourseBinding

class LanguageCourseFragment : Fragment() {

    private var _binding: FragmentLanguageCourseBinding? = null
    private val binding get() = _binding!!

    private lateinit var languageCourseAdapter: LanguageCourseAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLanguageCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()



        viewModel.fetchBildungsangebote(systematiken = "MC", orte = "Hannover_30159_9.741932_52.375891", sprachniveau = "MC01 2", beginntermine = 1)
        binding.backToDashboardButton.setOnClickListener {
            val action = LanguageCourseFragmentDirections.actionLanguageCourseFragmentToVocabularyLearningFragment()
            findNavController().navigate(action)
        }

    }


    private fun setupRecyclerView() {
        languageCourseAdapter = LanguageCourseAdapter { _, _ -> }
        binding.rvLanguageCourses.apply {
            adapter = languageCourseAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeData() {
        viewModel.bildungsangebote.observe(viewLifecycleOwner) { angebote ->
            languageCourseAdapter.submitList(angebote)
        }
    }

}
