package com.example.icm_proyecto01

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityExchangePointBinding

class ExchangePointActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExchangePointBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure binding is set correctly
        binding = ActivityExchangePointBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button navigation
        binding.backButton.setOnClickListener {
            finish() // Close current activity
        }
    }
}
