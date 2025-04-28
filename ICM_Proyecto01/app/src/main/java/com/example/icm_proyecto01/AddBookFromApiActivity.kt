package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityAddBookFromApiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class AddBookFromApiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookFromApiBinding
    private var userName: String? = null
    private var bookId: String? = null // NUEVO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookFromApiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = getSharedPreferences("UserProfile", MODE_PRIVATE).getString("userName", "Jane Doe")

        // Recibir datos enviados
        bookId = intent.getStringExtra("bookId")
        val title = intent.getStringExtra("title") ?: "Sin título"
        val author = intent.getStringExtra("author") ?: "Autor desconocido"
        val genre = intent.getStringExtra("genre") ?: "Sin género"
        val imageUrl = intent.getStringExtra("image")

        binding.tvTitulo.text = title
        binding.tvAutor.text = author
        binding.tvGenero.text = genre
        Picasso.get().load(imageUrl).into(binding.bookImage)

        val estados = listOf("Como nuevo", "Usado", "Dañado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        binding.spinnerState.adapter = adapter

        binding.btnAgregar.setOnClickListener {
            val estadoSeleccionado = binding.spinnerState.selectedItem.toString()

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val database = FirebaseDatabase.getInstance().reference

            if (userId != null && bookId != null) {
                val bookData = mapOf(
                    "hidden" to false,
                    "state" to estadoSeleccionado,
                    "title" to title,
                    "author" to author,
                    "genre" to genre,
                    "imageUrl" to imageUrl
                )

                database.child("Users")
                    .child(userId)
                    .child("Books")
                    .child(bookId!!)
                    .setValue(bookData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Libro agregado correctamente", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("userName", userName)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al agregar el libro", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error: usuario o libro inválido", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
