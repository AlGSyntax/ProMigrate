package com.example.promigrate.ui

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class VocabularyLearningFragment : Fragment() {

    private lateinit var binding: FragmentVocabularyLearningBinding
    private lateinit var adapter: VocabularyLearningAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private val userId: String by lazy {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVocabularyLearningBinding.inflate(inflater, container, false)

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
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_card, null)
        val frontEditText = view.findViewById<EditText>(R.id.frontEditText)
        val backEditText = view.findViewById<EditText>(R.id.backEditText)

        MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton("Add") { dialog, _ ->
                val frontText = frontEditText.text.toString()
                val backText = backEditText.text.toString()
                viewModel.addFlashcard(userId, frontText, backText)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun editIndexCard(indexCard: IndexCard) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_card, null)
        val editText = if (indexCard.isFlipped) {
            view.findViewById<EditText>(R.id.backEditText).apply {
                setText(indexCard.backText)
            }
        } else {
            view.findViewById<EditText>(R.id.frontEditText).apply {
                setText(indexCard.frontText)
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton("Save") { dialog, _ ->
                val newText = editText.text.toString()
                if (indexCard.isFlipped) {
                    viewModel.updateFlashcard(userId, indexCard.id!!, indexCard.frontText, newText)
                } else {
                    viewModel.updateFlashcard(userId, indexCard.id!!, newText, indexCard.backText)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
