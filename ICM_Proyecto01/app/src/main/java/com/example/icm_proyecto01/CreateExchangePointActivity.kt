package com.example.icm_proyecto01

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityCreateExchangePointBinding
import java.util.*

class CreateExchangePointActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateExchangePointBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateExchangePointBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Selector de fecha
        binding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                binding.tvSelectedDate.text = "Fecha: $selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day)

            datePicker.show()
        }

        // Selector de hora
        binding.btnSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                binding.tvSelectedTime.text = "Hora: $selectedHour:$selectedMinute"
            }, hour, minute, true)

            timePicker.show()
        }

        // Acción al confirmar
        binding.btnConfirm.setOnClickListener {
            val bookTitle = binding.etBookTitle.text.toString()
            val address = binding.etAddress.text.toString()
            val date = binding.tvSelectedDate.text.toString()
            val time = binding.tvSelectedTime.text.toString()

            if (bookTitle.isEmpty() || address.isEmpty() || date == "Fecha: No seleccionada" || time == "Hora: No seleccionada") {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Punto de intercambio creado!", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad
            }
        }
    }
}
