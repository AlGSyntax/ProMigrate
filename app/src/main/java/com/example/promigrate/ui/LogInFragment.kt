package com.example.promigrate.ui


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class LogInFragment : Fragment() {

    private  val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentLogInBinding
    private lateinit var googleSignInClient: GoogleSignInClient





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLogInBinding.inflate(inflater, container, false)
        configureGoogleSignIn()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        binding.loginBTN.setOnClickListener {
            viewModel.doesEmailExist(binding.emailET.text.toString())
            // Prüfe die Eingaben vor dem Login
            if (isValidInput()) {
                viewModel.login(binding.emailET.text.toString(), binding.passwordET.text.toString())
            }
        }

        binding.loginButton.setOnClickListener {
            signIn()
        }

        binding.resetPasswordTV.setOnClickListener {
            val email = binding.emailET.text.toString().trim()

            fun showSnackbar(message: Int) {
                val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
                snackbar.view.setBackgroundResource(R.drawable.snackbar_custom_background)
                snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
                snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.titles))
                snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.elevation))
                snackbar.setAction(R.string.close) {
                    snackbar.dismiss()
                }
                snackbar.show()
            }

            if (email.isEmpty()) {
                showSnackbar(R.string.pleaseputemailin)
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        val message = if (task.isSuccessful) R.string.resetpasswordsuccess else R.string.resetpasswordfail
                        showSnackbar(message)
                    }
            }
        }





        binding.toregister.setOnClickListener {
            // Hier führst du die Navigation zum RegisterFragment durch
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // User-Navigation

        viewModel.loginStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                if (isOnboardingComplete()) {
                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                } else {
                    findNavController().navigate(R.id.action_loginFragment_to_createYourProfileFragment)
                }
            }
        }

        viewModel.emailExists.observe(viewLifecycleOwner) { exists ->
            if (!exists) {
                binding.emailInputLayout.error = getString(R.string.emailnotexistserror)
            }
        }


        viewModel.userProfileData.observe(viewLifecycleOwner) { userProfile ->
            if (userProfile != null) {
                // Wenn userProfileData geladen ist, extrahiere die Jobtitel und Hash-IDs
                val jobTitlesArray = userProfile.selectedJobs?.keys?.toList()?.toTypedArray() ?: arrayOf()
                val refNrsArray = userProfile.selectedJobs?.values?.toList()?.toTypedArray() ?: arrayOf()
                val arbeitsort = userProfile.desiredLocation ?: ""

                val action = LogInFragmentDirections.actionLoginFragmentToDashboardFragment(
                    selectedJobTitles = jobTitlesArray,
                    selectedJobRefNrs = refNrsArray,
                    arbeitsort = arbeitsort
                )
                findNavController().navigate(action)
            }
        }

        binding.exitapp.setOnClickListener {
            activity?.finish()
        }


    }

    private fun isValidInput(): Boolean {
        val email = binding.emailET.text.toString()
        val password = binding.passwordET.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = getString(R.string.invalid_email_error)
            return false
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = getString(R.string.invalid_password_error_login)
            return false
        }

        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null

        return true
    }



    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }


    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            viewModel.onGoogleLoginClicked(account.idToken!!)
        } catch (e: ApiException) {
            // Der Google Sign-In ist fehlgeschlagen, handle den Fehler
            Toast.makeText(context, getString(R.string.googlesignupfailed, e.statusCode), Toast.LENGTH_LONG).show()
        }
    }

    private fun isOnboardingComplete(): Boolean {
        val sharedPref = activity?.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        return sharedPref?.getBoolean("OnboardingComplete", false) ?: false
    }


}
