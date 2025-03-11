package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityExchangePointBinding
import com.example.icm_proyecto01.databinding.ActivityRewardsBinding


class RewardsActivity : AppCompatActivity(){

    private lateinit var binding: ActivityRewardsBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure binding is set correctly
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener(){
            finish()
        }
    }
}