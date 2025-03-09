package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val userText = binding.etUserName.text.toString()
            val passwordText = binding.etPassword.text.toString()
            val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("userName", userText) // Aquí podrías usar el nombre en vez del email si está disponible
                apply()
            }

            if (userText.isBlank() || passwordText.isBlank()){
                Toast.makeText(this, "Por favor ingrese todos los datos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val i = Intent(this, HomeActivity::class.java)
            i.putExtra("userName", userText)
            startActivity(i)
            finish()
        }

        binding.tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
