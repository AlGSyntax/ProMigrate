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
import com.example.promigrate.R
import com.example.promigrate.adapter.VocabularyLearningAdapter
import com.example.promigrate.data.model.IndexCard
import com.example.promigrate.databinding.DialogAddCardBinding
import com.example.promigrate.databinding.DialogEditCardBinding
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
        val dialogBinding = DialogAddCardBinding.inflate(layoutInflater)
        val frontEditText = dialogBinding.frontEditText
        val backEditText = dialogBinding.backEditText

        MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setTitle(R.string.addflashcard)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val frontText = frontEditText.text.toString()
                val backText = backEditText.text.toString()
                viewModel.addFlashcard(userId, frontText, backText)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun editIndexCard(indexCard: IndexCard) {
        val binding = DialogEditCardBinding.inflate(LayoutInflater.from(context))

        val editText = if (indexCard.isFlipped) {
            binding.backEditText.apply {
                setText(indexCard.backText)
            }
        } else {
            binding.frontEditText.apply {
                setText(indexCard.frontText)
            }
        }

        MaterialAlertDialogBuilder(requireContext(),R.style.CustomAlertDialog)
            .setTitle(R.string.editflashcard)
            .setView(binding.root)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val newText = editText.text.toString()
                if (indexCard.isFlipped) {
                    viewModel.updateFlashcard(userId, indexCard.id!!, indexCard.frontText, newText)
                } else {
                    viewModel.updateFlashcard(userId, indexCard.id!!, newText, indexCard.backText)
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

}
