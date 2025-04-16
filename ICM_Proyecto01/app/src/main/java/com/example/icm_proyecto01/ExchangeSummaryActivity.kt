package com.example.icm_proyecto01

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityExchangeSummaryBinding
import com.example.icm_proyecto01.model.UserBook
import com.squareup.picasso.Picasso

class ExchangeSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExchangeSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedBook = intent.getSerializableExtra("selectedBook") as? UserBook

        val titulo = intent.getStringExtra("titulo") ?: "Sin título"
        val direccion = intent.getStringExtra("direccion") ?: "Sin dirección"
        val fecha = intent.getStringExtra("fecha") ?: "-"
        val hora = intent.getStringExtra("hora") ?: "-"
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)

        if (selectedBook != null) {
            // Mostrar datos del libro
            binding.tvBookTitle.text = selectedBook.titulo
            binding.tvBookAuthor.text = selectedBook.autor
            binding.tvBookGenre.text = selectedBook.genero
            binding.tvBookState.text = selectedBook.estado

            if (selectedBook.portadaUrl.isNotEmpty()) {
                Picasso.get().load(selectedBook.portadaUrl).into(binding.bookImage)
            } else {
                binding.bookImage.setImageResource(R.drawable.default_book)
            }

            // Mostrar datos del punto de intercambio
            binding.tvExchangeLocation.text = direccion
            binding.tvExchangeDateTime.text = "$fecha - $hora"
        } else {
            binding.tvBookTitle.text = "No se recibió información del libro"
        }

        binding.btnConfirmExchange.setOnClickListener {
            // Aquí se puede guardar el intercambio, mostrar una animación, etc.
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
