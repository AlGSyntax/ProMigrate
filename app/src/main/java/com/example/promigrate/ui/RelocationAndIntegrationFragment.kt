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
import com.example.promigrate.adapter.RelocationToDoListAdapter
import com.example.promigrate.data.model.ToDoItemRelocation
import com.example.promigrate.databinding.FragmentRelocationAndIntegrationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

/**
 * Ein Fragment, das die Aufgabenliste für die Umsiedlung anzeigt und Funktionen zur Bearbeitung dieser Liste bereitstellt.
 * Es ermöglicht dem Benutzer, Aufgaben hinzuzufügen, zu bearbeiten und zu löschen.
 */
class RelocationAndIntegrationFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentRelocationAndIntegrationBinding? = null

    // Initialisiert den Adapter für die Aufgabenliste.
    private lateinit var toDoListAdapter: RelocationToDoListAdapter

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
        binding = FragmentRelocationAndIntegrationBinding.inflate(inflater, container, false)
        // Initialisiert den Adapter für die Aufgabenliste.
        initToDoListAdapter()
        // Gibt die Wurzelansicht des Fragments zurück.
        return binding!!.root
    }

    /**
     * Initialisiert den Adapter für die Aufgabenliste.
     * Der Adapter ist verantwortlich für die Bereitstellung der Ansichten (View-Objekte),
     * die die Daten des RecyclerView repräsentieren.
     *
     * Es holt sich die aktuelle Benutzer-ID von Firebase und erstellt eine neue Instanz von RelocationToDoListAdapter.
     * Diese Instanz wird dann dem RecyclerView als Adapter zugewiesen.
     *
     * Die Funktionen zum Bearbeiten und Löschen von Aufgaben werden als Lambda-Funktionen an den Adapter übergeben.
     * Diese Funktionen werden aufgerufen, wenn der Benutzer auf die entsprechenden Schaltflächen in der Aufgabenliste klickt.
     */
    private fun initToDoListAdapter() {
        // Holt den aktuellen Benutzer von Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        // Holt die Benutzer-ID oder gibt einen leeren String zurück, wenn der Benutzer null ist
        val userId = currentUser?.uid ?: ""
        // Erstellt eine neue Instanz von RelocationToDoListAdapter und übergibt die Funktionen zum Bearbeiten und Löschen von Aufgaben
        toDoListAdapter = RelocationToDoListAdapter(
            onItemEdit = { toDoItem -> editToDoItem(userId, toDoItem) },
            onItemDelete = { toDoItem -> viewModel.deleteToDoItem(userId, toDoItem.id) }
        )

        // Setzt den LayoutManager für das RecyclerView
        binding!!.rvtodoreloc.layoutManager = LinearLayoutManager(context)
        // Setzt den Adapter für das RecyclerView
        binding!!.rvtodoreloc.adapter = toDoListAdapter
    }

    /**
     * Diese Funktion bearbeitet ein Aufgabenelement in der Liste.
     * Sie öffnet einen Dialog, in dem der Benutzer den Text der Aufgabe bearbeiten kann.
     * Nachdem der Benutzer seine Änderungen vorgenommen hat und auf "Speichern" klickt, wird die Aufgabe in der Datenbank aktualisiert.
     *
     * @param userId Die ID des aktuellen Benutzers. Wird verwendet, um die richtige Aufgabe in der Datenbank zu finden und zu aktualisieren.
     * @param toDoItem Das Aufgabenelement, das bearbeitet werden soll. Enthält die aktuellen Daten der Aufgabe.
     */
    private fun editToDoItem(userId: String, toDoItem: ToDoItemRelocation) {
        // Erstellt ein neues EditText-Element, das in den Dialog eingefügt wird
        val editText = EditText(context)
        // Erstellt einen neuen Dialog mit dem MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            // Setzt den Titel des Dialogs
            .setTitle(getString(R.string.edit_todo_hint))
            // Fügt das EditText-Element in den Dialog ein
            .setView(editText)
            // Fügt eine positive Schaltfläche hinzu, die die Aufgabe aktualisiert, wenn sie gedrückt wird
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // Holt die ID der Aufgabe aus dem übergebenen Aufgabenelement
                val todoId = toDoItem.id
                // Aktualisiert den Text der Aufgabe in der Datenbank mit dem Text, den der Benutzer eingegeben hat
                viewModel.updateToDoTextRelocation(userId, todoId, editText.text.toString())
            }
            // Fügt eine negative Schaltfläche hinzu, die den Dialog schließt, wenn sie gedrückt wird
            .setNegativeButton(getString(R.string.cancel), null)
            // Zeigt den Dialog an
            .show()
    }

    /**
     * Diese Funktion fügt ein neues Aufgabenelement zur Liste hinzu.
     * Sie öffnet einen Dialog, in dem der Benutzer den Text der neuen Aufgabe eingeben kann.
     * Nachdem der Benutzer den Text eingegeben und auf "Speichern" geklickt hat, wird die neue Aufgabe in der Datenbank erstellt.
     *
     * @param userId Die ID des aktuellen Benutzers. Wird verwendet, um die Aufgabe dem richtigen Benutzer zuzuordnen.
     */
    private fun addToDoItem(userId: String) {
        // Erstellt ein neues EditText-Element, das in den Dialog eingefügt wird
        val editText = EditText(context)
        // Erstellt einen neuen Dialog mit dem MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            // Setzt den Titel des Dialogs
            .setTitle(getString(R.string.add_todo_hint))
            // Fügt das EditText-Element in den Dialog ein
            .setView(editText)
            // Fügt eine positive Schaltfläche hinzu, die die neue Aufgabe erstellt, wenn sie gedrückt wird
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // Holt den Text, den der Benutzer eingegeben hat
                val newText = editText.text.toString()
                // Erstellt eine neue ID für die Aufgabe
                val newToDoId = UUID.randomUUID().toString()
                // Setzt den Status der Aufgabe auf "nicht abgeschlossen"
                val isCompleted = false // or true, depending on your logic
                // Erstellt die neue Aufgabe in der Datenbank
                viewModel.updateToDoItem(userId, newToDoId, isCompleted, newText)
            }
            // Fügt eine negative Schaltfläche hinzu, die den Dialog schließt, wenn sie gedrückt wird
            .setNegativeButton(getString(R.string.cancel), null)
            // Zeigt den Dialog an
            .show()
    }


    /**
     * Wird aufgerufen, nachdem die Ansicht des Fragments und seine hierarchische Struktur instanziiert wurden.
     * In dieser Methode werden weitere UI-Initialisierungen vorgenommen und Listener für UI-Elemente eingerichtet.
     *
     * Es holt sich die aktuelle Benutzer-ID von Firebase und beobachtet die Aufgabenliste dieses Benutzers.
     * Wenn sich die Aufgabenliste ändert, wird die Liste im Adapter aktualisiert.
     *
     * Es setzt auch OnClickListener für die Schaltflächen "Aufgabe hinzufügen", "Integrationskurs finden" und "Zurück".
     * Wenn der Benutzer auf diese Schaltflächen klickt, werden entsprechende Aktionen ausgeführt.
     *
     * @param view: Die erstellte Ansicht des Fragments.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Holt den aktuellen Benutzer von Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        // Holt die Benutzer-ID oder gibt einen leeren String zurück, wenn der Benutzer null ist
        val userId = currentUser?.uid ?: ""

        // Beobachtet die Aufgabenliste des Benutzers und aktualisiert die Liste im Adapter, wenn sich die Aufgabenliste ändert
        viewModel.getToDoItems(userId).observe(viewLifecycleOwner) { toDoItems ->
            toDoListAdapter.submitList(toDoItems)
        }

        // Setzt einen OnClickListener für die Schaltfläche "Aufgabe hinzufügen"
        // Wenn der Benutzer auf diese Schaltfläche klickt, wird eine neue Aufgabe hinzugefügt
        binding!!.addtodoButton.setOnClickListener {
            addToDoItem(userId)
        }

        // Setzt einen OnClickListener für die Schaltfläche "Integrationskurs finden"
        // Wenn der Benutzer auf diese Schaltfläche klickt, wird er zum Integrationskurs-Fragment navigiert
        binding!!.findIntegrationCourseButton.setOnClickListener {
            findNavController().navigate(RelocationAndIntegrationFragmentDirections.actionRelocationAndIntegrationFragmentToIntegrationCourseFragment())
        }

        // Setzt einen OnClickListener für die Schaltfläche "Zurück"
        // Wenn der Benutzer auf diese Schaltfläche klickt, wird er zum Dashboard-Fragment navigiert
        binding!!.backButton.setOnClickListener {
            val action = RelocationAndIntegrationFragmentDirections.actionRelocationAndIntegrationFragmentToDashboardFragment()
            findNavController().navigate(action)
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