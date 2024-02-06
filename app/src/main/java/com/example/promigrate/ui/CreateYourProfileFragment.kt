package com.example.promigrate.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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

        setupImagePicker()
        setupSaveButton()
    }

    private fun setupImagePicker() {
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                // Zeige das ausgewählte Bild in der ImageView an
                binding.ivProfilePicture.setImageURI(uri)
            }
        }

        binding.ivProfilePicture.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val age = binding.etAge.text.toString()
            val work = binding.etWork.text.toString()
            val isDataProtected = binding.cbDataProtection.isChecked

            if (validateInput(name, age, work, isDataProtected) && selectedImageUri != null) {
                viewModel.saveProfileWithImage(selectedImageUri!!, name, age, work, isDataProtected)
                findNavController().navigate(R.id.action_createYourProfileFragment_to_jobsForYouFragment)
            } else {
                // Zeige eine Fehlermeldung an
            }
        }
    }

    private fun validateInput(name: String, age: String, work: String, isDataProtected: Boolean): Boolean {
        // Implementiere Validierungslogik
        return name.isNotEmpty() && age.isNotEmpty() && work.isNotEmpty()
    }
}

