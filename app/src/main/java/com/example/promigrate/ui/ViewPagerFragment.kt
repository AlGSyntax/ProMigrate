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

class ViewPagerFragment : Fragment() {
    val viewModel: MainViewModel by activityViewModels()



    private lateinit var viewPager: ViewPager2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = MyViewPagerAdapter(this)
    }

    fun moveToNextPage() {
        val nextPage = viewPager.currentItem + 1
        if (nextPage < (viewPager.adapter?.itemCount ?: 0)) {
            viewPager.currentItem = nextPage
        }
    }

    fun navigateToDetailToDoJobApplicationFragment() {
        viewPager.currentItem = 1 // Index von DetailToDoJobApplicationFragment im ViewPager
    }

    private inner class MyViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2 // Anzahl der Seiten

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ReOnboardingFragment()
                1 -> JobOffersSelectionFragment()
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }
}
