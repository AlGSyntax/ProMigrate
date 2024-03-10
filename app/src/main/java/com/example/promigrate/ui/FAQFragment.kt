package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.promigrate.databinding.FragmentFaqBinding

class FAQFragment : Fragment() {

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
            binding!!.question1, binding!!.question2, binding!!.question3, binding!!.question4, binding!!.question5,
            binding!!.question6, binding!!.question7)

        val answers = listOf(
            binding!!.answer1, binding!!.answer2, binding!!.answer3, binding!!.answer4, binding!!.answer5,
            binding!!.answer6, binding!!.answer7)

        questions.zip(answers).forEach { (question, answer) ->
            question.setOnClickListener {
                binding!!.scrollView.scrollTo(0, answer.y.toInt())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}
