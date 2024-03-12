package com.example.promigrate.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.promigrate.MainViewModel
import com.example.promigrate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt für das DashboardFragment, ermöglicht den einfachen Zugriff auf
    // die UI-Komponenten.
    private lateinit var binding: FragmentDashboardBinding

    // 'args' wird verwendet, um die Argumente zu holen, die an das DashboardFragment übergeben wurden.
    // Die Verwendung von 'navArgs()' ermöglicht den sicheren und typisierten Zugriff auf diese Argumente.
    private val args: DashboardFragmentArgs by navArgs()

    /**
     * Initialisiert die Benutzeroberfläche des Fragments, indem das entsprechende Layout aufgeblasen wird und
     * die Google-Anmeldekonfiguration vorgenommen wird.
     *
     * @param inflater: Das LayoutInflater-Objekt, das zum Aufblasen des Layouts des Fragments verwendet wird.
     * @param container: Die übergeordnete ViewGroup, in den die neue Ansicht eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte View für das Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialisiert das Binding für das Fragment, um auf die UI-Elemente zugreifen zu können.
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        // Markiert den Onboarding-Prozess als abgeschlossen.
        markOnboardingComplete()

        // Beobachtet die userProfileData LiveData im ViewModel.
        // Wenn Daten vorhanden sind, werden bestimmte UI-Elemente sichtbar gemacht.
        viewModel.userProfileData.observe(viewLifecycleOwner) { userProfile ->
            if (userProfile != null) {
                // Setzt die Sichtbarkeit der Karten auf sichtbar, sobald die Benutzerdaten geladen sind.
                binding.cardTopLeft.visibility = View.VISIBLE
                binding.cardTopRight.visibility = View.VISIBLE
                binding.cardBottomLeft.visibility = View.VISIBLE
                binding.cardBottomRight.visibility = View.VISIBLE
            }
        }


        // Setzt einen OnClickListener für die obere linke Karte im Dashboard.
        binding.cardTopLeft.setOnClickListener {
            // Überprüft, ob userProfileData bereits geladen ist.
            val userProfile = viewModel.userProfileData.value
            if (userProfile?.selectedJobs != null) {
                // Verwendet die geladenen Benutzerprofildaten, wenn verfügbar.
                val jobTitlesArray = userProfile.selectedJobs!!.keys.toTypedArray()
                val refNrsArray = userProfile.selectedJobs!!.values.toTypedArray()
                val arbeitsort = userProfile.desiredLocation ?: ""

                // Erstellt eine Navigationsaktion mit den gesammelten Informationen.
                val action =
                    DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobResearchFragment(
                        selectedJobTitles = jobTitlesArray,
                        selectedJobRefNrs = refNrsArray,
                        arbeitsort = arbeitsort
                    )
                findNavController().navigate(action)
            } else {
                // Verwendet die Argumente des Fragments als Fallback, wenn keine Benutzerdaten vorhanden sind
                val selectedJobsArray = args.selectedJobTitles ?: arrayOf()
                val refNrsArray = args.selectedJobRefNrs ?: arrayOf()
                val arbeitsort = args.arbeitsort ?: ""

                // Erstellt eine Navigationsaktion mit den Argumenten des Fragments.
                val action =
                    DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobResearchFragment(
                        selectedJobTitles = selectedJobsArray,
                        selectedJobRefNrs = refNrsArray,
                        arbeitsort = arbeitsort
                    )
                findNavController().navigate(action)
            }
        }

        // Setzt einen OnClickListener für die obere rechte Karte im Dashboard.
        binding.cardTopRight.setOnClickListener {

            // Erstellt eine Navigationsaktion zur VocabularyLearningFragment.
            val action =
                DashboardFragmentDirections.actionDashboardFragmentToVocabularyLearningFragment()
            findNavController().navigate(action)
        }

        // Setzt einen OnClickListener für die untere rechte Karte im Dashboard.
        binding.cardBottomRight.setOnClickListener {

            // Erstellt eine Navigationsaktion zur RelocationAndIntegrationFragment.
            val action =
                DashboardFragmentDirections.actionDashboardFragmentToRelocationAndIntegrationFragment()
            findNavController().navigate(action)
        }


        // Setzt einen OnClickListener für die untere linke Karte im Dashboard.
        binding.cardBottomLeft.setOnClickListener {
            // Überprüft, ob userProfileData bereits geladen ist.
            val userProfile = viewModel.userProfileData.value
            if (userProfile?.selectedJobs != null) {
                // Verwendet die geladenen Benutzerprofildaten, wenn verfügbar.
                val jobTitlesArray = userProfile.selectedJobs!!.keys.toTypedArray()
                val refNrsArray = userProfile.selectedJobs!!.values.toTypedArray()
                val arbeitsort = userProfile.desiredLocation ?: ""

                // Erstellt eine Navigationsaktion mit den gesammelten Informationen.
                val action =
                    DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobApplicationFragment(
                        selectedJobTitles = jobTitlesArray,
                        selectedJobRefNrs = refNrsArray,
                        arbeitsort = arbeitsort
                    )
                findNavController().navigate(action)
            } else {
                // Verwendet die Argumente des Fragments als Fallback, wenn keine Benutzerdaten vorhanden sind
                val selectedJobsArray = args.selectedJobTitles ?: arrayOf()
                val refNrsArray = args.selectedJobRefNrs ?: arrayOf()
                val arbeitsort = args.arbeitsort ?: ""


                // Erstellt eine Navigationsaktion mit den Argumenten des Fragments.
                val action =
                    DashboardFragmentDirections.actionDashboardFragmentToDetailToDoJobApplicationFragment(
                        selectedJobTitles = selectedJobsArray,
                        selectedJobRefNrs = refNrsArray,
                        arbeitsort = arbeitsort
                    )
                findNavController().navigate(action)
            }
        }


        // Setzt einen OnClickListener für das Profilbild im Dashboard.
        binding.tosettingsfragbtn.setOnClickListener {
            // Erstellt eine Navigationsaktion zum SettingsFragment.
            val action = DashboardFragmentDirections.actionDashboardFragmentToSettingsFragment()
            findNavController().navigate(action)
        }


        // Gibt die erstellte View für das Fragment zurück.
        return binding.root
    }

    // Markiert den Onboarding-Prozess als abgeschlossen, indem ein entsprechender Wert in den Shared Preferences gespeichert wird.
    private fun markOnboardingComplete() {
        // Zugriff auf die Shared Preferences der App
        val sharedPref =
            activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            // Setzt den Wert von "OnboardingComplete" auf true und speichert die Änderungen.
            putBoolean("OnboardingComplete", true)
            // Wendet die Änderungen an die Shared Preferences an.
            apply()
        }
    }
}