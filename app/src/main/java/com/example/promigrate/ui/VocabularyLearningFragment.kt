package com.example.promigrate.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.VocabularyLearningAdapter
import com.example.promigrate.data.model.IndexCard
import com.example.promigrate.databinding.FragmentVocabularyLearningBinding
import com.google.firebase.auth.FirebaseAuth

class VocabularyLearningFragment : Fragment() {

    private var _binding: FragmentVocabularyLearningBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VocabularyLearningAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private var userId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVocabularyLearningBinding.inflate(inflater, container, false)
        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid ?: ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VocabularyLearningAdapter { indexCard ->
            editIndexCard(indexCard)
        }
        binding.rvLanguageCourses.layoutManager = LinearLayoutManager(context)
        binding.rvLanguageCourses.adapter = adapter

        viewModel.getFlashcards(userId).observe(viewLifecycleOwner) { flashcards ->
            adapter.submitList(flashcards)
        }

        binding.addFlashcardButton.setOnClickListener {
            addNewIndexCard()
        }

        binding.findLanguageCourseButton.setOnClickListener {
            findNavController().navigate(VocabularyLearningFragmentDirections.actionVocabularyLearningFragmentToLanguageCourseFragment())
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(VocabularyLearningFragmentDirections.actionVocabularyLearningFragmentToDashboardFragment())
        }
    }



    private fun addNewIndexCard() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_card, null)
        val frontEditText = dialogView.findViewById<EditText>(R.id.frontEditText)
        val backEditText = dialogView.findViewById<EditText>(R.id.backEditText)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val frontText = frontEditText.text.toString()
                val backText = backEditText.text.toString()
                viewModel.addFlashcard(userId, frontText = frontText, backText = backText)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editIndexCard(indexCard: IndexCard) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_card, null)
        val frontEditText = dialogView.findViewById<EditText>(R.id.frontEditText).apply {
            setText(indexCard.frontText)
        }
        val backEditText = dialogView.findViewById<EditText>(R.id.backEditText).apply {
            setText(indexCard.backText)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newFrontText = frontEditText.text.toString()
                val newBackText = backEditText.text.toString()
                indexCard.id?.let {
                    viewModel.updateFlashcard(userId,
                        it, newFrontText, newBackText)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}