// IntegrationCourseFragment.kt   »  vollständig ersetzen
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
import com.example.promigrate.adapter.IntegrationCourseAdapter
import com.example.promigrate.data.model.TerminResponse
import com.example.promigrate.databinding.FragmentIntegrationCourseBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Zeigt einen einzelnen statischen Integrationskurs an, abhängig von
 *   – der im Benutzerprofil gewählten Stadt (Berlin, Hannover, München)
 *   – der aktuellen UI-Sprache (Deutsch = Original, sonst Englisch).
 */
class IntegrationCourseFragment : Fragment() {

    /** Gemeinsames ViewModel  */
    private val viewModel: MainViewModel by activityViewModels()

    /** ViewBinding für das Layout. */
    private var binding: FragmentIntegrationCourseBinding? = null

    /** Adapter für RecyclerView. */
    private lateinit var adapter: IntegrationCourseAdapter

    /** Baut Layout auf. */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntegrationCourseBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = IntegrationCourseAdapter()
        binding!!.rvIntegrationCourses.layoutManager = LinearLayoutManager(requireContext())
        binding!!.rvIntegrationCourses.adapter = adapter

        /* ----------  Profiländerungen → Kursliste  ---------- */
        viewModel.userProfileData.observe(viewLifecycleOwner) { profile ->
            profile ?: return@observe
            val city     = profile.desiredLocation ?: ""
            val langCode = Locale.getDefault().language     // "de", "en", …
            val course   = buildCourse(city, langCode)     // statischer Datensatz

            // -> jetzt in Ziel-Sprache bringen (falls ≠ de)
            viewModel.translateEducationalOffers(listOf(course)) { translated ->
                adapter.submitList(translated)             // an RecyclerView binden
            }
        }

        /* ----------  Zurück-Button  ---------- */
        binding!!.backButton.setOnClickListener {
            findNavController().navigate(
                IntegrationCourseFragmentDirections
                    .actionIntegrationCourseFragmentToRelocationAndIntegrationFragment()
            )
        }
    }

    /**
     * Erstellt genau einen {@link TerminResponse} für die gewünschte Stadt.
     *
     * @param city Name der Stadt (Berlin, Hannover, München oder beliebig).
     * @param lang Sprachcode, z. B. "de" oder "en".
     * @return Statistischer Kursdatensatz (Titel, Inhalt …) mit Unix-Millis.
     */
    /**  Statisches Kursobjekt pro Stadt + Sprache  */
    private fun buildCourse(city: String, lang: String): TerminResponse {

        /** Wandelt „dd.MM.yyyy“ in Unix-Millis. */
        fun ts(date: String): Long =
            SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).parse(date)!!.time

        /* Vorlage für Felder */
        data class Tpl(
            val title: String, val provider: String, val address: String, val exam: String,
            val html: String, val cost: Int, val funded: Boolean,
            val degree: String, val target: String,
            val start: Long, val end: Long, val deadline: Long
        )

        /* Stadt- und Sprachabhängige Auswahl */
        val t = when (city) {
            "Berlin"   -> if (lang == "de") Tpl(
                "Integrationskurs – Orientierung & Sprache (Berlin)",
                "Berlin Integration Center",
                "Integrationsallee 10, 10117 Berlin",
                "BAMF",
                "Orientierungskurs + Sprachmodul A1-B1.",
                0, true,
                "Zertifikat Integrationskurs",
                "Neuankömmlinge in Berlin",
                ts("01.03.2024"), ts("31.08.2024"), ts("15.02.2024")
            ) else Tpl(
                "Integration Support Program (Berlin)",
                "Berlin Integration Center",
                "Integrationsallee 10, 10117 Berlin",
                "Federal Office",
                "Orientation classes and German A1–B1 for newcomers.",
                0, true,
                "Integration Certificate",
                "Newcomers in Berlin",
                ts("01.03.2024"), ts("31.08.2024"), ts("15.02.2024")
            )

            "Hannover" -> if (lang == "de") Tpl(
                "Integrationskurs – Orientierung & Sprache (Hannover)",
                "Hannover Integration Point",
                "Willkommensweg 5, 30159 Hannover",
                "BAMF",
                "Deutsch A1-B1 + Orientierungsteil.",
                0, true,
                "Zertifikat Integrationskurs",
                "Neuankömmlinge in Hannover",
                ts("15.04.2024"), ts("15.10.2024"), ts("31.03.2024")
            ) else Tpl(
                "Integration Support Program (Hannover)",
                "Hannover Integration Point",
                "Willkommensweg 5, 30159 Hannover",
                "Federal Office",
                "German A1–B1 plus orientation module.",
                0, true,
                "Integration Certificate",
                "Newcomers in Hannover",
                ts("15.04.2024"), ts("15.10.2024"), ts("31.03.2024")
            )

            "München"  -> if (lang == "de") Tpl(
                "Integrationskurs – Orientierung & Sprache (München)",
                "München Welcome Center",
                "Brunnenplatz 3, 80331 München",
                "BAMF",
                "Deutsch A1-B1 kombiniert mit Orientierungseinheiten.",
                0, true,
                "Zertifikat Integrationskurs",
                "Neuankömmlinge in München",
                ts("01.05.2024"), ts("30.11.2024"), ts("15.04.2024")
            ) else Tpl(
                "Integration Support Program (München)",
                "Munich Welcome Center",
                "Brunnenplatz 3, 80331 München",
                "Federal Office",
                "German A1–B1 with civic orientation.",
                0, true,
                "Integration Certificate",
                "Newcomers in Munich",
                ts("01.05.2024"), ts("30.11.2024"), ts("15.04.2024")
            )

            else      -> Tpl(
                "Integrationskurs ($city)",
                "Integration Center $city",
                "Hauptstraße 1, $city",
                "BAMF",
                "Deutsch A1-B1 plus Orientierung.",
                0, true,
                "Zertifikat Integrationskurs",
                "Neuankömmlinge in $city",
                ts("01.06.2024"), ts("30.12.2024"), ts("15.05.2024")
            )
        }

        /* Zusammenbauen in TerminResponse */
        val addr   = TerminResponse.Adresse(TerminResponse.OrtStrasse(t.address))
        val prov   = TerminResponse.Bildungsanbieter(t.provider, addr)
        val offer  = TerminResponse.Angebot(t.title, prov, t.html, t.degree, t.target)
        val idNum  = when (city) { "Berlin" -> 11; "Hannover" -> 12; "München" -> 13; else -> 0 }

        return TerminResponse(
            id              = idNum,
            angebot         = offer,
            pruefendeStelle = t.exam,
            beginn          = t.start,
            ende            = t.end,
            kostenWert      = t.cost,
            foerderung      = t.funded,
            anmeldeschluss  = t.deadline
        )
    }

    /** Räume Binding auf. */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}