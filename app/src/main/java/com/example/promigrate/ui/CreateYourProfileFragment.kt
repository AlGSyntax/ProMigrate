package com.example.promigrate.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
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



class CreateYourProfileFragment : Fragment() {

    private val TAG = "CreateYourProfileFragment"


    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentCreateYourProfileBinding? = null

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
        return binding!!.root
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
                            .into(binding!!.ivProfilePicture)
                    }
                    Log.d(TAG, "Selected URI: $uri")
                } else {
                    Log.d(TAG, "No media selected")
                }
            }




        binding!!.ivProfilePicture.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
                    binding!!.autoCompleteTextView.setAdapter(adapter)
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
                    binding!!.autoCompleteTextView2.setAdapter(adapter)
                }
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Arbeitsorte.")
            }
        }
        viewModel.fetchArbeitsorte()


        binding!!.btnAddContact.setOnClickListener {
            val dialogBinding = AdditionalContactInfoBinding.inflate(LayoutInflater.from(it.context))
            val dialogView = dialogBinding.root
            val streetEditText = dialogBinding.etStreet
            val birthPlaceEditText = dialogBinding.etBirthPlace
            val maidenNameEditText = dialogBinding.etMaidenName
            val firstNameEditText = dialogBinding.etFirstName
            val lastNameEditText = dialogBinding.etLastName
            val phoneNumEditText = dialogBinding.etPhoneNumber

            MaterialAlertDialogBuilder(it.context, R.style.CustomAlertDialog)
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

        binding!!.btnSave.setOnClickListener {
            val name = binding!!.etName.text.toString()
            val age = binding!!.etAge.text.toString()
            val fieldOfWork = binding!!.autoCompleteTextView.text.toString()
            val isDataProtected = binding!!.cbDataProtection.isChecked
            // Erfasse den Wert des Sprachniveaus und den gewünschten Ort
            val languageLevel = binding!!.languageLevelSlider.value.toString()
            val desiredLocation = binding!!.autoCompleteTextView2.text.toString()

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
                Log.d(TAG, fieldOfWork)
                // Navigiere zum nächsten Fragment
                val action = CreateYourProfileFragmentDirections
                    .actionCreateYourProfileFragmentToJobsForYouFragment(desiredLocation, fieldOfWork)
                findNavController().navigate(action)

            } else {
                // Zeige eine Fehlermeldung an, die aus der Validierung zurückgegeben wurde
                Toast.makeText(context, validationResponse.second, Toast.LENGTH_LONG).show()
            }
        }




//TODO: Material outline für Buttons

        binding!!.languageLevelSlider.addOnChangeListener { _, value, _ ->
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
            binding!!.languageLevelText.text = getString(languageLevel)
        }


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
            return Pair(false, requireContext().getString(R.string.errornameempt))
        }
        if (age.isEmpty()) {
            return Pair(false, requireContext().getString(R.string.errornageampth))
        } else {
            age.toIntOrNull()?.let {
                if (it < 0) return Pair(false, requireContext().getString(R.string.erroragepos))
            } ?: return Pair(false, requireContext().getString(R.string.erroragevalid))
        }

        if (!isDataProtected) {
            return Pair(false, requireContext().getString(R.string.errordataprotection))
        }

        if (selectedImageUri == null) {
            return Pair(false, requireContext().getString(R.string.errorprofilepic))
        }

        if (additionalFirstName.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorError))
            return Pair(false, requireContext().getString(R.string.emptyfirstname))
        }

        if (additionalLastName.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorError))
            return Pair(false, requireContext().getString(R.string.emptylastname))
        }

        if (additionalPhoneNum.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorError))
            return Pair(false, requireContext().getString(R.string.emptyphonenumber))
        }


        if (additionalStreet.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorError))
            return Pair(false, requireContext().getString(R.string.errorvoidaddress))
        }

        if (additionalBirthPlace.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorError))
            return Pair(false, requireContext().getString(R.string.emptybirthplace))
        }

        if (additionalMaidenName.isNullOrEmpty()) {
            binding!!.btnAddContact.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorError))
            return Pair(false, requireContext().getString(R.string.emptymaidenname))
        }



        // Wenn alle Prüfungen bestanden sind
        binding!!.btnAddContact.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background))
        return Pair(true, "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }




}

