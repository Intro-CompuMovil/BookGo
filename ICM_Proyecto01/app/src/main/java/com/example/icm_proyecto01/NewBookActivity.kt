package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityNewBookBinding

class NewBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate to ProfileActivity when profileImage is clicked
        binding.profileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            finish() // Close current activity
        }

    }
}
