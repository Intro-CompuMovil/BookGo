package com.example.icm_proyecto01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityNewBookBinding

class NewBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewBookBinding
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        userName = sharedPref.getString("userName", "Jane Doe")

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegisterBook.setOnClickListener{
            val title = binding.etBookTitle.text.toString().trim()
            val author = binding.etBookAuthor.text.toString().trim()
            val genre = binding.spinnerGenre.toString()
            val state = binding.spinnerState.toString()

            if (title.isEmpty() || author.isEmpty() ) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPrefB = getSharedPreferences("NewBook", Context.MODE_PRIVATE)
            val editor = sharedPrefB.edit()
            val bookData = "$title | $author | $genre | $state "
            editor.putString(title, bookData)
            editor.apply()

            Toast.makeText(this, "Libro registrado correctamente! ", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
            finish()

        }

    }
}
