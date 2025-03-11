package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BookSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_search)

        val btnFoundBook = findViewById<Button>(R.id.btnFoundBook)

        btnFoundBook.setOnClickListener {
            // Mostrar mensaje de confirmación
            Toast.makeText(this, "¡Libro oculto encontrado!", Toast.LENGTH_SHORT).show()

            // Regresar a la pantalla de perfil después del Toast
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish() // Cierra esta actividad para evitar que el usuario vuelva con "atrás"
        }
    }
}
