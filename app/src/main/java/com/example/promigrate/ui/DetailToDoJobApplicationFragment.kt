package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.DetailToDoJobApplicationAdapter
import com.example.promigrate.data.model.JobWithToDoItems
import com.example.promigrate.data.model.ToDoItem
import com.example.promigrate.databinding.FragmentDetailToDoJobApplicationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID


/**
 * Ein Fragment zur Darstellung und Bearbeitung der ToDo-Liste für Jobbewerbungen.
 * Dieses Fragment ermöglicht den Benutzern, ihre Aufgaben zu verwalten, die sie während des
 * Bewerbungsprozesses für verschiedene Jobs erledigen müssen.
 */
class DetailToDoJobApplicationFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentDetailToDoJobApplicationBinding? = null

    // Adapter-Instanz zur Verwaltung und Darstellung der Job-Anwendungs-To-Do-Liste im RecyclerView.
    // DetailToDoJobApplicationAdapter nimmt eine Callback-Funktion entgegen, die definiert,
    // wie auf Benutzerinteraktionen mit Listenelementen reagiert werden soll.
    private lateinit var adapter: DetailToDoJobApplicationAdapter

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
        // Initialisiert das Binding für das Fragment, um auf die UI-Elemente zugreifen zu können.
        binding = FragmentDetailToDoJobApplicationBinding.inflate(inflater, container, false)
        // Initialisiert den Adapter für den RecyclerView, der die To-Do-Liste darstellt.
        initAdapter()
        // Gibt die Wurzelansicht des Fragments zurück.
        return binding!!.root
    }


    private fun initAdapter() {
        // Ermittelt die aktuelle Benutzer-ID aus Firebase Auth, um nutzerspezifische Daten zu verwalten.
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        // Initialisiert den DetailToDoJobApplicationAdapter mit den erforderlichen Callback-Funktionen.
        // Diese Callbacks definieren das Verhalten für das Hinzufügen, Bearbeiten und Löschen von To-Do-Elementen.
        adapter = DetailToDoJobApplicationAdapter(
            { jobId ->
                addToDoItem(
                    userId,
                    jobId
                )
            },// Callback für das Hinzufügen eines To-Do-Elements.
            { jobId, todoId, currentText ->
                editToDoItem(
                    userId,
                    jobId,
                    todoId,
                    currentText
                )
            },// Callback für das Bearbeiten eines To-Do-Elements.
            { jobId, _ -> deleteJobItemItem(jobId) }// Callback für das Löschen eines Job-Elements.
        )
    }

    /**
     * Zeigt einen Dialog zur Bestätigung der Löschung eines Job-Elements an und führt die Löschung durch,
     * falls der Benutzer zustimmt.
     *
     * @param jobId: Die eindeutige ID des Job-Elements, das gelöscht werden soll.
     */
    private fun deleteJobItemItem(jobId: String) {
        // Erstellt und zeigt einen MaterialAlertDialogBuilder an, um die Löschung zu bestätigen.
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setMessage(getString(R.string.confirmdeletion))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                // Wenn der Benutzer auf "Löschen" klickt, wird die Methode `deleteJobSelection` im ViewModel aufgerufen,
                // um das entsprechende Job-Element basierend auf seiner ID zu löschen.
                viewModel.deleteJobSelection(jobId)
            }
            .setNegativeButton(
                getString(R.string.cancel),
                null
            )// Wenn der Benutzer auf "Abbrechen" klickt, wird der Dialog geschlossen.
            .show()// Zeigt den Dialog an.
    }


    /**
     * Zeigt einen Dialog zum Bearbeiten eines To-Do-Elements an und aktualisiert das Element bei Bestätigung.
     *
     * @param userId: Die Benutzer-ID, der das To-Do-Element zugeordnet ist.
     * @param jobId: Die Job-ID, der das To-Do-Element zugeordnet ist.
     * @param todoId: Die ID des zu bearbeitenden To-Do-Elements.
     * @param currentText: Der aktuelle Text des To-Do-Elements, der bearbeitet werden soll.
     */
    private fun editToDoItem(userId: String, jobId: String, todoId: String, currentText: String) {
        // Erstellt ein EditText-Element für die Eingabe des neuen Texts.
        val editText = EditText(context).apply { setText(currentText) }

        // Erstellt einen Dialog zum Bearbeiten des Texts und speichert die Änderungen bei Bestätigung.
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.edit_todo_hint))
            .setView(editText)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // Der Benutzer bestätigt die Bearbeitung, speichere die Änderung durch einen Aufruf im ViewModel.
                viewModel.updateToDoText(userId, jobId, todoId, editText.text.toString())
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Zeigt einen Dialog zum Hinzufügen eines neuen To-Do-Elements für einen spezifischen Jobeintrag.
     *
     * @param userId: Die Benutzer-ID, der das neue To-Do-Element zugeordnet wird.
     * @param jobId: Die Job-ID, zu der das neue To-Do-Element hinzugefügt werden soll.
     */
    private fun addToDoItem(userId: String, jobId: String) {
        // Erstellt ein EditText-Element für die Eingabe des neuen To-Do-Texts.
        val editText = EditText(context)
        // Erstellt einen Dialog zum Hinzufügen des neuen To-Do-Elements und speichert die Eingabe bei Bestätigung.
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.add_todo_hint))
            .setView(editText)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newText = editText.text.toString()
                val newToDoId = generateNewToDoId()
                // Der Benutzer bestätigt die Eingabe und speichert das neue To-Do-Element durch einen Aufruf im ViewModel.
                viewModel.updateToDoItemForJob(userId, jobId, newToDoId, false, newText)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
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
        binding!!.rvJobs.adapter = adapter

        // Erhält die aktuelle Benutzerinstanz und deren eindeutige Benutzer-ID.
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: ""

        // Beobachtet userProfileData für Änderungen. userProfileData enthält Informationen über die vom Benutzer ausgewählten Jobs.
        viewModel.userProfileData.observe(viewLifecycleOwner) { profile ->
            // Extrahiert die Jobtitel aus den Benutzerprofilinformationen.
            val jobTitles = profile?.selectedJobs?.keys?.toList() ?: emptyList()

            // Initialisiert eine Map, um die ToDoItems für jeden Job zu speichern
            val jobWithToDoItemsMap = mutableMapOf<String, List<ToDoItem>>()

            // Iteriert über jeden Jobtitel, um die zugehörigen To-Do-Elemente abzurufen.
            jobTitles.forEach { refNr ->
                viewModel.getToDoItemsForJob(userId, refNr)
                    .observe(viewLifecycleOwner) { toDoItems ->
                        // Aktualisiert die Map mit den abgerufenen To-Do-Elementen für den spezifischen Job.
                        jobWithToDoItemsMap[refNr] = toDoItems

                        // Erstellt eine Liste von JobWithToDoItems basierend auf der aktualisierten Map.
                        val jobWithToDoItemsList = jobWithToDoItemsMap.map { entry ->
                            JobWithToDoItems(entry.key, entry.value)
                        }

                        // Übergebe die Liste von JobWithToDoItems an den Adapter, um sie in der UI darzustellen.
                        adapter.submitList(jobWithToDoItemsList)
                    }
            }

        }

        // Setzt einen OnClickListener auf den Button restartOnboardingButton.
        binding!!.restartOnboardingButton.setOnClickListener {
            // Verwendet den NavController, um zur viewPagerFragment-Ansicht zu navigieren, sobald der Button gedrückt wird.
            findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_viewPagerFragment)
        }

        // Setzt einen OnClickListener auf den Button backtodashbtn.
        binding!!.backtodashbtn.setOnClickListener {
            // Überprüft, ob das aktuelle Fragment im NavController das DetailToDoJobApplicationFragment ist.
            if (findNavController().currentDestination?.id == R.id.detailToDoJobApplicationFragment) {
                // Wenn ja, navigiert es zum DashboardFragment.
                findNavController().navigate(R.id.action_detailToDoJobApplicationFragment_to_dashboardFragment)
            }
        }
    }

    /**
     * Generiert eine eindeutige Identifikationsnummer für ein ToDo-Element.
     *
     * @return: Eine als String formatierte einzigartige UUID.
     */
    private fun generateNewToDoId(): String {
        // Generiert eine zufällige UUID und konvertiert sie in einen String.
        return UUID.randomUUID().toString()
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