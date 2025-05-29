package com.example.icm_proyecto01.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.icm_proyecto01.fragments.CreatedExchangesFragment
import com.example.icm_proyecto01.fragments.OfferedExchangesFragment

class ExchangesPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CreatedExchangesFragment()
            1 -> OfferedExchangesFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
