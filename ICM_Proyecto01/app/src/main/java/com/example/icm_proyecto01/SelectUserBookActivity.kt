package com.example.icm_proyecto01

import UserRepository
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapters.UserBooksAdapter
import com.example.icm_proyecto01.databinding.ActivitySelectUserBookBinding
import com.example.icm_proyecto01.model.UserBook

class SelectUserBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectUserBookBinding
    private lateinit var userRepository: UserRepository
    private val userBooks = mutableListOf<UserBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val from = intent.getStringExtra("from") ?: ""

        val exchangeTitulo = intent.getStringExtra("titulo")
        val exchangeDireccion = intent.getStringExtra("direccion")
        val exchangeFecha = intent.getStringExtra("fecha")
        val exchangeHora = intent.getStringExtra("hora")
        val exchangeLat = intent.getDoubleExtra("lat", 0.0)
        val exchangeLon = intent.getDoubleExtra("lon", 0.0)

        val estadoLibroDisponible = intent.getStringExtra("estadoLibroDisponible")
        val portadaLibroDisponible = intent.getStringExtra("portadaLibroDisponible")

        // 🚀 Load books using UserRepository
        val repository = UserRepository()
        repository.fetchUserBooks { userBooks ->

            if (userBooks.isEmpty()) {
                Toast.makeText(this, "No tienes libros para seleccionar.", Toast.LENGTH_SHORT).show()
                return@fetchUserBooks
            }

            val adapter = UserBooksAdapter(userBooks) { selectedBook ->

                if (from == "createExchange") {
                    val resultIntent = Intent().apply {
                        putExtra("selectedBookTitle", selectedBook.titulo)
                        putExtra("selectedBookState", selectedBook.estado)
                        putExtra("selectedBookCoverUrl", selectedBook.portadaUrl ?: "")
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()

                } else {
                    val intent = Intent(this, ExchangeSummaryActivity::class.java).apply {
                        putExtra("selectedBook", selectedBook)
                        putExtra("titulo", exchangeTitulo)
                        putExtra("direccion", exchangeDireccion)
                        putExtra("fecha", exchangeFecha)
                        putExtra("hora", exchangeHora)
                        putExtra("lat", exchangeLat)
                        putExtra("lon", exchangeLon)
                        putExtra("estadoLibroDisponible", estadoLibroDisponible)
                        putExtra("portadaLibroDisponible", portadaLibroDisponible)
                    }
                    startActivity(intent)
                    finish()
                }
            }

            binding.rvUserBooks.layoutManager = LinearLayoutManager(this)
            binding.rvUserBooks.adapter = adapter
        }
    }

}
