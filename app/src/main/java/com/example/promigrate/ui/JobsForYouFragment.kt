package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.JobsForYouAdapter
import com.example.promigrate.databinding.FragmentJobsForYouBinding


/**
 * Ein Fragment, das die für den Benutzer relevanten Jobs anzeigt. Es ermöglicht dem Benutzer,
 * verschiedene Jobs auszuwählen und seine Präferenzen für zukünftige Jobvorschläge zu speichern.
 */
class JobsForYouFragment : Fragment() {



    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentJobsForYouBinding? = null

    // Initialisiert ein veränderbares Set, um ausgewählte Jobtitel zu speichern.
    private var selectedJobs = mutableSetOf<String>()


    /**
     * Der Adapter für die Job-Liste. Hier wird eine Instanz von JobsForYouAdapter erstellt,
     * die eine Lambda-Funktion als Argument nimmt. Diese Lambda-Funktion definiert, was passieren soll,
     * wenn ein Item in der Liste angeklickt wird.
     */
    private val jobsForYouAdapter = JobsForYouAdapter { jobTitle, isChecked ->
        if (isChecked) {
            selectedJobs.add(jobTitle)
        } else {
            selectedJobs.remove(jobTitle)
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
        binding = FragmentJobsForYouBinding.inflate(inflater, container, false)
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

        // Weist dem zuvor erstellten jobsForYouAdapter die RecyclerView zu.
        // Dieser Adapter ist verantwortlich für die Bereitstellung der Ansichten
        // (View-Objekte), die die Daten des RecyclerView repräsentieren.
        binding!!.rvJobs.adapter = jobsForYouAdapter


        // Setzt einen OnClickListener auf den Button "saveandnextbtn".
        binding!!.saveandnextbtn.setOnClickListener {

            // Konvertiert das Set der ausgewählten Jobs in ein Array,
            // da die Navigationssafe Args nur Arrays und keine Sets akzeptieren.
            val selectedJobsArray = selectedJobs.toTypedArray()

            // Hole den Arbeitsort aus den übergebenen Argumenten oder verwende einen leeren String, falls kein Arbeitsort vorhanden ist.
            val arbeitsort = arguments?.getString("wo") ?: ""

            // Erstellt eine Navigationsaktion, die das Array der ausgewählten Jobs und den Arbeitsort an das nächste Fragment übermittelt.
            val action =
                JobsForYouFragmentDirections.actionJobsForYouFragmentToJobOportunitiesFragment(
                    selectedJobsArray,
                    arbeitsort
                )
            // Navigiert zum nächsten Fragment, indem die zuvor erstellte Aktion verwendet wird.
            findNavController().navigate(action)

        }

        // Hole das Berufsfeld und den Arbeitsort aus den übergebenen Fragmentargumenten.
        // Verwendet einen leeren String als Fallback.
        val berufsfeld = arguments?.getString("berufsfeld") ?: ""
        val arbeitsort = arguments?.getString("wo") ?: ""

        // Übersetzt das Berufsfeld aus den Argumenten ins Deutsche.
        viewModel.translateToGerman(berufsfeld) { translatedOccupationalField ->
            // Übersetzt den Arbeitsort aus den Argumenten ins Deutsche.
            viewModel.translateToGerman(arbeitsort) { translatedWorkLocation ->
                // Ruft Jobdaten ab, indem die übersetzten Werte von Berufsfeld und Arbeitsort verwendet werden.
                viewModel.fetchJobNomination(
                    berufsfeld = translatedOccupationalField,
                    arbeitsort = translatedWorkLocation
                )
            }
        }

        // Beobachtet Änderungen an den Jobtiteln im MainViewModel. Wenn Jobtitel verfügbar sind, werden diese verarbeitet.
        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            // Überprüft, ob die Jobdaten erfolgreich abgerufen wurden.
            if (jobs != null) {
                // Übersetzt die Jobtitel mithilfe der im ViewModel definierten Funktion translateJobTitles.
                viewModel.translateJobTitles(jobs) { translatedJobs ->
                    // Aktualisiert die Liste im Adapter mit den ggf. übersetzten Jobtiteln.
                    jobsForYouAdapter.submitList(translatedJobs)
                }
            } else {
                Toast.makeText(context, R.string.unkownjobserror, Toast.LENGTH_SHORT).show()
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