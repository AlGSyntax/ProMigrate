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
    private  var binding : FragmentReOnboardingBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding= FragmentReOnboardingBinding.inflate(inflater, container, false)
        return binding!!.root
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
                binding!!.arbeitsortEditText.setAdapter(adapter)

                // Stelle sicher, dass der Adapter benachrichtigt wird, wenn sich die Daten ändern
                adapter.notifyDataSetChanged()
            }
        }


        binding!!.arbeitsortEditText.setOnItemClickListener { adapterView, _, position, _ ->
            val selectedArbeitsort = adapterView.getItemAtPosition(position) as String
            viewModel.translateToGerman(selectedArbeitsort) { translatedArbeitsort ->
                val berufsfeld = viewModel.userProfileData.value?.fieldOfWork ?: ""
                if (berufsfeld.isNotBlank()) {
                    viewModel.translateToGerman(berufsfeld) { translatedBerufsfeld ->
                        viewModel.fetchJobs(translatedBerufsfeld, translatedArbeitsort)
                    }
                } else {
                    Toast.makeText(context, "Berufsfeld im Profil nicht gesetzt", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            if (jobs.isNotEmpty()) {
                viewModel.translateJobTitles(jobs) { translatedJobs ->
                    // Verwende die übersetzten Jobs, um den ArrayAdapter zu befüllen
                    val jobsAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        translatedJobs
                    )
                    binding!!.berufEditText.setAdapter(jobsAdapter)
                }
            } else {
                Toast.makeText(context, "Keine Jobangebote gefunden", Toast.LENGTH_SHORT).show()
            }
        }


        binding!!.submitButton.setOnClickListener {
            val arbeitsort = binding!!.arbeitsortEditText.text.toString()
            val beruf = binding!!.berufEditText.text.toString()

            if (arbeitsort.isNotBlank() && beruf.isNotBlank()) {
                viewModel.translateToGerman(arbeitsort) { translatedArbeitsort ->
                    viewModel.translateToGerman(beruf) { translatedBeruf ->
                        // Verwende die übersetzten Daten, um das ViewModel zu aktualisieren und zum nächsten Schritt zu navigieren.
                        (parentFragment as? ViewPagerFragment)?.let {
                            it.viewModel.updateJobOffers(translatedBeruf, translatedArbeitsort)
                            it.moveToNextPage()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Bitte füllen Sie alle Felder aus", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
