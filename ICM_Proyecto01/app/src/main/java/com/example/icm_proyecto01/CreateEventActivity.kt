package com.example.icm_proyecto01

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityCreateEventBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de regreso
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Selector de Fecha
        binding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                binding.tvSelectedDate.text = "Fecha: $day/${month + 1}/$year"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        // Selector de Hora
        binding.btnSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog(this, { _, hour, minute ->
                binding.tvSelectedTime.text = "Hora: $hour:$minute"
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }

        // Botón para Crear Evento
        binding.btnCreateEvent.setOnClickListener {
            val name = binding.etEventName.text.toString().trim()
            val location = binding.etEventLocation.text.toString().trim()
            val date = binding.tvSelectedDate.text.toString().replace("Fecha: ", "").trim()
            val time = binding.tvSelectedTime.text.toString().replace("Hora: ", "").trim()
            val description = binding.etEventDescription.text.toString().trim()

            if (name.isEmpty() || location.isEmpty() || date == "No seleccionada" || time == "No seleccionada" || description.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                saveEventToJSON(name, location, "$date - $time", description)
                Toast.makeText(this, "Evento creado con éxito", Toast.LENGTH_SHORT).show()

                binding.btnCreateEvent.postDelayed({
                    startActivity(Intent(this, ExploreActivity::class.java))
                    finish()
                }, 1000)
            }
        }
    }
    private fun saveEventToJSON(name: String, location: String, date: String, description: String) {
        val sharedPreferences = getSharedPreferences("EventsData", Context.MODE_PRIVATE)
        val eventsJsonString = sharedPreferences.getString("events", "[]")
        val eventsArray = JSONArray(eventsJsonString)

        // Crear evento en JSON
        val newEvent = JSONObject().apply {
            put("name", name)
            put("location", location)
            put("date", date)
            put("description", description)
        }

        eventsArray.put(newEvent)

        with(sharedPreferences.edit()) {
            putString("events", eventsArray.toString())
            apply()
        }
    }
}
