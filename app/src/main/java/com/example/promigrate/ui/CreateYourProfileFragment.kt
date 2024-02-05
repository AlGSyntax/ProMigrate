package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.promigrate.MainViewModel
import com.example.promigrate.R
import com.example.promigrate.databinding.FragmentCreateYourProfileBinding

class CreateYourProfileFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentCreateYourProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialisierung des View Bindings
        binding = FragmentCreateYourProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Implementierung der Logik für das Erstellen des Profils
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val age = binding.etAge.text.toString()
            val work = binding.etWork.text.toString()
            val isDataProtected = binding.cbDataProtection.isChecked

            // Überprüfe die Eingaben
            if (validateInput(name, age, work, isDataProtected)) {
                // Speichere die Daten und navigiere weiter
                saveProfileData(name, age, work)
                findNavController().navigate(R.id.jobsForYouFragment)
            } else {
                // Zeige eine Fehlermeldung, wenn die Validierung fehlschlägt
                Toast.makeText(context, "Bitte überprüfe deine Eingaben", Toast.LENGTH_SHORT).show()
            }
        }

        // Weitere Logik, z.B. zum Laden von Daten oder für weitere Interaktionen
    }

    private fun validateInput(name: String, age: String, work: String, isDataProtected: Boolean): Boolean {
        // Implementiere Validierungslogik
        return name.isNotEmpty() && age.isNotEmpty() && work.isNotEmpty() && isDataProtected
    }

    private fun saveProfileData(name: String, age: String, work: String) {
        // Speichere die Profildaten
        // Diese Methode könnte beispielsweise Daten in ViewModel speichern oder eine Datenbankabfrage ausführen
    }
}
