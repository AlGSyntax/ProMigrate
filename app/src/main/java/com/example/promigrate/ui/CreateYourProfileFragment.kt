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
import android.widget.Toast
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
    private var additionalFirstName: String? = null
    private var additionalLastName: String? = null
    private var additionalPhoneNum: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
        Bundle?
    ): View {
        binding = FragmentCreateYourProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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
            val desiredLocation = binding.autoCompleteTextView2.text.toString()

            // Prüfe, ob alle notwendigen Informationen vorhanden sind
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
                // Navigiere zum nächsten Fragment
                findNavController().navigate(R.id.action_createYourProfileFragment_to_jobsForYouFragment)
            } else {
                // Zeige eine Fehlermeldung an, die aus der Validierung zurückgegeben wurde
                Toast.makeText(context, validationResponse.second, Toast.LENGTH_LONG).show()
            }
        }

        binding.btnAddContact.setOnClickListener {
            val dialogView =
                LayoutInflater.from(it.context).inflate(R.layout.additional_contact_info, null)
            val streetEditText = dialogView.findViewById<EditText>(R.id.etStreet)
            val birthPlaceEditText = dialogView.findViewById<EditText>(R.id.etBirthPlace)
            val maidenNameEditText = dialogView.findViewById<EditText>(R.id.etMaidenName)
            val firstNameEditText = dialogView.findViewById<EditText>(R.id.etFirstName)
            val lastNameEditText = dialogView.findViewById<EditText>(R.id.etLastName)
            val phoneNumEditText = dialogView.findViewById<EditText>(R.id.etPhoneNumber)

            AlertDialog.Builder(it.context,R.style.CustomAlertDialog)
                .setView(dialogView)
                .setTitle(R.string.additional_contact_info)
                .setPositiveButton(R.string.save) { _, _ ->
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
                Log.d(TAG, "Berufsfelder erfolgreich abgerufen.")
                viewModel.translateBerufsfelder(berufsfelder) { translatedBerufsfelder ->
                    // Setze den Adapter nach der Übersetzung
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        translatedBerufsfelder
                    )
                    binding.autoCompleteTextView.setAdapter(adapter)
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Berufsfelder.")
            }
        }
        viewModel.fetchBerufsfelder()


        viewModel.arbeitsorte.observe(viewLifecycleOwner) { arbeitsorte ->
            if (arbeitsorte != null) {
                Log.d(TAG, "Arbeitsorte erfolgreich abgerufen.")
                viewModel.translateArbeitsorte(arbeitsorte) { translatedArbeitsorte ->
                    // Setze den Adapter nach der Übersetzung
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        translatedArbeitsorte
                    )
                    binding.autoCompleteTextView2.setAdapter(adapter)
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Arbeitsorte.")
            }
        }
        viewModel.fetchArbeitsorte()
    }

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
        if (name.isEmpty()) {
            return Pair(false, R.string.errornameempt.toString())
        }

        if (age.isEmpty()) {
            return Pair(false, R.string.errornageampth.toString())
        } else {
            age.toIntOrNull()?.let {
                if (it < 0) return Pair(false, R.string.erroragepos.toString())
            } ?: return Pair(false, R.string.erroragevalid.toString())
        }

        if (!isDataProtected) {
            return Pair(false, R.string.errordataprotection.toString())
        }

        if (selectedImageUri == null) {
            return Pair(false, R.string.errorprofilepic.toString())
        }

        if (additionalStreet.isNullOrEmpty()) {
            return Pair(false, R.string.errorvoidaddress.toString())
        }

        if (additionalBirthPlace.isNullOrEmpty()) {
            return Pair(false, R.string.emptybirthplace.toString())
        }

        if (additionalMaidenName.isNullOrEmpty()) {
            return Pair(false, R.string.emptymaidenname.toString())
        }

        if (additionalFirstName.isNullOrEmpty()) {
            return Pair(false, R.string.emptyfirstname.toString())
        }

        if (additionalLastName.isNullOrEmpty()) {
            return Pair(false, R.string.emptylastname.toString())
        }

        if (additionalPhoneNum.isNullOrEmpty()) {
            return Pair(false, R.string.emptyphonenumber.toString())
        }

        // Wenn alle Prüfungen bestanden sind
        return Pair(true, R.string.allentavalid.toString())
    }




}

