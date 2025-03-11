package com.example.icm_proyecto01

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("userName") ?: "Jane Doe"
        binding.tvUserName.text = userName

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val savedImageUri = sharedPref.getString("profileImageUri", null)
        if (savedImageUri != null) {
            binding.profileImage.setImageURI(Uri.parse(savedImageUri))
        }

        binding.tvEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

        binding.btnRegisterHiddenBook.setOnClickListener {
           startActivity(Intent(this, RegisterHiddenBookActivity::class.java))
        }

        binding.btnSearchHiddenBook.setOnClickListener {
            startActivity(Intent(this, BookSearchActivity::class.java))
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_profile

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish() // Cierra esta actividad para evitar duplicados
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, MessagesActivity::class.java)) // Cuando esté lista
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> { true
                }
                else -> false
            }
        }
    }
}
