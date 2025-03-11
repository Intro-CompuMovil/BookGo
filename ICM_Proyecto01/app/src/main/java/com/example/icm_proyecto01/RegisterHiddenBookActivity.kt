package com.example.icm_proyecto01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterHiddenBookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_hidden_book)

        val etBookTitle = findViewById<EditText>(R.id.etBookTitle)
        val etBookAuthor = findViewById<EditText>(R.id.etBookAuthor)
        val spinnerGenre = findViewById<Spinner>(R.id.spinnerGenre)
        val spinnerState = findViewById<Spinner>(R.id.spinnerState)
        val etBookLocation = findViewById<EditText>(R.id.etBookLocation)
        val btnRegisterBook = findViewById<Button>(R.id.btnRegisterBook)

        btnRegisterBook.setOnClickListener {
            val title = etBookTitle.text.toString().trim()
            val author = etBookAuthor.text.toString().trim()
            val genre = spinnerGenre.selectedItem.toString()
            val state = spinnerState.selectedItem.toString()
            val location = etBookLocation.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar libro oculto en SharedPreferences
            val sharedPref = getSharedPreferences("HiddenBooks", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            val bookData = "$title | $author | $genre | $state | $location"
            editor.putString(title, bookData)
            editor.apply()

            // Mostrar mensaje de confirmación y regresar inmediatamente a perfil
            Toast.makeText(this, "Libro oculto registrado correctamente", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish() // Cierra esta actividad
        }
    }
}
