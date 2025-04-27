package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityExchangeSummaryBinding
import com.example.icm_proyecto01.model.UserBook
import com.squareup.picasso.Picasso
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class ExchangeSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExchangeSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Libros
        val userBook = intent.getSerializableExtra("selectedBook") as? UserBook
        val tituloIntercambio = intent.getStringExtra("titulo") ?: "Sin título"
        val direccion = intent.getStringExtra("direccion") ?: "Sin dirección"
        val fecha = intent.getStringExtra("fecha") ?: "-"
        val hora = intent.getStringExtra("hora") ?: "-"
        val estadoLibroDisponible = intent.getStringExtra("estadoLibroDisponible") ?: "-"
        val portadaLibroDisponible = intent.getStringExtra("portadaUrl") ?: ""

        binding.tvBookExchangeTitle.text = tituloIntercambio
        binding.tvBookExchangeState.text = "Estado: $estadoLibroDisponible"
        binding.tvExchangeLocation.text = direccion
        binding.tvExchangeDateTime.text = "$fecha - $hora"

        if (portadaLibroDisponible.isNotEmpty() && portadaLibroDisponible != "null") {
            Picasso.get().load(portadaLibroDisponible)
                .placeholder(R.drawable.default_book)
                .into(binding.bookImageExchange)
        } else {
            binding.bookImageExchange.setImageResource(R.drawable.default_book)
        }

        // Libro ofrecido por el usuario
        if (userBook != null) {
            binding.tvBookUserTitle.text = userBook.titulo
            //binding.tvBookUserAuthor.text = "Autor: ${userBook.autor}"
            binding.tvBookUserGenre.text = "Género: ${userBook.genero}"
            binding.tvBookUserState.text = "Estado: ${userBook.estado}"

            if (userBook.portadaUrl.isNotEmpty()) {
                Picasso.get().load(userBook.portadaUrl).placeholder(R.drawable.default_book)
                    .into(binding.bookImageUser)
            } else {
                binding.bookImageUser.setImageResource(R.drawable.default_book)
            }
        }

        binding.btnConfirmExchange.setOnClickListener {
            val direccion = intent.getStringExtra("direccion") ?: "Sin dirección"
            val direccionCodificada = URLEncoder.encode(direccion, StandardCharsets.UTF_8.toString())
            val uri = "https://www.google.com/maps/search/?api=1&query=$direccionCodificada"

            val intentWeb = Intent(this, ExchangeWebRouteActivity::class.java).apply {
                putExtra("url", uri)
            }
            startActivity(intentWeb)
        }




        binding.btnBack.setOnClickListener { finish() }
    }

}