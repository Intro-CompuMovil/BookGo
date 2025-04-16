package com.example.icm_proyecto01

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityBookDetailBinding
import com.squareup.picasso.Picasso

class BookDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos del intent
        val title = intent.getStringExtra("title") ?: "Título no disponible"
        val author = intent.getStringExtra("author") ?: "Autor no disponible"
        val genre = intent.getStringExtra("genre") ?: "Género no disponible"
        val state = intent.getStringExtra("state") ?: "Estado no disponible"
        val imageUrl = intent.getStringExtra("image") ?: ""

        // Mostrar datos en la vista
        binding.tvBookTitle.text = title
        binding.tvBookAuthor.text = "Autor: $author"
        binding.tvBookGenre.text = "Género: $genre"
        binding.tvBookState.text = "Estado: $state"

        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.default_book).into(binding.imgBookCover)
        } else {
            binding.imgBookCover.setImageResource(R.drawable.default_book)
        }

        // Botones
        binding.btnIntercambiar.setOnClickListener {
            Toast.makeText(this, "Función INTERCAMBIAR próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnOcultar.setOnClickListener {
            Toast.makeText(this, "Función OCULTAR próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
