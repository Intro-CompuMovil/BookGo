package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityEventDetailBinding

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventName = intent.getStringExtra("EVENT_NAME") ?: "Evento desconocido"
        val eventLocation = intent.getStringExtra("EVENT_LOCATION") ?: "Ubicación no disponible"
        val eventDate = intent.getStringExtra("EVENT_DATE") ?: "Fecha no disponible"

        binding.tvEventDetails.text = "$eventName\n$eventLocation\n$eventDate"

        binding.btnAttend.setOnClickListener {
            Toast.makeText(this, "¡Te has registrado en $eventName!", Toast.LENGTH_SHORT).show()

            // Esperar 1 segundo antes de regresar a la lista de eventos
            binding.btnAttend.postDelayed({
                val intent = Intent(this, ExploreActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Cierra las actividades anteriores
                startActivity(intent)
                finish()
            }, 1000)
        }
    }
}
