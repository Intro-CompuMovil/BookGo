package com.example.icm_proyecto01

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityRewardsBinding

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        cargarRecompensas()
    }

    private fun cargarRecompensas() {
        // Obtener pasos guardados
        val sharedPref = getSharedPreferences("StepCounter", MODE_PRIVATE)
        val pasos = sharedPref.getInt("steps", 0)

        // Mostrar pasos dados
        binding.tvSteps.text = "Pasos dados: $pasos"

        // Aquí si quieres puedes hacer lógica tipo:
        // Si pasos > 5000 --> desbloquear una recompensa, etc...
    }
}
