package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("userName") ?: ""
        binding.etUserName.setText(userName)

        binding.btnSaveProfile.setOnClickListener {
            val newUserName = binding.etUserName.text.toString()

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", newUserName)
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener{
            val userNameBack = intent.getStringExtra("userName") ?: ""
            binding.etUserName.setText(userName)
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userNameBack)
            startActivity(intent)
        }

    }
}