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

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentProfileImage()

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Glide.with(this).load(uri).circleCrop().into(binding.profileImage)
                viewModel.updateProfileImage(uri)
            } else {
                Log.d(TAG, "No image selected")
            }
        }

        binding.profileImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        viewModel.userProfileData.observe(viewLifecycleOwner) { userProfile ->
            userProfile?.let {
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .circleCrop()
                    .into(binding.profileImage)

                val userName = it.name ?: getString(R.string.unknownuser)
                binding.userGreeting.text = getString(R.string.hellouser, userName)
            }
        }



        binding.gotofeedbackbtn.setOnClickListener {
            val feedbackBinding = FeedbackDialogLayoutBinding.inflate(layoutInflater)

            MaterialAlertDialogBuilder(it.context)
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
                    Toast.makeText(context, "Feedback wird gesendet", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }





        binding.backtodashbtn3.setOnClickListener{
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDashboardFragment())
        }


        binding.logoutbtn.setOnClickListener{
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
                            .into(binding.profileImage)
                    } else {
                        R.drawable.elevationtodolistrelocation// passendes palceholder suchen
                    }
                }
            }.addOnFailureListener {
                Log.e(TAG, "Fehler beim Laden des Profilbildes", it)
            }
        }
    }
}