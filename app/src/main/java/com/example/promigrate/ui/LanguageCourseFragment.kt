package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.promigrate.MainViewModel
import com.example.promigrate.adapter.LanguageCourseAdapter
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.databinding.FragmentLanguageCourseBinding
import java.text.SimpleDateFormat
import java.util.Locale



/**
 * Fragment zum Anzeigen eines einzelnen Sprachkurses (Deutsch- oder Englischkurs).
 *
 * Das Fragment erzeugt für die Profil-Stadt (Berlin, Hannover, München) einen
 * statischen TerminResponse und stellt diesen dar. Anschließend wird – falls die aktuelle
 * UI-Sprache nicht Deutsch ist – der Kursinhalt über
 * eine Methode des ViewModels ins Englische übersetzt
 * und an eine RecyclerView gebunden.
 *
 */
class LanguageCourseFragment : Fragment() {

    /** Activity-Scoped ViewModel. */
    private val viewModel: MainViewModel by activityViewModels()

    /** ViewBinding für das Fragment-Layout. */
    private var binding: FragmentLanguageCourseBinding? = null

    /** RecyclerView-Adapter. */
    private lateinit var languageCourseAdapter: LanguageCourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLanguageCourseBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        languageCourseAdapter = LanguageCourseAdapter()
        binding!!.rvLanguageCourses.layoutManager = LinearLayoutManager(requireContext())
        binding!!.rvLanguageCourses.adapter = languageCourseAdapter

        // ⇣ Profil abhören und anschließend übersetzen
        viewModel.userProfileData.observe(viewLifecycleOwner) { p ->
            p ?: return@observe
            val city   = p.desiredLocation ?: ""
            val course = buildCourse(city, Locale.getDefault().language)
            viewModel.translateEducationalOffers(listOf(course)) { translated ->
                languageCourseAdapter.submitList(translated)
            }
        }

        binding!!.backToDashboardButton.setOnClickListener {
            val action = LanguageCourseFragmentDirections
                .actionLanguageCourseFragmentToVocabularyLearningFragment()
            findNavController().navigate(action)
        }
    }


    /**
     * Statischer Kurs pro Stadt + Sprachcode.
     * Unterstützt: Berlin · Hannover · München   /   "de" oder alles andere (= Englisch).
     */
    private fun buildCourse(city: String, lang: String): TerminResponse {

        // Hilfs-Funktion: Datum "dd.MM.yyyy" → Long (ms)
        fun ts(date: String): Long =
            SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).parse(date)!!.time

        // Daten-Container
        data class Template(
            val title: String,
            val provider: String,
            val address: String,
            val examAuth: String,
            val contentHtml: String,
            val cost: Int,
            val funded: Boolean,
            val degreeHtml: String,
            val targetHtml: String,
            val startMs: Long,
            val endMs: Long,
            val deadlineMs: Long
        )

        // --------  Vorlagen wählen  --------
        val t: Template = when (city) {
            "Berlin"   -> if (lang == "de") Template(
                "Sprachkurs Deutsch A1-B1 (Berlin)",
                "Sprachschule Berlin",
                "Musterstraße 1, 10117 Berlin",
                "BAMF",
                "Dieser Kurs vermittelt <b>Deutschkenntnisse</b> von A1 bis B1.",
                390, true,
                "Zertifikat B1 nach Abschluss",
                "Neuzugewanderte in Berlin",
                ts("01.01.2024"), ts("30.06.2024"), ts("15.12.2023")
            ) else Template(
                "English Language Course (Berlin)",
                "Language School Berlin",
                "Example Street 5, 10115 Berlin",
                "Cambridge English",
                "This course covers English level A2 → B1 with focus on conversation.",
                500, false,
                "Certificate B1 (Cambridge)",
                "Residents of Berlin wanting English",
                ts("01.02.2024"), ts("01.08.2024"), ts("15.01.2024")
            )

            "Hannover" -> if (lang == "de") Template(
                "Sprachkurs Deutsch A1-B1 (Hannover)",
                "VHS Hannover",
                "Beispielweg 10, 30159 Hannover",
                "BAMF",
                "Deutsch von A1 bis B1 mit Prüfungsvorbereitung.",
                300, true,
                "B1-Zertifikat",
                "Migranten in Hannover",
                ts("15.03.2024"), ts("15.09.2024"), ts("01.03.2024")
            ) else Template(
                "English Language Course (Hannover)",
                "Language Center Hannover",
                "Bahnhofstraße 20, 30159 Hannover",
                "Cambridge English",
                "Intensive English A2 → B1 for daily and business use.",
                450, false,
                "Certificate B1 (Cambridge)",
                "Hannover residents improving English",
                ts("01.04.2024"), ts("01.10.2024"), ts("15.03.2024")
            )

            "München"  -> if (lang == "de") Template(
                "Sprachkurs Deutsch A1-B1 (München)",
                "Sprachschule München",
                "Beispielplatz 3, 80331 München",
                "BAMF",
                "Deutsch-Intensivkurs von A1 bis B1.",
                350, true,
                "B1-Zertifikat",
                "Migranten in München",
                ts("01.05.2024"), ts("30.11.2024"), ts("15.04.2024")
            ) else Template(
                "English Language Course (München)",
                "Language Academy München",
                "Marienplatz 8, 80331 München",
                "Cambridge English",
                "Comprehensive English A2 → B1 in central Munich.",
                480, false,
                "Certificate B1 (Cambridge)",
                "Anyone in Munich learning English",
                ts("15.06.2024"), ts("15.12.2024"), ts("30.05.2024")
            )

            else      -> Template( // Fallback
                "Sprachkurs Deutsch A1-B1 ($city)",
                "Sprachschule $city",
                "Hauptstraße 1, $city",
                "BAMF",
                "Deutschkenntnisse A1 bis B1.",
                300, true,
                "B1-Zertifikat",
                "Einwanderer in $city",
                ts("01.01.2024"), ts("30.06.2024"), ts("15.12.2023")
            )
        }

        // --------  TerminResponse zusammensetzen  --------
        val ort   = TerminResponse.OrtStrasse(name = t.address)
        val addr  = TerminResponse.Adresse(ortStrasse = ort)
        val prov  = TerminResponse.Bildungsanbieter(name = t.provider, adresse = addr)
        val anbg  = TerminResponse.Angebot(
            titel            = t.title,
            bildungsanbieter = prov,
            inhalt           = t.contentHtml,
            abschlussart     = t.degreeHtml,
            zielgruppe       = t.targetHtml
        )

        val idNum = when (city) { "Berlin" -> 1; "Hannover" -> 2; "München" -> 3; else -> 0 }

        return TerminResponse(
            id              = idNum,
            angebot         = anbg,
            pruefendeStelle = t.examAuth,
            beginn          = t.startMs,
            ende            = t.endMs,
            kostenWert      = t.cost,
            foerderung      = t.funded,
            anmeldeschluss  = t.deadlineMs
        )
    }

    /** Räume Binding auf. */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}