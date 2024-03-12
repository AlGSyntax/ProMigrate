package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.adapter.DetailToDoJobResearchAdapter
import com.example.promigrate.databinding.FragmentDetailToDoJobResearchBinding

/**
 * DetailToDoJobResearchFragment zeigt eine Benutzeroberfläche, die detaillierte Informationen zu ausgewählten Jobs bereitstellt.
 * Der Benutzer kann spezifische Details zu jedem Job einsehen, die aus einer Liste von Jobs abgerufen werden,
 * die im vorherigen Schritt ausgewählt wurden. Das Fragment verwendet DetailToDoJobResearchAdapter,
 * um die Liste der Jobs und ihre Details darzustellen.
 * Es interagiert mit dem MainViewModel, um Jobdetails abzurufen und die Nutzerinteraktionen zu verarbeiten.
 */
class DetailToDoJobResearchFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentDetailToDoJobResearchBinding? = null

    // 'args' wird verwendet, um die Argumente zu holen, die an das DetailToDoJobResearchFragment übergeben wurden.
    private val args: DetailToDoJobResearchFragmentArgs by navArgs()


    /**
     * Initialisiert die Benutzeroberfläche des Fragments, indem das entsprechende Layout aufgeblasen wird.
     *
     * @param inflater: Das LayoutInflater-Objekt, das zum Aufblasen des Layouts des Fragments verwendet wird.
     * @param container: Die übergeordnete ViewGroup, in den die neue Ansicht eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte View für das Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialisiert das Binding für das Fragment, um auf die UI-Elemente
        binding = FragmentDetailToDoJobResearchBinding.inflate(inflater, container, false)
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

        /**
         * Instanziiert einen Adapter für die Darstellung von Jobforschungsdetails.
         * Der Adapter wird mit einem Lambda-Ausdruck initialisiert, der als Callback dient,
         * wenn ein Element in der Liste angeklickt wird. Dieser Callback ruft die entsprechende
         * Funktion im ViewModel auf, um die Details für den ausgewählten Job zu laden.
         */
        val adapter = DetailToDoJobResearchAdapter { refnr ->
            // Ruft die Funktion im ViewModel auf, um die Jobdetails basierend auf der Referenznummer zu holen.
            viewModel.fetchJobDetails(refnr)
        }

        // Setzt den LayoutManager für das RecyclerView, der bestimmt, wie die Elemente angeordnet werden.
        // Hier wird ein LinearLayoutManager verwendet, der die Elemente in einer vertikalen Liste anordnet.
        binding!!.rvJobs.layoutManager = LinearLayoutManager(context)

        // Weist dem zuvor erstellten jobsAdapter die RecyclerView zu.
        // Dieser Adapter ist verantwortlich für die Bereitstellung der Ansichten
        // (View-Objekte), die die Daten des RecyclerView repräsentieren.
        binding!!.rvJobs.adapter = adapter

        // Zuweisen der Jobtitel und Referenznummern aus den Argumenten des Fragments an den Adapter.
        // 'args.selectedJobTitles' enthält die Jobtitel und 'args.selectedJobRefNrs' die dazugehörigen Referenznummern.
        // Hier werden beide Arrays zusammengezippt und als Liste von Paaren an den Adapter übergeben.
        args.selectedJobRefNrs.let { refNrs ->
            adapter.submitList(args.selectedJobTitles.zip(refNrs))
        }

        // Beobachten der jobDetails LiveData im ViewModel, um auf Änderungen zu reagieren.
        // Bei erfolgreicher Abfrage werden die Jobdetails ggf. übersetzt und an den Adapter weitergeleitet, um sie anzuzeigen.
        viewModel.jobDetails.observe(viewLifecycleOwner) { result ->
            result.onSuccess { jobDetails ->
                // Übersetzen der erhaltenen Jobdetails und Aktualisieren der Anzeige im Adapter.
                viewModel.translateJobDetails(jobDetails) { translatedJobDetails ->
                    // Überprüfung, ob die Referenznummer vorhanden ist, und Weitergabe der Details an den Adapter.
                    translatedJobDetails.refnr?.let { refnr ->
                        adapter.setJobDetails(refnr, translatedJobDetails)
                    }
                }
            }.onFailure { exception ->
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Setzt einen OnClickListener für den Button, um bei Klick zum DashboardFragment zu navigieren.
        binding!!.backtodashbtn2.setOnClickListener {
            // Verwendet die Navigation-Komponente, um die Navigationsaktion auszuführen und zum DashboardFragment zu navigieren.
            findNavController().navigate(DetailToDoJobResearchFragmentDirections.actionDetailToDoJobResearchFragmentToDashboardFragment())
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

