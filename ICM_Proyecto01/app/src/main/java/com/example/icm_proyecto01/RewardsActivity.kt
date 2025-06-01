package com.example.icm_proyecto01

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityRewardsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


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
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance()

        val userRef = db.getReference("Users").child(uid)
        userRef.child("readerLvl").get().addOnSuccessListener { snapshot ->
            val pasos = snapshot.getValue(Int::class.java) ?: 0


            val limite1 = 100
            val limite2 = 1000
            val limite3 = 5000

            val (nivelActual, pasosParaSiguienteNivel) = when {
                pasos < limite1 -> Pair(1, limite1)
                pasos < limite2 -> Pair(2, limite2)
                pasos < limite3 -> Pair(3, limite3)
                else -> Pair(4, pasos)
            }

            val progreso = when (nivelActual) {
                1 -> (pasos * 100) / limite1
                2 -> ((pasos - limite1) * 100) / (limite2 - limite1)
                3 -> ((pasos - limite2) * 100) / (limite3 - limite2)
                else -> 100
            }

            binding.progressBar1.progress = progreso
            binding.tvProgress1.text = "Nivel $nivelActual - $pasos/$pasosParaSiguienteNivel pasos"

        }.addOnFailureListener {
            Toast.makeText(this, "No se pudieron cargar los pasos", Toast.LENGTH_SHORT).show()
        }

        val booksRef = db.getReference("Users").child(uid).child("Books")
        booksRef.get().addOnSuccessListener { snapshot ->
            val totalLibros = snapshot.childrenCount.toInt()
            val metaLibros = 10
            val progresoLibros = (totalLibros * 100) / metaLibros

            binding.tvProgressBooks.text = "$totalLibros/$metaLibros"
            binding.progressBarBooks.progress = progresoLibros.coerceAtMost(100)


            if (totalLibros >= metaLibros) {
                Toast.makeText(this, "¡Felicidades! Has agregado $metaLibros libros 🎉", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "No se pudieron cargar los libros", Toast.LENGTH_SHORT).show()
        }
    }

}
