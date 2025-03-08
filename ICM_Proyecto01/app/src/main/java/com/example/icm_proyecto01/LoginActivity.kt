package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //manejo de binding:
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // boton de login
        binding.btnLogin.setOnClickListener {

            val emailText = binding.etEmail.text.toString()
            val passwordText = binding.etPassword.text.toString()

            if (emailText.isBlank() || passwordText.isBlank()){
                Toast.makeText(this, "Por favor ingrese todos los datos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            val caja = Bundle()
            caja.putString("Email", emailText)
            caja.putString("Password", passwordText)

            val i = Intent(this, HomeActivity::class.java)
            i.putExtra("paquete", caja) // por ahora no tiene uso funcional, pero se guarda su intent para futuras funcionalidades

            startActivity(i)
            finish() // Cierra LoginActivity para que no vuelva atrás
        }

        // se puede omitir
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
