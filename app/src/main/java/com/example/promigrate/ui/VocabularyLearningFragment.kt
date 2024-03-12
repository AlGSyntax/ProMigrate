package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.adapter.VocabularyLearningAdapter
import com.example.promigrate.data.model.FlashCard
import com.example.promigrate.databinding.DialogAddCardBinding
import com.example.promigrate.databinding.DialogEditCardBinding
import com.example.promigrate.databinding.FragmentVocabularyLearningBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth


/**
 * VocabularyLearningFragment ist eine Unterklasse von Fragment.
 * Dieses Fragment ist verantwortlich für die Darstellung und Interaktion mit der Vokabellernfunktion der Anwendung.
 * Es stellt eine Liste von Indexkarten dar, die der Benutzer zum Lernen verwenden kann.
 * Der Benutzer kann neue Indexkarten hinzufügen und bestehende Indexkarten bearbeiten.
 * Die Daten für die Indexkarten werden von einem ViewModel bereitgestellt.
 */
class VocabularyLearningFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentVocabularyLearningBinding? = null

    // Initialisiert den VocabularyLearningAdapter, der die Daten für die RecyclerView bereitstellt.
    private lateinit var adapter: VocabularyLearningAdapter

    // Lazy Initialisierung der userId, die beim ersten Zugriff die UID des aktuellen Benutzers abruft.
    // Wird kein Benutzer gefunden, wird ein leerer String zurückgegeben.
    private val userId: String by lazy {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid ?: ""
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialisiert das Binding für das Fragment, um auf die UI-Elemente
        binding = FragmentVocabularyLearningBinding.inflate(inflater, container, false)
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

        // Erstellt eine Instanz von VocabularyLearningAdapter.
        // Wenn ein Benutzer die Bearbeitung einer Indexkarte anfordert, wird editIndexCard aufgerufen.
        adapter = VocabularyLearningAdapter { indexCard ->
            // Hier wird die Methode aufgerufen, die die Bearbeitungslogik für die Indexkarte beinhaltet.
            editFlashCard(indexCard)
        }

        // Setzt den LayoutManager für das RecyclerView, der bestimmt, wie die Elemente angeordnet werden.
        // Hier wird ein LinearLayoutManager verwendet, der die Elemente in einer vertikalen Liste anordnet.
        binding!!.rvLanguageCourses.layoutManager = LinearLayoutManager(context)

        // Weist dem zuvor erstellten jobsAdapter die RecyclerView zu.
        // Dieser Adapter ist verantwortlich für die Bereitstellung der Ansichten
        // (View-Objekte), die die Daten des RecyclerView repräsentieren.
        binding!!.rvLanguageCourses.adapter = adapter


        // Beobachte die LiveData-Liste von Flashcards aus dem ViewModel.
        // Jedes Mal, wenn die Daten sich ändern, wird der folgende Code ausgeführt.
        viewModel.getFlashcards(userId).observe(viewLifecycleOwner) { flashcards ->
            // Übergebe die aktualisierte Liste von Flashcards an den Adapter.
            adapter.submitList(flashcards)
        }

        // Setzt einen OnClickListener für den addFlashcardButton.
        // Wenn der Button geklickt wird, wird die Methode addNewIndexCard aufgerufen.
        binding!!.addFlashcardButton.setOnClickListener {
            // Ruft die Methode auf, die die Logik zum Hinzufügen einer neuen Indexkarte beinhaltet.
            addNewFlashCard()
        }


        // Listener für den "Find Language Course"-Button. Bei Klick navigiert der Benutzer zum LanguageCourseFragment.
        binding!!.findLanguageCourseButton.setOnClickListener {
            // Die Navigation erfolgt durch Verwendung der generierten Directions-Klasse, die sicherstellt, dass die korrekten Argumente übergeben werden.
            findNavController().navigate(VocabularyLearningFragmentDirections.actionVocabularyLearningFragmentToLanguageCourseFragment())
        }

        // Listener für den "Back"-Button. Bei Klick navigiert der Benutzer zurück zum DashboardFragment.
        binding!!.backButton.setOnClickListener {
            // Auch hier wird die Navigation durch Verwendung der generierten Directions-Klasse durchgeführt, was Typsicherheit und Klarheit gewährleistet.
            findNavController().navigate(VocabularyLearningFragmentDirections.actionVocabularyLearningFragmentToDashboardFragment())
        }

    }

    /**
     * Diese Funktion wird verwendet, um eine neue Flashcard zum Vokabellernsystem hinzuzufügen.
     * Sie erstellt einen Dialog mit zwei Textfeldern, in die der Benutzer den Vorder- und Rücktext der Indexkarte eingeben kann.
     * Nachdem der Benutzer auf die Schaltfläche "Speichern" geklickt hat, ruft die Funktion die Eingabe aus den Textfeldern ab und ruft die addFlashcard-Methode des ViewModels auf.
     * Wenn der Benutzer auf die Schaltfläche "Abbrechen" klickt, wird der Dialog abgebrochen.
     */
    private fun addNewFlashCard() {
        // Aufblasen des Dialoglayouts
        val dialogBinding = DialogAddCardBinding.inflate(layoutInflater)
        // Referenzen auf die Vorder- und Rückseite der EditText-Felder
        val frontEditText = dialogBinding.frontEditText
        val backEditText = dialogBinding.backEditText

        // Erstellen eines neuen MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            // Setzen der Ansicht des Dialogs auf das aufgeblasene Layout
            .setView(dialogBinding.root.apply { parent?.let { (it as ViewGroup).removeView(this) } })
            // Setzen des Titels des Dialogs
            .setTitle(R.string.addflashcard)
            // Setzen der positiven (Speichern) Schaltfläche und ihres Klick-Listeners
            .setPositiveButton(R.string.save) { dialog, _ ->
                // Abrufen der Eingabe aus den Vorder- und Rückseite der EditText-Felder
                val frontText = frontEditText.text.toString()
                val backText = backEditText.text.toString()
                // Aufrufen der addFlashcard-Methode des ViewModels mit dem eingegebenen Vorder- und Rücktext
                viewModel.addFlashcard(userId, frontText, backText)
                // Dialog schließen
                dialog.dismiss()
            }
            // Setzen der negativen (Abbrechen) Schaltfläche und ihres Klick-Listeners
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            // Anzeigen des Dialogs
            .show()
    }


    /**
     * Diese Funktion wird verwendet, um eine bestehende Flashcard im Vokabellernsystem zu bearbeiten.
     * Sie erstellt einen Dialog mit einem Textfeld, in das der Benutzer den neuen Text der Indexkarte eingeben kann.
     * Der Text, der bearbeitet wird, hängt davon ab, ob die Karte umgedreht ist oder nicht.
     * Nachdem der Benutzer auf die Schaltfläche "Speichern" geklickt hat, ruft die Funktion die Eingabe aus dem Textfeld ab und ruft die updateFlashcard-Methode des ViewModels auf.
     * Wenn der Benutzer auf die Schaltfläche "Abbrechen" klickt, wird der Dialog abgebrochen.
     *
     * @param flashCard: Die Indexkarte, die bearbeitet werden soll.
     */
    private fun editFlashCard(flashCard: FlashCard) {
        // Aufblasen des Dialoglayouts
        val binding = DialogEditCardBinding.inflate(LayoutInflater.from(context))

        // Entscheidet, welcher Text bearbeitet wird, basierend darauf, ob die Karte umgedreht ist oder nicht
        val editText = if (flashCard.isFlipped) {
            // Wenn die Karte umgedreht ist, wird der Rücktext bearbeitet
            binding.backEditText.apply {
                setText(flashCard.backText)
            }
        } else {
            // Wenn die Karte nicht umgedreht ist, wird der Vordertext bearbeitet
            binding.frontEditText.apply {
                setText(flashCard.frontText)
            }
        }

        // Erstellen eines neuen MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            // Setzen der Ansicht des Dialogs auf das aufgeblasene Layout
            .setTitle(R.string.editflashcard)
            .setView(binding.root)
            // Setzen der positiven (Speichern) Schaltfläche und ihres Klick-Listeners
            .setPositiveButton(R.string.save) { dialog, _ ->
                // Abrufen der Eingabe aus dem EditText-Feld
                val newText = editText.text.toString()
                // Entscheidet, welcher Text aktualisiert wird, basierend darauf, ob die Karte umgedreht ist oder nicht
                if (flashCard.isFlipped) {
                    // Wenn die Karte umgedreht ist, wird der Rücktext aktualisiert
                    viewModel.updateFlashcard(userId, flashCard.id!!, flashCard.frontText, newText)
                } else {
                    // Wenn die Karte nicht umgedreht ist, wird der Vordertext aktualisiert
                    viewModel.updateFlashcard(userId, flashCard.id!!, newText, flashCard.backText)
                }
                // Dialog schließen
                dialog.dismiss()
            }
            // Setzen der negativen (Abbrechen) Schaltfläche und ihres Klick-Listeners
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            // Anzeigen des Dialogs
            .show()
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
