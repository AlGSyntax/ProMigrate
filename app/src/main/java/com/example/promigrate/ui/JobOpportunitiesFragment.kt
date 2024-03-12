package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.JobOpportunitiesAdapter
import com.example.promigrate.databinding.FragmentJobOportunitiesBinding

/**
 * JobOpportunitiesFragment stellt eine Benutzeroberfläche dar, die dem Benutzer eine Liste von Jobmöglichkeiten zeigt.
 * Der Benutzer kann einzelne Jobangebote auswählen, um weitere Details zu diesen Jobs anzuzeigen.
 * Die Klasse nutzt JobOpportunitiesAdapter, um die Jobliste anzuzeigen und interagiert mit dem [MainViewModel],
 * um die benötigten Daten zu holen und Benutzerinteraktionen zu verarbeiten.
 */
class JobOpportunitiesFragment : Fragment() {

    private val TAG = "JobOpportunitiesFragment"

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentJobOportunitiesBinding? = null

    // Definiert ein MutableMap für ausgewählte Jobreferenznummern, die den Jobtiteln zugeordnet sind.
    private var selectedJobRefNrs = mutableMapOf<String, String>()

    // Initialisiert den JobOpportunitiesAdapter mit einem Lambda-Callback. Dieser Callback wird
    // aufgerufen, wenn der Zustand einer Checkbox in der Jobliste geändert wird.
    private val jobsAdapter = JobOpportunitiesAdapter { jobTitle, refNr, isChecked ->
        // Prüft den Zustand der Checkbox. Wenn die Checkbox markiert ist (isChecked == true),
        // fügt den Jobtitel und die Referenznummer zum selectedJobRefNrs-Map hinzu.
        // Andernfalls, wenn die Checkbox nicht markiert ist, wird der Eintrag aus dem Map entfernt.
        if (isChecked) {
            selectedJobRefNrs[jobTitle] = refNr
        } else {
            selectedJobRefNrs.remove(jobTitle)
        }
    }

    /**
     * Initialisiert die Benutzeroberfläche des Fragments, indem das entsprechende Layout aufgeblasen wird.
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
        binding = FragmentJobOportunitiesBinding.inflate(inflater, container, false)
        // Gibt die Wurzelansicht des Fragments zurück.
        return binding!!.root
    }

    /**
     * Wird aufgerufen, nachdem die Ansicht des Fragments und seine hierarchische Struktur instanziiert wurden.
     * In dieser Methode werden weitere UI-Initialisierungen vorgenommen und Listener für UI-Elemente eingerichtet.
     *
     * @param view: Die erstellte Ansicht des Fragments.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Setzt den LayoutManager für das RecyclerView, der bestimmt, wie die Elemente angeordnet werden.
        // Hier wird ein LinearLayoutManager verwendet, der die Elemente in einer vertikalen Liste anordnet.
        binding!!.rvJobs.layoutManager = LinearLayoutManager(context)

        // Weist dem zuvor erstellten jobsAdapter die RecyclerView zu.
        // Dieser Adapter ist verantwortlich für die Bereitstellung der Ansichten
        // (View-Objekte), die die Daten des RecyclerView repräsentieren.
        binding!!.rvJobs.adapter = jobsAdapter


        // Setzt einen ClickListener für den "Finish"-Button.
        binding!!.finishbtn.setOnClickListener {

            // Extrahiert Argumente aus den Bundle-Daten, die von dem vorherigen Fragment übergeben wurden.
            val args = JobOpportunitiesFragmentArgs.fromBundle(requireArguments())
            val arbeitsort = args.arbeitsort

            // Erstellt eine Navigationsaktion zum DashboardFragment, inklusive der ausgewählten Jobtitel, Referenznummern und dem Arbeitsort.
            val action =
                JobOpportunitiesFragmentDirections.actionJobOpportunitiesFragmentToDashboardFragment(
                    selectedJobTitles = selectedJobRefNrs.keys.toTypedArray(),
                    selectedJobRefNrs = selectedJobRefNrs.values.toTypedArray(),
                    arbeitsort = arbeitsort
                )

            // Aktualisiert die ausgewählten Jobs im ViewModel und persistiert diese Informationen.
            viewModel.updateSelectedJobsAndPersist(selectedJobRefNrs)

            // Führt die Navigation aus, falls das aktuelle Ziel noch das JobOpportunitiesFragment ist.
            if (findNavController().currentDestination?.id != R.id.dashboardFragment) {
                findNavController().navigate(action)
            }
        }

        // Argumente, die von einem anderen Fragment an dieses Fragment übergeben werden.
        val args = JobOpportunitiesFragmentArgs.fromBundle(requireArguments())
        val selectedJobs = args.selectedJobs.toList() // Liste der ausgewählten Jobtitel.
        val arbeitsort = args.arbeitsort // Der Arbeitsort, der als Argument übergeben wurde.

        // Übersetzt den Arbeitsort ins Deutsche, um die Abfrage an die API korrekt zu stellen.
        viewModel.translateToGerman(arbeitsort) { translatedArbeitsort ->
            // Iteriert über die Liste der ausgewählten Jobtitel.
            selectedJobs.forEach { selectedJob ->
                // Übersetzt den ausgewählten Job ins Deutsche, um die Abfrage an die API korrekt zu stellen.
                viewModel.translateToGerman(selectedJob) { translatedJobs ->
                    // Ruft die Jobangebote ab, die zu den übersetzten Jobtiteln und dem übersetzten Arbeitsort passen.
                    viewModel.fetchJobOffers(
                        was = translatedJobs,
                        arbeitsort = translatedArbeitsort
                    )
                }
            }
        }

        // Beobachtet Änderungen an den Stellenangeboten im MainViewModel. Wenn Stellenangebote verfügbar sind, werden diese verarbeitet.
        viewModel.jobOffers.observe(viewLifecycleOwner) { jobOffers ->
            // Überprüft, ob die Stellenangebotsdaten erfolgreich abgerufen wurden.
            if (jobOffers != null) {
                Log.d(TAG, "Jobangebote erfolgreich abgerufen.")
                // Übersetzt die Jobtitel mithilfe der im ViewModel definierten Funktion translateJobTitles.
                viewModel.translateJobOffers(jobOffers) { translatedJobOffers ->
                    // Aktualisiert die Liste im Adapter mit den ggf. übersetzten Jobtiteln.
                    jobsAdapter.submitList(translatedJobOffers)
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Jobangebote.")
            }
        }


    }

    /**
     * Wird aufgerufen, wenn die View-Hierarchie des Fragments zerstört wird.
     * Hier wird das Binding-Objekt auf null gesetzt, um Memory Leaks zu vermeiden,
     * da das Binding-Objekt eine Referenz auf die View hält, welche nicht länger existiert.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}