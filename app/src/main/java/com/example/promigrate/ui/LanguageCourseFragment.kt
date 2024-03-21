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
import com.example.promigrate.R
import com.example.promigrate.adapter.LanguageCourseAdapter
import com.example.promigrate.databinding.FragmentLanguageCourseBinding

/**
 * LanguageCourseFragment ist ein Fragment, das die Sprachkurse anzeigt.
 * Es beobachtet die userProfileData und educationaloffers im ViewModel und aktualisiert die Liste der Sprachkurse entsprechend.
 * Es ermöglicht dem Benutzer auch, zum VocabularyLearningFragment zu navigieren, indem er auf den "backToDashboardButton" klickt.
 *
 * Es verwendet ein Binding-Objekt, um auf die im XML definierten Views zuzugreifen, und einen LanguageCourseAdapter, um die Liste der Sprachkurse anzuzeigen.
 */
class LanguageCourseFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    private val viewModel: MainViewModel by activityViewModels()

    // Binding-Objekt, das Zugriff auf die im XML definierten Views ermöglicht.
    // Wird nullable gehalten, da es zwischen onCreateView und onDestroyView null sein kann.
    private var binding: FragmentLanguageCourseBinding? = null

    private lateinit var languageCourseAdapter: LanguageCourseAdapter

    /**
     * Initialisiert die Benutzeroberfläche des Fragments, indem das entsprechende Layout aufgeblasen wird.
     *
     * @param inflater: Das LayoutInflater-Objekt, das zum Aufblasen des Layouts des Fragments verwendet wird.
     * @param container: Die übergeordnete ViewGroup, in den die neue Ansicht eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte View für das Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLanguageCourseBinding.inflate(inflater, container, false)

        // Beobachtet die userProfileData im ViewModel
        viewModel.userProfileData.observe(viewLifecycleOwner) { userProfile ->
            // Konvertiert den gewünschten Standort und das Sprachniveau
            val location = userProfile?.desiredLocation?.let { convertLocation(it) }
            val sprachniveau = userProfile?.languageLevel?.let { convertLanguageLevel(it) }
            // Wenn Standort und Sprachniveau nicht null sind, werden Bildungsangebote abgerufen
            if (location != null) {
                if (sprachniveau != null) {
                    viewModel.fetchEducationalOffers("MC", location, sprachniveau, 1)
                }
            }
        }

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

        // Erstellt eine Instanz des LanguageCourseAdapter
        languageCourseAdapter = LanguageCourseAdapter()
        binding!!.rvLanguageCourses.apply {
            // Setzt den Adapter für das RecyclerView
            adapter = languageCourseAdapter
            // Setzt den LayoutManager für das RecyclerView
            layoutManager = LinearLayoutManager(context)
        }



        // Beobachtet die educationaloffers im ViewModel
        viewModel.educationaloffers.observe(viewLifecycleOwner) { offer ->
            // Übersetzt die Bildungsangebote
            viewModel.translateEducationalOffers(offer) { translatedOffers ->
                // Aktualisiert die Liste im Adapter mit den übersetzten Angeboten
                languageCourseAdapter.submitList(translatedOffers)
            }
        }

        // Setzt einen OnClickListener auf den "backToDashboardButton"
        binding!!.backToDashboardButton.setOnClickListener {
            // Erstellt eine Navigationsaktion zum VocabularyLearningFragment
            val action =
                LanguageCourseFragmentDirections.actionLanguageCourseFragmentToVocabularyLearningFragment()
            // Navigiert zum VocabularyLearningFragment
            findNavController().navigate(action)
        }
    }

    /**
     * Diese Funktion konvertiert den gewünschten Standort (eine Stadt in Deutschland oder Österreich) in eine spezielle Zeichenkette.
     * Diese Zeichenkette enthält den Namen der Stadt, die Postleitzahl und die geographischen Koordinaten (Längen- und Breitengrad).
     * Diese spezielle Zeichenkette wird dann von anderen Funktionen verwendet, um Bildungsangebote in der Nähe des gewünschten Standorts zu suchen.
     *
     * @param desiredLocation: Der gewünschte Standort, der konvertiert werden soll. Es sollte der Name einer Stadt in Deutschland oder Österreich sein.
     * @return: Eine Zeichenkette, die den Namen der Stadt, die Postleitzahl und die geographischen Koordinaten enthält.
     *         Wenn der gewünschte Standort nicht in der Liste der unterstützten Städte enthalten ist, wird eine leere Zeichenkette zurückgegeben.
     */
    private fun convertLocation(desiredLocation: String): String {
        return when (desiredLocation) {
            "Aachen" -> "Aachen_52062_6.08342_50.77535"
            "Aschaffenburg" -> "Aschaffenburg_63739_9.15448_49.97823"
            "Augsburg, Bayern" -> "Augsburg_86150_10.89851_48.366804"
            "Bamberg" -> "Bamberg_96047_10.88659_49.891604"
            "Bayreuth" -> "Bayreuth_95444_11.576307_49.942719"
            "Berlin" -> "Berlin_10117_13.404954_52.5200066"
            "Bielefeld" -> "Bielefeld_33602_8.53247_52.030228"
            "Bochum" -> "Bochum_44787_7.215982_51.481844"
            "Bonn" -> "Bonn_53111_7.0982068_50.73743"
            "Braunschweig" -> "Braunschweig_38100_10.5267696_52.2688736"
            "Bremen" -> "Bremen_28195_8.8016936_53.0792962"
            "Bremerhaven" -> "Bremerhaven_27568_8.576735_53.539584"
            "Chemnitz, Sachsen" -> "Chemnitz_09111_12.913411_50.8322608"
            "Cottbus" -> "Cottbus_03046_14.333333_51.7563108"
            "Darmstadt" -> "Darmstadt_64283_8.651177_49.872775"
            "Dessau-Roßlau" -> "Dessau-Roßlau_06844_12.247637_51.83864"
            "Donauwörth" -> "Donauwörth_86609_10.7815_48.71811"
            "Dortmund" -> "Dortmund_44135_7.4652981_51.5135872"
            "Dresden" -> "Dresden_01067_13.7372621_51.0504088"
            "Duisburg" -> "Duisburg_47051_6.7623293_51.4344079"
            "Düsseldorf" -> "Düsseldorf_40213_6.7763137_51.2254018"
            "Erfurt" -> "Erfurt_99084_11.02988_50.978698"
            "Erlangen" -> "Erlangen_91052_11.0119614_49.5896744"
            "Essen, Ruhr" -> "Essen_45127_7.0122815_51.4565704"
            "Esslingen am Neckar" -> "Esslingen_73728_9.3079682_48.7433425"
            "Frankfurt am Main" -> "Frankfurt_60311_8.6821267_50.1109221"
            "Freiburg im Breisgau" -> "Freiburg_79098_7.8523213_47.9958656"
            "Fulda" -> "Fulda_36037_9.6696066_50.5516204"
            "Fürth, Bayern" -> "Fürth_90762_10.988667_49.4759303"
            "Gelsenkirchen" -> "Gelsenkirchen_45879_7.0960121_51.5072759"
            "Gera" -> "Gera_07545_12.08344_50.8787"
            "Gießen, Lahn" -> "Gießen_35390_8.6784033_50.5840512"
            "Göttingen, Niedersachsen" -> "Göttingen_37073_9.93228_51.5327604"
            "Gütersloh" -> "Gütersloh_33330_8.3834481_51.9066346"
            "Hagen, Westfalen" -> "Hagen_58095_7.463279_51.35848"
            "Halle (Saale)" -> "Halle_06108_11.96975_51.4825041"
            "Hamburg" -> "Hamburg_20095_9.9936819_53.5510846"
            "Hamm, Westfalen" -> "Hamm_59065_7.82089_51.6738583"
            "Hanau" -> "Hanau_63450_8.91654_50.13361"
            "Heidelberg, Neckar" -> "Heidelberg_69117_8.69079_49.4093582"
            "Heilbronn, Neckar" -> "Heilbronn_74072_9.2186552_49.1422912"
            "Herne, Westfalen" -> "Herne_44623_7.22572_51.53804"
            "Hildesheim" -> "Hildesheim_31134_9.95795_52.154778"
            "Ingolstadt, Donau" -> "Ingolstadt_85049_11.42372_48.76508"
            "Jena" -> "Jena_07743_11.5892372_50.927054"
            "Kaiserslautern" -> "Kaiserslautern_67655_7.76833_49.44469"
            "Karlsruhe, Baden" -> "Karlsruhe_76133_8.40365_49.0068901"
            "Kassel, Hessen" -> "Kassel_34117_9.4910033_51.3127114"
            "Kempten (Allgäu)" -> "Kempten_87435_10.3171296_47.72614"
            "Kiel" -> "Kiel_24103_10.135555_54.32133"
            "Koblenz am Rhein" -> "Koblenz_56068_7.5889959_50.3569429"
            "Krefeld" -> "Krefeld_47798_6.562334_51.3387609"
            "Köln" -> "Köln_50667_6.9602786_50.937531"
            "Landshut, Isar" -> "Landshut_84028_12.152686_48.537281"
            "Leipzig" -> "Leipzig_04109_12.3730747_51.3396955"
            "Leverkusen" -> "Leverkusen_51373_7.0048329_51.0459247"
            "Linz, Donau" -> "Linz_4020_14.28611_48.30639"
            "Ludwigsburg, Württemberg" -> "Ludwigsburg_71638_9.2220122_48.8973113"
            "Ludwigshafen am Rhein" -> "Ludwigshafen_67059_8.44518_49.47741"
            "Lübeck" -> "Lübeck_23552_10.6865593_53.8654673"
            "Lüneburg" -> "Lüneburg_21335_10.40662_53.24932"
            "Magdeburg" -> "Magdeburg_39104_11.6276237_52.1205333"
            "Mainz am Rhein" -> "Mainz_55116_8.2472526_49.9928617"
            "Mannheim" -> "Mannheim_68159_8.46372_49.4874592"
            "Mönchengladbach" -> "Mönchengladbach_41061_6.44172_51.18539"
            "Mülheim an der Ruhr" -> "Mülheim_45468_6.8833701_51.4274411"
            "München" -> "München_80331_11.5819806_48.1351253"
            "Münster, Westfalen" -> "Münster_48143_7.6251879_51.9606649"
            "Neuss" -> "Neuss_41460_6.6916484_51.2041968"
            "Nürnberg, Mittelfranken" -> "Nürnberg_90403_11.07752_49.44999"
            "Oberhausen, Rheinland" -> "Oberhausen_46045_6.85144_51.496334"
            "Offenbach am Main" -> "Offenbach_63065_8.76007_50.095636"
            "Offenburg" -> "Offenburg_77652_7.9405945_48.4723124"
            "Oldenburg (Oldb)" -> "Oldenburg_26122_8.2145521_53.1434501"
            "Osnabrück" -> "Osnabrück_49074_8.04718_52.279911"
            "Paderborn" -> "Paderborn_33098_8.7543892_51.7189205"
            "Pforzheim" -> "Pforzheim_75175_8.70342_48.89176"
            "Potsdam" -> "Potsdam_14467_13.0644729_52.3905689"
            "Recklinghausen, Westfalen" -> "Recklinghausen_45657_7.19996_51.61406"
            "Regensburg" -> "Regensburg_93047_12.1016244_49.0134297"
            "Reutlingen" -> "Reutlingen_72764_9.21602_48.49344"
            "Rosenheim, Oberbayern" -> "Rosenheim_83022_12.1286127_47.8563713"
            "Rostock" -> "Rostock_18055_12.1404931_54.0924406"
            "Saarbrücken" -> "Saarbrücken_66111_6.9816347_49.2401572"
            "Salzgitter" -> "Salzgitter_38226_10.331634_52.154778"
            "Schweinfurt" -> "Schweinfurt_97421_10.2252821_50.0493286"
            "Schwerin, Mecklenburg" -> "Schwerin_19053_11.4183752_53.6288297"
            "Siegen" -> "Siegen_57072_8.024309_50.87481"
            "Solingen" -> "Solingen_42651_7.0939957_51.165219"
            "Stuttgart" -> "Stuttgart_70173_9.181332_48.7784485"
            "Trier" -> "Trier_54290_6.641389_49.749992"
            "Ulm, Donau" -> "Ulm_89073_9.9876076_48.4010822"
            "Villingen-Schwenningen" -> "Villingen-Schwenningen_78050_8.540851_48.0616012"
            "WIEN" -> "Wien_1010_16.3738189_48.2081743"
            "Wiesbaden" -> "Wiesbaden_65183_8.2416559_50.0782184"
            "Wolfsburg" -> "Wolfsburg_38440_10.7865461_52.4226503"
            "Wuppertal" -> "Wuppertal_42103_7.1493501_51.2562128"
            "Würzburg" -> "Würzburg_97070_9.93228_49.791304"
            "Zwickau" -> "Zwickau_08056_12.4956443_50.7090527"


            else -> ""// Wenn nichts übereinstimmt, wird ein leerer String übergeben
        }
    }

    /**
     * Diese Funktion konvertiert das Sprachniveau in eine spezielle Zeichenkette.
     * Diese Zeichenkette wird dann von anderen Funktionen verwendet, um Bildungsangebote auf der Grundlage des Sprachniveaus zu suchen.
     *
     * @param languageLevel: Das Sprachniveau, das konvertiert werden soll. Es sollte einer der Werte sein, die in den Ressourcen-Strings definiert sind.
     * @return: Eine Zeichenkette, die das Sprachniveau repräsentiert.
     *          Wenn das Sprachniveau nicht in der Liste der unterstützten Niveaus enthalten ist, wird ein Standardwert zurückgegeben.
     */
    private fun convertLanguageLevel(languageLevel: String): String {
        return when (languageLevel) {
            getString(R.string.beginner) -> "MC01 2" // Es gibt keine Kurse unter A2.
            getString(R.string.basic_knowledge) -> "MC01 2" // Konvertierung für A2.
            getString(R.string.intermediate) -> "MC01 3" // Konvertierung für B1.
            getString(R.string.independent) -> "MC01 4" // Konvertierung für B2.
            getString(R.string.proficient) -> "MC01 5" // Konvertierung für C1.
            getString(R.string.near_native) -> "MC01 6" // Konvertierung für C2.
            else -> "MC01 2" // Standard-/Fallback-Konvertierung.
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
