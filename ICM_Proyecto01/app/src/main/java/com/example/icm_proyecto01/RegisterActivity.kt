package com.example.icm_proyecto01


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener{

            val nameText = binding.etNewUserName.text.toString()
            val emailText = binding.etEmail.text.toString()
            val passwordText = binding.etPassword.text.toString()
            val repetirPasswordText = binding.etRepetirPassword.text.toString()

            if (nameText.isBlank() || emailText.isBlank() || passwordText.isBlank() || repetirPasswordText.isBlank()){
                Toast.makeText(this, "Por favor ingrese todos los datos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            else if(passwordText != repetirPasswordText){
                Toast.makeText(this, "Las contraseñas no coinciden. Inténtelo de nuevo", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }
            val caja = Bundle()
            caja.putString("Usuario", nameText)
            caja.putString("Email", emailText)
            caja.putString("Password", passwordText)

            val i = Intent(this, PresentationActivity::class.java)
            i.putExtra("paquete", caja) // por ahora no tiene uso funcional, pero se guarda su intent para futuras funcionalidades

            startActivity(i)
            finish()
        }

        // Volver a LoginActivity si ya tiene cuenta
        binding.tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
