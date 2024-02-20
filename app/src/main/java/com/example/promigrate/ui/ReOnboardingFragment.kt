package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.promigrate.MainViewModel
import com.example.promigrate.databinding.FragmentReOnboardingBinding

class ReOnboardingFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding : FragmentReOnboardingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= FragmentReOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        viewModel.arbeitsorte.observe(viewLifecycleOwner) { arbeitsorte ->
            if (arbeitsorte != null) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    arbeitsorte
                )
                binding.arbeitsortEditText.setAdapter(adapter)
            } else {
                Toast.makeText(context, "Fehler beim Laden der Arbeitsorte", Toast.LENGTH_SHORT).show()
            }
        }

        binding.arbeitsortEditText.setOnItemClickListener { adapterView, _, position, _ ->
            val arbeitsort = adapterView.getItemAtPosition(position) as String
            val berufsfeld = viewModel.userProfileData.value?.fieldOfWork ?: ""

            if (berufsfeld.isNotBlank()) {
                viewModel.fetchJobs(berufsfeld, arbeitsort)
            } else {
                Toast.makeText(context, "Berufsfeld im Profil nicht gesetzt", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            if (jobs.isNotEmpty()) {
                val jobsAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    jobs
                )
                binding.berufEditText.setAdapter(jobsAdapter)
            } else {
                Toast.makeText(context, "Keine Jobangebote gefunden", Toast.LENGTH_SHORT).show()
            }
        }

        binding.submitButton.setOnClickListener {
            val arbeitsort = binding.arbeitsortEditText.text.toString()
            val beruf = binding.berufEditText.text.toString()

            if (arbeitsort.isNotBlank() && beruf.isNotBlank()) {
                (parentFragment as? ViewPagerFragment)?.let {
                    it.viewModel.updateJobOffers(beruf, arbeitsort)
                    it.moveToNextPage()
                }
            } else {
                Toast.makeText(context, "Bitte füllen Sie alle Felder aus", Toast.LENGTH_SHORT).show()
            }
        }


    }


}
