package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar


class RegisterFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private  var binding: FragmentRegisterBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.registrationStatus.observe(viewLifecycleOwner) { registrationStatus ->
            registrationStatus?.let { status ->
                if (status.success) {
                    // Navigiere zum CreateYourProfileFragment
                    findNavController().navigate(R.id.createYourProfileFragment)
                } else {
                    // Holt den String basierend auf der Ressourcen-ID
                    val message = getString(status.message as Int) // Konvertiert die message in eine Ressourcen-ID und holt den String
                    val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
                    val snackbarLayout = snackbar.view
                    snackbarLayout.setBackgroundResource(R.drawable.snackbar_custom_background)
                    // Anpassen der Snackbar (optional)
                    snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
                    snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.titles))
                    snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.elevation))
                    snackbar.setAction(R.string.close) {
                        // Die Aktion dient dazu, die Snackbar zu schließen.
                    }
                    snackbar.show()
                }
            }
        }





        viewModel.selectedLanguageCode.observe(viewLifecycleOwner) { selectedLanguageCode ->
            binding!!.registerBTN.setOnClickListener {
                val email = binding!!.emailEditText.text.toString()
                val password = binding!!.passwordEditText.text.toString()
                val confirmPassword = binding!!.confirmPasswordEditText.text.toString()

                // Überprüfung der Eingaben und Registrierung mit dem ausgewählten Sprachcode
                if (isValidInput(email, password, confirmPassword)) {
                    viewModel.register(email, password, confirmPassword, selectedLanguageCode)
                }
            }
        }
    }


    private fun isValidInput(email: String, password: String, confirmPassword: String): Boolean {
        // Reset errors
        binding!!.emailTextInputLayout.error = null
        binding!!.passwordTextInputLayout.error = null
        binding!!.confirmPasswordTextInputLayout.error = null

        return when {
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding!!.emailTextInputLayout.error = getString(R.string.invalid_email_error)
                false
            }
            password.isEmpty() || password.length < 8 -> {
                binding!!.passwordTextInputLayout.error = getString(R.string.invalid_password_error)
                false
            }
            confirmPassword != password -> {
                binding!!.confirmPasswordTextInputLayout.error = getString(R.string.password_mismatch_error)
                false
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }





}
