package com.example.icm_proyecto01


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //botoncitos
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        // Navegar a la pantalla principal
        btnRegister.setOnClickListener {
            val intent = Intent(this, PresentationActivity::class.java)
            startActivity(intent)
            finish() // Cierra esta actividad para evitar que el usuario regrese al registro
        }

        // Volver a LoginActivity si ya tiene cuenta
        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
