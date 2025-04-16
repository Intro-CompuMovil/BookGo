package com.example.icm_proyecto01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityAddBookFromApiBinding
import com.squareup.picasso.Picasso

class AddBookFromApiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookFromApiBinding
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookFromApiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = getSharedPreferences("UserProfile", MODE_PRIVATE).getString("userName", "Jane Doe")

        // Obtener datos enviados desde BookSearchActivity
        val title = intent.getStringExtra("title") ?: "Sin título"
        val author = intent.getStringExtra("author") ?: "Autor desconocido"
        val genre = intent.getStringExtra("genre") ?: "Sin género"
        val imageUrl = intent.getStringExtra("image")

        // Mostrar los datos en la vista
        binding.tvTitulo.text = title
        binding.tvAutor.text = author
        binding.tvGenero.text = genre
        Picasso.get().load(imageUrl).into(binding.bookImage)

        // Adaptador para el spinner de estado
        val estados = listOf("Como nuevo", "Usado", "Dañado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        binding.spinnerState.adapter = adapter

        binding.btnAgregar.setOnClickListener {
            val estadoSeleccionado = binding.spinnerState.selectedItem.toString()

            val sharedPref = getSharedPreferences("UserBooks", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            val bookData = "$title|$author|$genre|$estadoSeleccionado|$imageUrl"
            editor.putString(title, bookData)
            editor.apply()

            Toast.makeText(this, "Libro agregado correctamente", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
