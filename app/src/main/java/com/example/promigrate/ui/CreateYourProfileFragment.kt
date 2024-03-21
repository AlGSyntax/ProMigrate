package com.example.promigrate.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.AdditionalContactInfoBinding
import com.example.promigrate.databinding.FragmentCreateYourProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


/**
 * Fragment zur Erstellung des Benutzerprofils.
 * Erlaubt Benutzern die Eingabe ihrer Daten, wie z.B. Name, Alter, Berufsfeld, Datenschutzoptionen,
 * und ermöglicht die Auswahl eines Profilbildes und zusätzlicher Kontaktdaten.
 */
class CreateYourProfileFragment : Fragment() {


    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentCreateYourProfileBinding? = null

    // Hier werden die Variablen deklariert, die für die Erfassung der Benutzerdaten verwendet werden.
    private var selectedImageUri: Uri? = null        // URI des ausgewählten Profilbildes
    private var additionalStreet: String? = null     // Zusätzliche Adresse
    private var additionalBirthPlace: String? = null // Geburtsort
    private var additionalMaidenName: String? = null // Mädchenname
    private var additionalFirstName: String? = null  // Vorname
    private var additionalLastName: String? = null   // Nachname
    private var additionalPhoneNum: String? = null   // Telefonnummer


    /**
     * Initialisiert die Benutzeroberfläche des Fragments, indem das entsprechende Layout aufgeblasen wird.
     *
     * @param inflater: Das LayoutInflater-Objekt, das zum Aufblasen des Layouts des Fragments verwendet wird.
     * @param container: Die übergeordnete ViewGroup, in den die neue Ansicht eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte View für das Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
        Bundle?
    ): View {
        // Initialisiert das Binding für das Fragment, um auf die UI-Elemente zugreifen zu können.
        binding = FragmentCreateYourProfileBinding.inflate(inflater, container, false)

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

        // Ein ActivityResultLauncher, um Medien mit der PickVisualMedia API auszuwählen.
        // Das Ergebnis (URI) des ausgewählten Bildes wird verarbeitet.
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    // Wenn ein Bild ausgewählt wurde, speichert es die URI und lädt das Bild in die ImageView.
                    selectedImageUri = uri
                    context?.let { context ->
                        Glide.with(context)
                            .load(uri)
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding!!.ivProfilePicture)
                    }

                } else {
                    // Wenn keine URI vorhanden ist, wird ein Fehler angezeigt.
                    Toast.makeText(context, R.string.error_image, Toast.LENGTH_LONG).show()
                }
            }


        // Setzt einen OnClickListener auf die ImageView. Wenn darauf geklickt wird, startet der Media Picker.
        binding!!.ivProfilePicture.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        /**
         * Beobachtet die LiveData `berufsfelder` aus dem ViewModel. Sobald die Arbeitsorte erfolgreich abgerufen sind,
         * werden sie durch die `translateArbeitsorte` Methode übersetzt. Ein ArrayAdapter wird dann mit den übersetzten
         * Arbeitsorten initialisiert und dem autoCompleteTextView zugewiesen.
         */
        viewModel.occupationalfields.observe(viewLifecycleOwner) { occupationalfields ->
            if (occupationalfields != null) {
                // Wenn Berufsfelder erfolgreich geladen wurden, übersetzt es sie und setzt sie im AutoCompleteTextView.
                viewModel.translateOccupationalFields(occupationalfields) { translatedOccupationalFields ->
                    // Setzt den Adapter nach der Übersetzung
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        translatedOccupationalFields
                    )
                    binding!!.autoCompleteTextView.setAdapter(adapter)
                }
            } else {
                Toast.makeText(context, R.string.error_occupational_fields, Toast.LENGTH_LONG)
                    .show()
            }
        }
        // Startet den Abrufprozess der Berufsfelder aus dem ViewModel.
        viewModel.fetchOccupationalFields()

        /**
         * Beobachtet die LiveData `arbeitsorte` aus dem ViewModel. Sobald die Arbeitsorte erfolgreich abgerufen sind,
         * werden sie durch die `translateArbeitsorte` Methode übersetzt. Ein ArrayAdapter wird dann mit den übersetzten
         * Arbeitsorten initialisiert und dem autoCompleteTextView2 zugewiesen.
         */
        viewModel.worklocations.observe(viewLifecycleOwner) { worklocations ->
            if (worklocations != null) {
                // Wenn Arbeitsorte erfolgreich geladen wurden, übersetzt es sie und setzt sie im AutoCompleteTextView2.
                viewModel.translateWorkLocations(worklocations) { translatedWorkLocations ->
                    // Setzt den Adapter nach der Übersetzung
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        translatedWorkLocations
                    )
                    binding!!.autoCompleteTextView2.setAdapter(adapter)
                }
            } else {
                Toast.makeText(context, R.string.error_work_locations, Toast.LENGTH_LONG).show()
            }
        }
        // Startet den Abrufprozess der Arbeitsorte im ViewModel.
        viewModel.fetchWorkLocations()


        /**
         * Setzt einen OnClickListener für den btnAddContact-Button. Beim Klicken wird ein Dialog geöffnet,
         * der es dem Benutzer ermöglicht, zusätzliche Kontaktinformationen einzugeben. Diese Informationen
         * umfassen Straße, Geburtsort, Mädchenname, Vorname, Nachname und Telefonnummer.
         * Nach Eingabe kann der Benutzer die Informationen speichern oder den Vorgang abbrechen.
         */
        binding!!.btnAddContact.setOnClickListener {
            // Initialisiert das Dialog-Binding und die Views.
            val dialogBinding =
                AdditionalContactInfoBinding.inflate(LayoutInflater.from(it.context))
            val dialogView = dialogBinding.root

            // Referenzen auf die EditText-Elemente im Dialog.
            val streetEditText = dialogBinding.etStreet
            val birthPlaceEditText = dialogBinding.etBirthPlace
            val maidenNameEditText = dialogBinding.etMaidenName
            val firstNameEditText = dialogBinding.etFirstName
            val lastNameEditText = dialogBinding.etLastName
            val phoneNumEditText = dialogBinding.etPhoneNumber

            // Erstellt und zeigt den Dialog.
            MaterialAlertDialogBuilder(it.context, R.style.CustomAlertDialog)
                .setView(dialogView)
                .setTitle(R.string.additional_contact_info)
                .setPositiveButton(R.string.save) { _, _ ->
                    // Speichert die eingegebenen Daten in Variablen, wenn auf "Speichern" geklickt wird.
                    additionalStreet = streetEditText.text.toString()
                    additionalBirthPlace = birthPlaceEditText.text.toString()
                    additionalMaidenName = maidenNameEditText.text.toString()
                    additionalFirstName = firstNameEditText.text.toString()
                    additionalLastName = lastNameEditText.text.toString()
                    additionalPhoneNum = phoneNumEditText.text.toString()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }


        /**
         * Setzt einen OnClickListener für den btnSave-Button. Beim Klicken werden die vom Benutzer
         * eingegebenen Profildaten gespeichert, vorausgesetzt, alle Validierungen sind erfolgreich.
         * Dazu gehören Benutzername, Alter, Arbeitsfeld, Datenschutzbestätigung, Sprachniveau und
         * Wunschort. Zusätzlich werden die in einem vorherigen Dialog eingegebenen zusätzlichen
         * Kontaktinformationen gespeichert. Bei erfolgreicher Validierung und Speicherung navigiert
         * der Benutzer zum nächsten Fragment. Bei Fehlern wird eine entsprechende Meldung angezeigt.
         */
        binding!!.btnSave.setOnClickListener {
            // Erfasst die Benutzereingaben aus den verschiedenen Feldern.
            val name = binding!!.etName.text.toString()
            val age = binding!!.etAge.text.toString()
            val fieldOfWork = binding!!.autoCompleteTextView.text.toString()
            val isDataProtected = binding!!.cbDataProtection.isChecked
            // Erfasse den Wert des Sprachniveaus und den gewünschten Ort
            val languageLevel = binding!!.languageLevelSlider.value.toString()
            val desiredLocation = binding!!.autoCompleteTextView2.text.toString()

            // Validiert die Eingaben und zeigt Fehlermeldungen an, falls notwendig.
            val validationResponse = validateInput(
                name = name,
                age = age,
                isDataProtected = isDataProtected,
                selectedImageUri = selectedImageUri,
                additionalStreet = additionalStreet,
                additionalBirthPlace = additionalBirthPlace,
                additionalMaidenName = additionalMaidenName,
                additionalFirstName = additionalFirstName,
                additionalLastName = additionalLastName,
                additionalPhoneNum = additionalPhoneNum
            )

            if (validationResponse.first) {
                // Speichert das Profil mit Bild und navigiert zum nächsten Fragment, wenn die Validierung erfolgreich ist.
                viewModel.saveProfileWithImage(
                    uri = selectedImageUri!!,
                    name = name,
                    age = age,
                    fieldOfWork = fieldOfWork,
                    isDataProtected = isDataProtected,
                    languageLevel = languageLevel,
                    desiredLocation = desiredLocation,
                    street = additionalStreet.toString(),
                    birthplace = additionalBirthPlace.toString(),
                    maidenname = additionalMaidenName.toString(),
                    firstname = additionalFirstName.toString(),
                    lastname = additionalLastName.toString(),
                    phonenumber = additionalPhoneNum.toString()

                )
                // Navigiert zum nächsten Fragment
                val action = CreateYourProfileFragmentDirections
                    .actionCreateYourProfileFragmentToJobsForYouFragment(
                        desiredLocation,
                        fieldOfWork
                    )
                findNavController().navigate(action)

            } else {
                // Zeige eine Fehlermeldung an, die aus der Validierung zurückgegeben wurde
                Toast.makeText(context, validationResponse.second, Toast.LENGTH_LONG).show()
            }
        }


        /**
         * Fügt dem languageLevelSlider einen ChangeListener hinzu. Wenn der Benutzer den Slider verschiebt,
         * wird das ausgewählte Sprachniveau in einer TextView angezeigt. Das Sprachniveau reicht von
         * Anfänger (1) bis fast muttersprachlich (6). Der Wert des Sliders bestimmt das Sprachniveau.
         */
        binding!!.languageLevelSlider.addOnChangeListener { _, value, _ ->
            // Bestimmt das Sprachniveau basierend auf dem Wert des Sliders.
            val languageLevel = when (value.toInt()) {
                1 -> R.string.beginner
                2 -> R.string.basic_knowledge
                3 -> R.string.intermediate
                4 -> R.string.independent
                5 -> R.string.proficient
                6 -> R.string.near_native
                else -> R.string.undefined
            }
            // Aktualisiert den Text der TextView, um das ausgewählte Sprachniveau anzuzeigen.
            binding!!.languageLevelText.text = getString(languageLevel)
        }


    }

    /**
     * Validiert die Eingaben des Benutzers im Profilerstellungsprozess.
     * Überprüft, ob alle erforderlichen Felder ausgefüllt sind und die Daten den Anforderungen entsprechen.
     *
     * @param name: Der Name des Benutzers.
     * @param age: Das Alter des Benutzers.
     * @param isDataProtected: Zustimmung des Benutzers zur Datenverarbeitung.
     * @param selectedImageUri: Die URI des ausgewählten Profilbildes.
     * @param additionalStreet: Die zusätzliche Straßeninformation.
     * @param additionalBirthPlace: Der Geburtsort.
     * @param additionalMaidenName: Der Mädchenname.
     * @param additionalFirstName: Der Vorname.
     * @param additionalLastName: Der Nachname.
     * @param additionalPhoneNum: Die Telefonnummer.
     * @return: Ein Paar, das einen Booleschen Wert und eine Nachricht enthält.
     *         Der Boolesche Wert zeigt an, ob die Validierung erfolgreich war.
     */
    private fun validateInput(
        name: String,
        age: String,
        isDataProtected: Boolean,
        selectedImageUri: Uri?,
        additionalStreet: String?,
        additionalBirthPlace: String?,
        additionalMaidenName: String?,
        additionalFirstName: String?,
        additionalLastName: String?,
        additionalPhoneNum: String?
    ): Pair<Boolean, String> {
        // Validierungen für verschiedene Felder mit entsprechenden Fehlermeldungen.
        if (name.isEmpty()) {
            return Pair(false, requireContext().getString(R.string.errornameempt))
        }
        if (age.isEmpty()) {
            return Pair(false, requireContext().getString(R.string.errornageampth))
        } else {
            // Überprüft, ob das Alter eine gültige Zahl und positiv ist.
            age.toIntOrNull()?.let {
                if (it < 0) return Pair(false, requireContext().getString(R.string.erroragepos))
            } ?: return Pair(false, requireContext().getString(R.string.erroragevalid))
        }

        // Überprüfung der Zustimmung zur Datenverarbeitung.
        if (!isDataProtected) {
            return Pair(false, requireContext().getString(R.string.errordataprotection))
        }

        // Überprüfung, ob ein Profilbild ausgewählt wurde.
        if (selectedImageUri == null) {
            return Pair(false, requireContext().getString(R.string.errorprofilepic))
        }

        // Überprüfungen für zusätzliche Kontaktinformationen
        if (additionalFirstName.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorError
                )
            )
            return Pair(false, requireContext().getString(R.string.emptyfirstname))
        }

        if (additionalLastName.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorError
                )
            )
            return Pair(false, requireContext().getString(R.string.emptylastname))
        }

        if (additionalPhoneNum.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorError
                )
            )
            return Pair(false, requireContext().getString(R.string.emptyphonenumber))
        }


        if (additionalStreet.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorError
                )
            )
            return Pair(false, requireContext().getString(R.string.errorvoidaddress))
        }

        if (additionalBirthPlace.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorError
                )
            )
            return Pair(false, requireContext().getString(R.string.emptybirthplace))
        }

        if (additionalMaidenName.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorError
                )
            )
            return Pair(false, requireContext().getString(R.string.emptymaidenname))
        }


        // Wenn alle Validierungen erfolgreich waren.
        binding!!.btnAddContact.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.background
            )
        )
        return Pair(true, "")
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

