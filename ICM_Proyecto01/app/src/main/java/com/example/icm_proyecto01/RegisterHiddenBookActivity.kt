package com.example.icm_proyecto01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityRegisterHiddenBookBinding

class RegisterHiddenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterHiddenBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterHiddenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registrar el libro oculto
        binding.btnRegisterBook.setOnClickListener {
            val title = binding.etBookTitle.text.toString().trim()
            val author = binding.etBookAuthor.text.toString().trim()
            val genre = binding.etBookGenre.text.toString()
            val state = binding.etBookState.text.toString()
            val location = binding.etBookLocation.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar libro en SharedPreferences
            val sharedPref = getSharedPreferences("HiddenBooks", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            val bookData = "$title | $author | $genre | $state | $location"
            editor.putString(title, bookData)
            editor.apply()

            // Confirmación y regreso al perfil
            Toast.makeText(this, "Libro oculto registrado correctamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Acción para volver al perfil
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
