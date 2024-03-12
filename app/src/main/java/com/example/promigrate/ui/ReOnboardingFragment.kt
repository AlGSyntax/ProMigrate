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
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentReOnboardingBinding

/**
 * ReOnboardingFragment ist ein Fragment, das den Re-Onboarding-Prozess für den Benutzer verwaltet.
 * Es beobachtet die Arbeitsorte und Jobs im ViewModel und aktualisiert die AutoCompleteTextViews entsprechend.
 * Es ermöglicht dem Benutzer auch, den ausgewählten Arbeitsort und Beruf zu speichern und zum nächsten Schritt zu navigieren, indem er auf den "submitButton" klickt.
 *
 * Es verwendet ein Binding-Objekt, um auf die im XML definierten Ansichten zuzugreifen.
 */
class ReOnboardingFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentReOnboardingBinding? = null

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
        binding = FragmentReOnboardingBinding.inflate(inflater, container, false)

        /**
         * Beobachtet die Arbeitsorte im ViewModel und aktualisiert die AutoCompleteTextView entsprechend.
         * Wenn die Arbeitsorte nicht null sind, wird ein ArrayAdapter erstellt und an die AutoCompleteTextView gebunden.
         * Der ArrayAdapter wird dann benachrichtigt, dass sich die Daten geändert haben.
         */
        viewModel.worklocations.observe(viewLifecycleOwner) { arbeitsorte ->
            if (arbeitsorte != null) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    arbeitsorte
                )
                binding!!.arbeitsortEditText.setAdapter(adapter)

                // Benachrichtigt den ArrayAdapter, dass sich die zugrunde liegenden Daten geändert haben.
                // Dies führt dazu, dass der ArrayAdapter die Ansicht aktualisiert.
                adapter.notifyDataSetChanged()
            }
        }

        /**
         * Beobachtet die Jobs im ViewModel und aktualisiert die AutoCompleteTextView entsprechend.
         * Wenn die Jobs nicht leer sind, werden die Jobtitel übersetzt und ein ArrayAdapter mit den übersetzten Jobs erstellt und an die AutoCompleteTextView gebunden.
         * Wenn die Jobs leer sind, wird eine Toast-Nachricht angezeigt, die den Benutzer darüber informiert, dass keine Jobs gefunden wurden.
         */
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
                Toast.makeText(context, R.string.notfoundjobs, Toast.LENGTH_SHORT).show()
            }
        }

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
         * Setzt einen OnItemClickListener für das arbeitsortEditText.
         * Wenn ein Element ausgewählt wird, wird der ausgewählte Arbeitsort übersetzt und das Berufsfeld des Benutzers abgerufen.
         * Wenn das Berufsfeld nicht leer ist, wird es ebenfalls übersetzt und die JobNomination-Methode im ViewModel aufgerufen.
         * Wenn das Berufsfeld leer ist, wird eine Toast-Nachricht angezeigt, die den Benutzer darüber informiert, dass kein Berufsfeld gefunden wurde.
         */
        binding!!.arbeitsortEditText.setOnItemClickListener { adapterView, _, position, _ ->
            val selectedArbeitsort = adapterView.getItemAtPosition(position) as String
            viewModel.translateToGerman(selectedArbeitsort) { translatedArbeitsort ->
                val berufsfeld = viewModel.userProfileData.value?.fieldOfWork ?: ""
                if (berufsfeld.isNotBlank()) {
                    viewModel.translateToGerman(berufsfeld) { translatedBerufsfeld ->
                        viewModel.fetchJobNomination(translatedBerufsfeld, translatedArbeitsort)
                    }
                } else {
                    Toast.makeText(context, R.string.notfoundoccu, Toast.LENGTH_SHORT).show()
                }
            }
        }

        /**
         * Setzt einen OnClickListener für den submitButton.
         * Wenn der Button geklickt wird, werden der Arbeitsort und der Beruf aus den EditText-Feldern abgerufen.
         * Wenn beide Felder ausgefüllt sind, werden der Arbeitsort und der Beruf übersetzt und die entsprechenden Methoden im ViewModel aufgerufen, um die Daten zu speichern und zum nächsten Schritt zu navigieren.
         * Wenn eines der Felder leer ist, wird eine Toast-Nachricht angezeigt, die den Benutzer auffordert, alle Felder auszufüllen.
         */
        binding!!.submitButton.setOnClickListener {
            val arbeitsort = binding!!.arbeitsortEditText.text.toString()
            val beruf = binding!!.berufEditText.text.toString()

            if (arbeitsort.isNotBlank() && beruf.isNotBlank()) {
                viewModel.translateToGerman(arbeitsort) { translatedArbeitsort ->
                    viewModel.savedesiredLocationToFirebase(translatedArbeitsort)
                    viewModel.translateToGerman(beruf) { translatedBeruf ->
                        // Verwende die übersetzten Daten, um das ViewModel zu aktualisieren und zum nächsten Schritt zu navigieren.
                        (parentFragment as? ViewPagerFragment)?.let {
                            it.viewModel.updateJobOffers(translatedBeruf, translatedArbeitsort)
                            it.moveToNextPage()
                        }
                    }
                }
            } else {
                Toast.makeText(context, R.string.fillfields, Toast.LENGTH_SHORT).show()
            }
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