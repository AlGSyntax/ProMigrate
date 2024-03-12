package com.example.promigrate.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentFaqBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * FAQFragment ist ein Fragment, das eine Liste von häufig gestellten Fragen (FAQs) und deren Antworten anzeigt.
 * Es bietet dem Benutzer auch die Möglichkeit, zur Einstellungsseite zurückzukehren, zum Anfang der Seite zu blättern,
 * zu einer bestimmten Antwort zu blättern, wenn eine Frage angeklickt wird, und sein Konto zu löschen.
 */
class FAQFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentFaqBinding? = null

    /**
     * Wird aufgerufen, um die Ansicht des Fragments zu erstellen.
     * Es erstellt das Binding-Objekt und gibt die Wurzelansicht zurück.
     *
     * @param inflater: Der LayoutInflater, der zum Aufblasen der Ansichten verwendet wird.
     * @param container: Die übergeordnete Ansicht, in die das Fragment eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte Ansicht des Fragments.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFaqBinding.inflate(inflater, container, false)
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

        // Setzt einen OnClickListener auf den backButton. Wenn er angeklickt wird, navigiert er zum SettingsFragment.
        binding!!.backButton.setOnClickListener {
            findNavController().navigate(FAQFragmentDirections.actionFAQFragmentToSettingsFragment())
        }

        // Setzt einen OnClickListener auf den scrollToTopFromRightArrow. Wenn er angeklickt wird, scrollt er an den oberen Rand der scrollView.
        binding!!.scrollToTopFromRightArrow.setOnClickListener {
            binding!!.scrollView.scrollTo(0, 0)
        }

        // Listen von Fragen und Antworten
        val questions = listOf(
            binding!!.question1, binding!!.question2, binding!!.question3, binding!!.question4,
            binding!!.question5, binding!!.question6, binding!!.question7
        )

        val answers = listOf(
            binding!!.answer1, binding!!.answer2, binding!!.answer4,
            binding!!.answer5, binding!!.answer6, binding!!.answer7
        )

        // Setzt einen OnClickListener auf jede Frage. Wenn eine Frage angeklickt wird, blättert sie zur entsprechenden Antwort.
        questions.zip(answers).forEach { (question, answer) ->
            question.setOnClickListener {
                binding!!.scrollView.scrollTo(0, answer.y.toInt())
            }
        }

        // Setzt einen OnClickListener auf die Frage8. Wenn sie angeklickt wird, wird ein Dialog zur Bestätigung der Kontolöschung angezeigt.
        binding!!.question8.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
                .setTitle(getString(R.string.delete_account))
                .setMessage(getString(R.string.delete_account_confirmation))
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                    viewModel.deleteAccount()
                    dialog.dismiss()
                }
                .show()
        }

        // Legt den Text und die Bewegungsmethode von answer3 fest.
        binding!!.answer3.apply {
            text = getString(R.string.answer3)
            movementMethod = LinkMovementMethod.getInstance()
        }

        // Beobachtet die deleteAccountStatus LiveData im ViewModel. Wenn die Kontolöschung erfolgreich ist, navigiert es zum LanguageSelectionFragment.
        // Schlägt die Kontolöschung fehl, wird eine Toast-Meldung angezeigt.
        viewModel.deleteAccountStatus.observe(viewLifecycleOwner) { isSuccess ->
            isSuccess?.let {
                if (it) {
                    findNavController().navigate(R.id.action_FAQFragment_to_languageSelectionFragment)
                } else {
                    Toast.makeText(context, R.string.account_deletion_failed, Toast.LENGTH_SHORT)
                        .show()
                }
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