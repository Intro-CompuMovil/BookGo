package com.example.icm_proyecto01

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityEventDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var assistPref: SharedPreferences

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

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val eventId = intent.getStringExtra("EVENT_ID") ?: return
        val eventRef = FirebaseDatabase.getInstance().getReference("Events").child(eventId)

        eventRef.child("participants").child(userId).get().addOnSuccessListener { snapshot ->
            val isParticipating = snapshot.exists()

            if (isParticipating) {
                binding.btnAttend.text = "Desasistir"
                binding.btnGoToEvent.visibility = View.VISIBLE
            } else {
                binding.btnAttend.text = "Asistir"
                binding.btnGoToEvent.visibility = View.GONE
            }
        }


        binding.btnAttend.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val eventId = intent.getStringExtra("EVENT_ID") ?: return@setOnClickListener
            val eventRef = FirebaseDatabase.getInstance().getReference("Events").child(eventId)

            eventRef.child("participants").child(userId).get().addOnSuccessListener { snapshot ->
                val isParticipating = snapshot.exists()

                if (!isParticipating) {
                    eventRef.child("participants").child(userId).setValue(true)
                    Toast.makeText(this, "¡Te has registrado en $name!", Toast.LENGTH_SHORT).show()
                    binding.btnAttend.text = "Desasistir"
                    binding.btnGoToEvent.visibility = View.VISIBLE
                } else {
                    eventRef.child("participants").child(userId).removeValue()
                    Toast.makeText(this, "Has cancelado tu asistencia a $name", Toast.LENGTH_SHORT).show()
                    binding.btnAttend.text = "Asistir"
                    binding.btnGoToEvent.visibility = View.GONE
                }
            }
        }


        binding.btnGoToEvent.setOnClickListener {
            val intent = Intent(this, MapToEventActivity::class.java)
            intent.putExtra("EVENT_NAME", name)
            intent.putExtra("EVENT_LOCATION", location)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ExploreActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
