package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityEventDetailBinding

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("EVENT_NAME") ?: "Evento Desconocido"
        val location = intent.getStringExtra("EVENT_LOCATION") ?: "Ubicación no disponible"
        val date = intent.getStringExtra("EVENT_DATE") ?: "Fecha no disponible"
        val description = intent.getStringExtra("EVENT_DESCRIPTION") ?: "Descripción no disponible"

        binding.tvEventName.text = name
        binding.tvEventLocation.text = location
        binding.tvEventDate.text = date
        binding.tvEventDescription.text = description

        binding.btnAttend.setOnClickListener {
            Toast.makeText(this, "¡Te has registrado en $name!", Toast.LENGTH_SHORT).show()

            // Regresar a la lista de eventos después de 1 segundo
            binding.btnAttend.postDelayed({
                val intent = Intent(this, ExploreActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }, 1000)
        }

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
        }
    }
}
