package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.databinding.ActivityExploreBinding

class ExploreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExploreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //eventos de ejemplo
        val eventList = listOf(
            Event("Intercambio de Ficción", "Biblioteca Central", "10 de Marzo - 3:00 PM"),
            Event("Feria de Libros Usados", "Parque del Libro", "12 de Marzo - 11:00 AM"),
            Event("Club de Lectura: Ciencia Ficción", "Café Literario", "15 de Marzo - 6:00 PM")
        )

        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.rvEvents.adapter = EventAdapter(eventList) { selectedEvent ->
            val intent = Intent(this, EventDetailActivity::class.java).apply {
                putExtra("EVENT_NAME", selectedEvent.name)
                putExtra("EVENT_LOCATION", selectedEvent.location)
                putExtra("EVENT_DATE", selectedEvent.date)
            }
            startActivity(intent)
        }

        // Configuración del menú inferior usando binding
        binding.bottomNavigation.selectedItemId = R.id.nav_explore

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish() // Cierra esta actividad para evitar duplicados
                    true
                }
                R.id.nav_explore -> {
                    // Ya estamos en Explorar, no hacer nada
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, MessagesActivity::class.java)) // Cuando esté lista
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}