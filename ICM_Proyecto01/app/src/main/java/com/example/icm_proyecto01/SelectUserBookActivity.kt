package com.example.icm_proyecto01

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
    private val userBooks = mutableListOf<UserBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras == null) {
            Toast.makeText(this, "No se recibió información del punto", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val exchangeTitulo = extras.getString("titulo")
        val exchangeDireccion = extras.getString("direccion")
        val exchangeFecha = extras.getString("fecha")
        val exchangeHora = extras.getString("hora")
        val exchangeLat = extras.getDouble("lat")
        val exchangeLon = extras.getDouble("lon")

        val sharedPref = getSharedPreferences("UserBooks", MODE_PRIVATE)
        for ((_, value) in sharedPref.all) {
            val data = value as? String ?: continue
            val parts = data.split("|").map { it.trim() }
            if (parts.size >= 5) {
                val book = UserBook(parts[0], parts[1], parts[2], parts[3], parts[4])
                userBooks.add(book)
            }
        }

        val adapter = UserBooksAdapter(userBooks) { selectedBook ->
            val intent = Intent(this, ExchangeSummaryActivity::class.java).apply {
                putExtra("selectedBook", selectedBook)
                putExtra("titulo", exchangeTitulo)
                putExtra("direccion", exchangeDireccion)
                putExtra("fecha", exchangeFecha)
                putExtra("hora", exchangeHora)
                putExtra("lat", exchangeLat)
                putExtra("lon", exchangeLon)
            }
            startActivity(intent)
            finish()
        }

        binding.rvUserBooks.layoutManager = LinearLayoutManager(this)
        binding.rvUserBooks.adapter = adapter
    }
}
