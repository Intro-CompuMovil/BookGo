package com.example.icm_proyecto01

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.adapters.ExchangesPagerAdapter
import com.example.icm_proyecto01.databinding.ActivityMyExchangesBinding
import com.google.android.material.tabs.TabLayoutMediator

class MyExchangesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyExchangesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyExchangesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ExchangesPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Creados"
                1 -> "Ofrecidos"
                else -> ""
            }
        }.attach()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
