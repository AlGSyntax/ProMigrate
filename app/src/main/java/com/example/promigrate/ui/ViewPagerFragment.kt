package com.example.promigrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.promigrate.MainViewModel
import com.example.promigrate.R

/**
 * ViewPagerFragment ist ein Fragment, das einen ViewPager2 enthält.
 * Es verwendet ein ViewModel, um Daten zwischen den Fragmenten zu teilen.
 * Es enthält auch eine innere Klasse, die als Adapter für den ViewPager2 dient.
 */
class ViewPagerFragment : Fragment() {

    // Die Verwendung von activityViewModels() bietet Zugriff auf das ViewModel, das von der
    // zugehörigen Activity genutzt wird.
    val viewModel: MainViewModel by activityViewModels()

    // Der ViewPager2, der die verschiedenen Seiten anzeigt.
    private lateinit var viewPager: ViewPager2

    /**
     * Wird aufgerufen, um die Ansicht des Fragments zu erstellen.
     * Es bläst das XML-Layout auf und gibt die Wurzelansicht zurück.
     *
     * @param inflater: Der LayoutInflater, der zum Aufblasen der Ansichten verwendet wird.
     * @param container: Die übergeordnete Ansicht, in die das Fragment eingefügt wird.
     * @param savedInstanceState: Ein Bundle, das den Zustand des Fragments gespeichert hat.
     * @return :Die erstellte Ansicht des Fragments.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_pager, container, false)
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
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = MyViewPagerAdapter(this)
    }

    /**
     * Bewegt den ViewPager zur nächsten Seite, wenn es eine gibt.
     */
    fun moveToNextPage() {
        val nextPage = viewPager.currentItem + 1
        if (nextPage < (viewPager.adapter?.itemCount ?: 0)) {
            viewPager.currentItem = nextPage
        }
    }

    /**
     * MyViewPagerAdapter ist ein Adapter für den ViewPager2.
     * Es erstellt die verschiedenen Fragmente, die im ViewPager angezeigt werden.
     */
    private inner class MyViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        /**
         * Gibt die Anzahl der Seiten im ViewPager zurück.
         *
         * @return: Die Anzahl der Seiten.
         */
        override fun getItemCount(): Int = 2 // Anzahl der Seiten

        /**
         * Erstellt das Fragment für die angegebene Position.
         *
         * @param position: Die Position des Fragments im ViewPager.
         * @return: Das erstellte Fragment.
         */
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ReOnboardingFragment()
                1 -> JobOffersSelectionFragment()
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }
}