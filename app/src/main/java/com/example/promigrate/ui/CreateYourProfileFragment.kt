package com.example.promigrate.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            val work = binding.etWork.text.toString()
            val isDataProtected = binding.cbDataProtection.isChecked
            val languageLevel = binding.languageLevelSlider.value.toInt() // Erfasse den Wert des Sliders
            val desiredLocation = binding.etDesiredLocation.text.toString()


            if (validateInput(name, age, work, isDataProtected) && selectedImageUri != null) {
                viewModel.saveProfileWithImage(selectedImageUri!!, name, age, work, isDataProtected, languageLevel,desiredLocation)
                findNavController().navigate(R.id.action_createYourProfileFragment_to_jobsForYouFragment)
            } else {
                // Zeige eine Fehlermeldung an
            }
        }

        binding.languageLevelSlider.addOnChangeListener { _, value, _ ->
            // Aktualisiere die TextView mit dem ausgewählten Sprachniveau
            // Angenommen, du hast eine TextView mit der ID tvLanguageLevel
            val languageLevel = when (value.toInt()) {
                1 -> "A1: Anfänger"
                2 -> "A2: Grundlegende Kenntnisse"
                3 -> "B1: Fortgeschrittene Sprachverwendung"
                4 -> "B2: Selbstständige Sprachverwendung"
                5 -> "C1: Fachkundige Sprachkenntnisse"
                6 -> "C2: Annähernd muttersprachliche Kenntnisse"
                else -> "Nicht definiert"
            }
            binding.languageLevelText.text = languageLevel
        }
    }


    private fun validateInput(name: String, age: String, work: String, isDataProtected: Boolean): Boolean {
        // Implementiere Validierungslogik
        return name.isNotEmpty() && age.isNotEmpty() && work.isNotEmpty() && isDataProtected
    }
}

