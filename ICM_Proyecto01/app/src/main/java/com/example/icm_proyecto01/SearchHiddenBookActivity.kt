package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivitySearchHiddenBookBinding

class SearchHiddenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchHiddenBookBinding
    private var userName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchHiddenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        userName = sharedPref.getString("userName", "Jane Doe")

        binding.btnFoundBook.setOnClickListener {
            Toast.makeText(this, "¡Libro oculto encontrado!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
            finish()
        }
    }
}
