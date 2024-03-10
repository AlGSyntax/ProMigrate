package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentFaqBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class FAQFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentFaqBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.backButton.setOnClickListener {
            findNavController().navigate(FAQFragmentDirections.actionFAQFragmentToSettingsFragment())
        }

        binding!!.scrollToTopFromRightArrow.setOnClickListener {
            binding!!.scrollView.scrollTo(0, 0)
        }

        val questions = listOf(
            binding!!.question1, binding!!.question2, binding!!.question3, binding!!.question4,
            binding!!.question5, binding!!.question6, binding!!.question7
        )

        val answers = listOf(
            binding!!.answer1, binding!!.answer2, binding!!.answer3, binding!!.answer4,
            binding!!.answer5, binding!!.answer6, binding!!.answer7
        )

        questions.zip(answers).forEach { (question, answer) ->
            question.setOnClickListener {
                binding!!.scrollView.scrollTo(0, answer.y.toInt())
            }
        }

        binding!!.question8.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
                .setTitle(getString(R.string.delete_account))
                .setMessage(getString(R.string.delete_account_confirmation))
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                    viewModel.deleteAccount()
                    dialog.dismiss()
                }
                .show()
        }

        viewModel.deleteAccountStatus.observe(viewLifecycleOwner) { isSuccess ->
            isSuccess?.let {
                if (it) {
                    findNavController().navigate(R.id.action_FAQFragment_to_languageSelectionFragment)
                } else {
                    Toast.makeText(context, R.string.account_deletion_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}