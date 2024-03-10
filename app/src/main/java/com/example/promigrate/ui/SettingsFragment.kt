package com.example.promigrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FeedbackDialogLayoutBinding
import com.example.promigrate.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private val TAG = "SettingsFragment"


    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentSettingsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentProfileImage()

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Glide.with(this).load(uri).circleCrop().into(binding!!.profileImage)
                viewModel.updateProfileImage(uri)
            } else {
                Log.d(TAG, "No image selected")
            }
        }

        binding!!.profileImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        viewModel.userProfileData.observe(viewLifecycleOwner) { userProfile ->
            userProfile?.let {
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .circleCrop()
                    .into(binding!!.profileImage)

                val userName = it.name ?: getString(R.string.unknownuser)
                binding!!.userGreeting.text = getString(R.string.hellouser, userName)
            }
            userProfile?.languageLevel?.let { languageLevel ->
                // Setze den Slider auf den Wert, der dem Sprachniveau des Benutzers entspricht
                val sliderValue = when (languageLevel) {
                    getString(R.string.beginner) -> 1f
                    getString(R.string.basic_knowledge) -> 2f
                    getString(R.string.intermediate) -> 3f
                    getString(R.string.independent) -> 4f
                    getString(R.string.proficient) -> 5f
                    getString(R.string.near_native) -> 6f
                    else -> 0f // oder ein anderer Default-Wert
                }
                binding!!.languageLevelSlider.value = sliderValue
                binding!!.languageLevelText.text = languageLevel
            }
        }

        binding!!.gotofaqbtn.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToFAQFragment())
        }



        binding!!.gotofeedbackbtn.setOnClickListener {
            val feedbackBinding = FeedbackDialogLayoutBinding.inflate(layoutInflater)

            MaterialAlertDialogBuilder(it.context,R.style.CustomAlertDialog)
                .setTitle(R.string.feedback)
                .setView(feedbackBinding.root)
                .setPositiveButton(R.string.sendfeedback) { _, _ ->
                    val designFeedback = feedbackBinding.ratingBarDesign.rating
                    val functionalityFeedback = feedbackBinding.ratingBarFunctionality.rating
                    val generalOpinion = feedbackBinding.editTextGeneralOpinion.text.toString()

                    // Calculate the overall rating as the average of design and functionality ratings
                    val overallRating = (designFeedback + functionalityFeedback) / 2

                    // Die UserID sollte dynamisch aus dem Benutzerkontext abgerufen werden
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val userId = currentUser?.uid ?: ""

                    viewModel.saveFeedback(
                        userId = userId,
                        designRating = designFeedback,
                        functionalityRating = functionalityFeedback,
                        overallRating = overallRating,
                        generalFeedback = generalOpinion
                    )
                    Toast.makeText(context, R.string.feedbacksent, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }


        binding!!.languageLevelSlider.addOnChangeListener { _, value, _ ->

            viewModel.saveLanguageLevelToFirebase(value.toString())
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






        binding!!.backtodashbtn3.setOnClickListener{
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDashboardFragment())
        }


        binding!!.logoutbtn.setOnClickListener{
            viewModel.logout()
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLoginFragment())
        }


    }

    private fun loadCurrentProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("user").document(userId).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val imageUrl = document.getString("profilePicture")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(binding!!.profileImage)
                    } else {
                        R.drawable.elevationtodolistrelocation// passendes palceholder suchen
                    }
                }
            }.addOnFailureListener {
                Log.e(TAG, "Fehler beim Laden des Profilbildes", it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}