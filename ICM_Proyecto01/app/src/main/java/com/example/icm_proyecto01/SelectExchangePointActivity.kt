package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapters.ExchangePointAdapter
import com.example.icm_proyecto01.databinding.ActivitySelectExchangePointBinding
import com.example.icm_proyecto01.model.ExchangePoint
import com.example.icm_proyecto01.model.UserBook

class SelectExchangePointActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectExchangePointBinding
    private val exchangePoints = mutableListOf<ExchangePoint>()

    private lateinit var selectedBook: UserBook

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectExchangePointBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvExchangePoints.layoutManager = LinearLayoutManager(this)

        selectedBook = intent.getSerializableExtra("selectedBook") as? UserBook ?: run {
            Toast.makeText(this, "No se recibió información del libro", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        cargarPuntosDesdeSharedPreferences()
    }

    private fun cargarPuntosDesdeSharedPreferences() {
        val sharedPref = getSharedPreferences("ExchangePoints", MODE_PRIVATE)
        val storedSet = sharedPref.getStringSet("points", null)

        if (!storedSet.isNullOrEmpty()) {
            for (punto in storedSet) {
                val parts = punto.split("|").map { it.trim() }
                if (parts.size == 7) {
                    val point = ExchangePoint(
                        tituloLibro = parts[0],
                        fecha = parts[1],
                        hora = parts[2],
                        lat = parts[3].toDoubleOrNull() ?: 0.0,
                        lon = parts[4].toDoubleOrNull() ?: 0.0,
                        estadoLibro = parts[5],
                        portadaUrl = parts[6],
                        direccion = obtenerDireccionDesdeLatLon(parts[3].toDoubleOrNull(), parts[4].toDoubleOrNull())
                    )

                    exchangePoints.add(point)
                }
            }

            val adapter = ExchangePointAdapter(exchangePoints) { selectedPoint ->
                val selectedBook = intent.getSerializableExtra("selectedBook") as? UserBook

                if (selectedBook != null) {
                    val intent = Intent(this, ExchangeSummaryActivity::class.java).apply {
                        putExtra("selectedBook", selectedBook)
                        putExtra("titulo", selectedPoint.tituloLibro)
                        putExtra("direccion", selectedPoint.direccion)
                        putExtra("fecha", selectedPoint.fecha)
                        putExtra("hora", selectedPoint.hora)
                        putExtra("lat", selectedPoint.lat)
                        putExtra("lon", selectedPoint.lon)
                        putExtra("estadoLibroDisponible", selectedPoint.estadoLibro)
                        putExtra("portadaLibroDisponible", selectedPoint.portadaUrl)

                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No se recibió información del libro", Toast.LENGTH_SHORT).show()
                }
            }




            binding.rvExchangePoints.adapter = adapter

        } else {
            Toast.makeText(this, "No hay puntos de intercambio registrados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerDireccionDesdeLatLon(lat: Double?, lon: Double?): String {
        return if (lat != null && lon != null) {
            try {
                val geocoder = android.location.Geocoder(this)
                val result = geocoder.getFromLocation(lat, lon, 1)
                result?.firstOrNull()?.getAddressLine(0) ?: "Dirección desconocida"
            } catch (e: Exception) {
                "Dirección desconocida"
            }
        } else {
            "Dirección desconocida"
        }
    }
}
