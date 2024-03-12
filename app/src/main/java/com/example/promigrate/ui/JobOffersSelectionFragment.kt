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
import com.example.promigrate.adapter.JobOffersSelectionAdapter
import com.example.promigrate.databinding.FragmentJobOffersSelectionBinding

/**
 * JobOffersSelectionFragment ist ein Fragment, das eine Liste von Jobangeboten anzeigt.
 * Es ermöglicht dem Benutzer, mehrere Jobangebote auszuwählen und diese Auswahl zu speichern.
 * Es verwendet ein Binding-Objekt, um auf die im XML definierten Ansichten zuzugreifen.
 */
class JobOffersSelectionFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentJobOffersSelectionBinding? = null
    // Eine Map, die die ausgewählten Jobs speichert. Der Schlüssel ist der Jobtitel und der Wert ist die Referenznummer.
    private var selectedJobs = mutableMapOf<String, String>()

    /**
     * Wird aufgerufen, um die Ansicht des Fragments zu erstellen.
     * Es erstellt das Binding-Objekt und gibt die Wurzelansicht zurück.
     *
     * @param inflater: Der LayoutInflater, der zum Aufblasen der Ansichten verwendet wird.
     * @param container: Die übergeordnete Ansicht, in die das Fragment eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte Ansicht des Fragments.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentJobOffersSelectionBinding.inflate(inflater, container, false)
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
        // Setzt den LayoutManager für die RecyclerView.
        binding!!.rvJobs.layoutManager = LinearLayoutManager(context)

        // Erstellt einen neuen Adapter für die RecyclerView.
        val adapter = JobOffersSelectionAdapter { jobTitle, refNr, isChecked ->
            // Aktualisiert die Map der ausgewählten Jobs, wenn ein Item angekreuzt oder abgewählt wird.
            if (isChecked) {
                selectedJobs[jobTitle] = refNr
            } else {
                selectedJobs.remove(jobTitle)
            }
        }

        // Setzt den Adapter für die RecyclerView.
        binding!!.rvJobs.adapter = adapter

        // Beobachtet die Jobangebote im ViewModel und aktualisiert die RecyclerView entsprechend.
        viewModel.jobOffers.observe(viewLifecycleOwner) { jobOffers ->
            if (jobOffers != null) {
                viewModel.translateJobOffers(jobOffers) { translatedJobOffers ->
                    adapter.submitList(translatedJobOffers)
                }
            } else {
                Toast.makeText(context, R.string.nojobofffound, Toast.LENGTH_SHORT).show()
            }
        }

        // Setzt einen OnClickListener für den backtodashbtn.
        // Wenn der Button geklickt wird, werden die ausgewählten Jobs im ViewModel aktualisiert und der Benutzer wird zur vorherigen Seite navigiert.
        binding!!.backtodashbtn.setOnClickListener {
            viewModel.updateSelectedJobsAndPersist(selectedJobs)
            findNavController().navigateUp()
        }
    }

    /**
     * Wird aufgerufen, wenn die Ansicht des Fragments zerstört wird.
     * Es setzt das Binding-Objekt auf null, um Speicherlecks zu vermeiden.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}