package com.example.promigrate.ui

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentCreateYourProfileBinding

const val TAG = "CreateYourProfileFragment"

class CreateYourProfileFragment : Fragment() {

    private lateinit var binding: FragmentCreateYourProfileBinding
    private val viewModel: MainViewModel by activityViewModels()

    private var selectedImageUri: Uri? = null
    private var additionalStreet: String? = null
    private var additionalBirthPlace: String? = null
    private var additionalMaidenName: String? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateYourProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                context?.let { context ->
                    Glide.with(context)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.ivProfilePicture)
                }
                Log.d(TAG, "Selected URI: $uri")
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        binding.ivProfilePicture.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val age = binding.etAge.text.toString()
            val fieldOfWork = binding.autoCompleteTextView.text.toString()
            val isDataProtected = binding.cbDataProtection.isChecked
            // Erfasse den Wert des Sprachniveaus und den gewünschten Ort
            val languageLevel = binding.languageLevelSlider.value.toInt()
            val desiredLocation = binding.etDesiredLocation.text.toString()

            // Prüfe, ob alle notwendigen Informationen vorhanden sind
            if (validateInput(name, age, fieldOfWork, isDataProtected) && selectedImageUri != null) {
                viewModel.saveProfileWithImage(
                    uri = selectedImageUri!!, name = name, age = age, fieldOfWork = fieldOfWork,
                    isDataProtected = isDataProtected, languageLevel = languageLevel,
                    desiredLocation = desiredLocation, street = additionalStreet.toString(),
                    birthplace = additionalBirthPlace.toString(), maidenname = additionalMaidenName.toString()
                )
                // Navigiere zum nächsten Fragment
                findNavController().navigate(R.id.action_createYourProfileFragment_to_jobsForYouFragment)
            } else {
                // Zeige eine Fehlermeldung an
            }
        }

        binding.btnAddContact.setOnClickListener {
            val dialogView = LayoutInflater.from(it.context).inflate(R.layout.additional_contact_info, null)
            val streetEditText = dialogView.findViewById<EditText>(R.id.etStreet)
            val birthPlaceEditText = dialogView.findViewById<EditText>(R.id.etBirthPlace)
            val maidenNameEditText = dialogView.findViewById<EditText>(R.id.etMaidenName)

            AlertDialog.Builder(it.context)
                .setView(dialogView)
                .setPositiveButton("Speichern") { _, _ ->
                    additionalStreet = streetEditText.text.toString()
                    additionalBirthPlace = birthPlaceEditText.text.toString()
                    additionalMaidenName = maidenNameEditText.text.toString()
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }



//TODO: Material outline für Buttons

        binding.languageLevelSlider.addOnChangeListener { _, value, _ ->
            // Aktualisiere die TextView mit dem ausgewählten Sprachniveau
            // Angenommen, du hast eine TextView mit der ID tvLanguageLevel
            val languageLevel = when (value.toInt()) {
                1 -> R.string.beginner
                2 -> R.string.basic_knowledge
                3 -> R.string.intermediate
                4 -> R.string.independent
                5 -> R.string.proficient
                6 -> R.string.near_native
                else -> R.string.undefined
            }
            binding.languageLevelText.text = getString(languageLevel)
        }

        viewModel.berufsfelder.observe(viewLifecycleOwner) { berufsfelder ->
            if (berufsfelder != null) {
                Log.d(TAG, "Berufsfelder erfolgreich abgerufen und Adapter gesetzt.")
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, berufsfelder)
                binding.autoCompleteTextView.setAdapter(adapter)
            }else{
                Log.e(TAG, "Fehler beim Abrufen der Berufsfelder.")
            }

        }
        viewModel.fetchBerufsfelder()

    }


    private fun validateInput(name: String, age: String, work: String, isDataProtected: Boolean): Boolean {
        // Implementiere Validierungslogik
        return name.isNotEmpty() && age.isNotEmpty() && work.isNotEmpty() && isDataProtected
    }


}

