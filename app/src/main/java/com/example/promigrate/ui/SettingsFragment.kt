package com.example.promigrate.ui

import android.os.Bundle
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

/**
 * SettingsFragment ist ein Fragment, das die Einstellungsseite der App darstellt.
 * Es ermöglicht dem Benutzer, sein Profilbild zu ändern, Feedback zu geben und sich abzumelden.
 * Es verwendet ein Binding-Objekt, um auf die im XML definierten Ansichten zuzugreifen.
 */
class SettingsFragment : Fragment() {


    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentSettingsBinding? = null

    /**
     * Wird aufgerufen, um die Ansicht des Fragments zu erstellen.
     * Es erstellt das Binding-Objekt und gibt die Wurzelansicht zurück.
     *
     * @param inflater: Der LayoutInflater, der zum Aufblasen der Ansichten verwendet wird.
     * @param container: Die übergeordnete Ansicht, in die das Fragment eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte Ansicht des Fragments.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    /**
     * Wird aufgerufen, nachdem die Ansicht des Fragments und seine hierarchische Struktur instanziiert wurden.
     * In dieser Methode werden weitere UI-Initialisierungen vorgenommen und Listener für UI-Elemente eingerichtet.
     *
     * @param view: Die erstellte Ansicht des Fragments.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aufruf der Methode loadCurrentProfileImage()
        loadCurrentProfileImage()

        /**
         * Dieser Codeblock registriert einen Callback für die Auswahl eines Bildes aus der Galerie.
         * Der Callback wird ausgelöst, wenn ein Bild ausgewählt wird, und die URI des Bildes wird als Parameter übergeben.
         *
         * Wenn die URI nicht null ist (was bedeutet, dass ein Bild ausgewählt wurde), wird das Bild mit der Glide-Bibliothek in das ImageView profileImage geladen.
         * Das Bild wird auch im ViewModel aktualisiert, was normalerweise die Änderung an die zugrunde liegende Datenquelle weiterleitet.
         *
         * Wenn die URI null ist (was bedeutet, dass kein Bild ausgewählt wurde), wird eine Toast-Nachricht angezeigt, um den Benutzer darüber zu informieren, dass keine Mediendatei ausgewählt wurde.
         */
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    Glide.with(this).load(uri).circleCrop().into(binding!!.profileImage)
                    viewModel.updateProfileImage(uri)
                } else {
                    Toast.makeText(context, "Keine Mediendatei ausgewählt", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        /**
         * Dieser Codeblock setzt einen OnClickListener auf die ImageView profileImage.
         * Wenn die ImageView angeklickt wird, startet sie den Startvorgang für das Ergebnis der pickMedia-Aktivität.
         * Der Launcher ist so konfiguriert, dass er nur Bilder aus den visuellen Medien (Galerie, etc.) auswählt.
         * Dies ermöglicht dem Benutzer, ein neues Profilbild aus den visuellen Medien seines Geräts auszuwählen.
         */
        binding!!.profileImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        /**
         * Dieser Codeblock beobachtet Änderungen im userProfileData LiveData-Objekt im ViewModel.
         * Wenn sich die userProfileData ändert, wird die Benutzeroberfläche entsprechend aktualisiert.
         *
         * Wenn das userProfile-Objekt nicht null ist, wird das Profilbild mit Hilfe der Glide-Bibliothek in die profileImage ImageView geladen.
         * Wenn die URL des Profilbildes null ist, lädt Glide kein Bild.
         * Der Name des Benutzers wird ebenfalls aus dem userProfile-Objekt abgerufen und in der userGreeting TextView angezeigt.
         * Wenn der Name des Benutzers null ist, wird eine Standardbegrüßung angezeigt.
         *
         * Die Eigenschaft languageLevel des userProfile-Objekts wird ebenfalls beachtet.
         * Wenn languageLevel nicht null ist, wird der Wert des languageLevelSliders so gesetzt, dass er dem Sprachniveau des Benutzers entspricht.
         * Das Sprachniveau wird auch in der languageLevelText TextView angezeigt.
         * Wenn languageLevel null ist, wird der Wert des Sliders auf einen Standardwert gesetzt, und die TextView zeigt einen Standardtext an.
         */
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

        /**
         * Dieser Codeblock setzt einen OnClickListener auf den gotofaqbtn Button.
         * Wenn der Button angeklickt wird, navigiert er zum FAQFragment unter Verwendung der findNavController() Methode und der actionSettingsFragmentToFAQFragment() Aktion.
         * Dies ermöglicht dem Benutzer, vom SettingsFragment zu FAQFragment zu navigieren.
         */
        binding!!.gotofaqbtn.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToFAQFragment())
        }


        /**
         * Dieser Codeblock setzt einen OnClickListener auf die Schaltfläche gotofeedbackbtn.
         * Wenn der Button angeklickt wird, wird ein Dialog-Layout für die Übermittlung von Feedback mit Hilfe der FeedbackDialogLayoutBinding erstellt.
         *
         * Ein MaterialAlertDialogBuilder wird verwendet, um den Dialog zu erstellen. Der Titel des Dialogs wird auf "Feedback" gesetzt.
         * Das aufgeblähte Layout wird als Ansicht für den Dialog festgelegt.
         *
         * Das Dialogfeld hat zwei Schaltflächen - eine positive und eine negative Schaltfläche.
         * Die positive Schaltfläche ist mit "Sendfeedback" beschriftet. Wenn diese Schaltfläche angeklickt wird, werden die Bewertungen für Design und Funktionalität abgerufen,
         * und die allgemeine Meinung aus dem Layout des Dialogs. Die Gesamtbewertung errechnet sich aus dem Durchschnitt der Bewertungen für Design und Funktionalität.
         *
         * Die ID des Benutzers wird von FirebaseAuth abgerufen. Wenn der Benutzer nicht angemeldet ist (d.h. die Benutzer-ID ist null), wird ein leerer String als Benutzer-ID verwendet.
         *
         * Das Feedback (einschließlich Benutzer-ID, Designbewertung, Funktionsbewertung, Gesamtbewertung und allgemeines Feedback) wird mit der saveFeedback-Methode des ViewModel gespeichert.
         * Eine Toast-Meldung wird angezeigt, um den Benutzer darüber zu informieren, dass das Feedback gesendet wurde.
         *
         * Die negative Schaltfläche ist mit 'Abbrechen' beschriftet. Sie hat keinen OnClickListener, so dass sie den Dialog einfach beendet, wenn sie angeklickt wird.
         *
         * Schließlich wird der Dialog mit der Methode show() angezeigt.
         */
        binding!!.gotofeedbackbtn.setOnClickListener {
            val feedbackBinding = FeedbackDialogLayoutBinding.inflate(layoutInflater)

            MaterialAlertDialogBuilder(it.context, R.style.CustomAlertDialog)
                .setTitle(R.string.feedback)
                .setView(feedbackBinding.root)
                .setPositiveButton(R.string.sendfeedback) { _, _ ->
                    val designFeedback = feedbackBinding.ratingBarDesign.rating
                    val functionalityFeedback = feedbackBinding.ratingBarFunctionality.rating
                    val generalOpinion = feedbackBinding.editTextGeneralOpinion.text.toString()

                    // Berechnung der Gesamtbewertung als Durchschnitt aus Design- und Funktionsbewertung
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

        /**
         * Dieser Codeblock setzt einen OnChangeListener auf den languageLevelSlider Slider.
         * Wenn sich der Wert des Sliders ändert, wird der neue Wert mit der saveLanguageLevelToFirebase-Methode des ViewModels in Firebase gespeichert.
         *
         * Der neue Wert wird auch zur Aktualisierung der TextView languageLevelText verwendet.
         * Der Wert wird in eine ganze Zahl umgewandelt und in einem When-Ausdruck verwendet, um die entsprechende Sprachlevel-String-Ressource zu bestimmen.
         * Die String-Ressource wird dann abgerufen und als Text des TextViews festgelegt.
         *
         * Entspricht der Wert keiner der vordefinierten Sprachstufen, wird die String-Ressource für 'undefined' verwendet.
         */
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


        /**
         * Dieser Codeblock setzt einen OnClickListener auf die Schaltfläche backtodashbtn3.
         * Wenn die Schaltfläche angeklickt wird, navigiert sie mithilfe der findNavController()-Methode und der actionSettingsFragmentToDashboardFragment()-Aktion zum DashboardFragment.
         * Dies ermöglicht dem Benutzer, von der Einstellungsseite zurück zur Dashboard-Seite zu navigieren.
         */
        binding!!.backtodashbtn3.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToDashboardFragment())
        }

        /**
         * Dieser Codeblock setzt einen OnClickListener auf die Schaltfläche logoutbtn.
         * Wenn der Button angeklickt wird, ruft er die logout()-Methode des ViewModels auf, um den Benutzer abzumelden.
         * Anschließend wird mit der findNavController()-Methode und der actionSettingsFragmentToLoginFragment()-Aktion zum LoginFragment navigiert.
         * Dadurch kann sich der Benutzer abmelden und wird zur Login-Seite weitergeleitet.
         */
        binding!!.logoutbtn.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLoginFragment())
        }


    }

    /**
     * Lädt das aktuelle Profilbild des Benutzers.
     * Es holt die Benutzer-ID von FirebaseAuth und verwendet sie, um das Dokument des Benutzers in Firestore abzurufen.
     * Wenn das Dokument existiert, wird das Profilbild-URL abgerufen und verwendet, um das Bild mit Glide zu laden und in der ImageView anzuzeigen.
     * Wenn das Dokument nicht existiert oder das Profilbild-URL leer ist, wird ein Platzhalterbild angezeigt.
     */
    private fun loadCurrentProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("user").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val imageUrl = document.getString("profilePicture")
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(binding!!.profileImage)
                        } else {
                            // passenden Platzhalter suchen
                            binding!!.profileImage.setImageResource(R.drawable.elevationtodolistrelocation)
                        }
                    }
                }.addOnFailureListener {
                Toast.makeText(context, R.string.errorload, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Wird aufgerufen, wenn die View-Hierarchie des Fragments zerstört wird.
     * Hier wird das Binding-Objekt auf null gesetzt, um Memory Leaks zu vermeiden,
     * da das Binding-Objekt eine Referenz auf die View hält, welche nicht länger existiert.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}