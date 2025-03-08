package com.example.icm_proyecto01


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //menu inferior principal
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Ya estamos en HomeActivity, no hacer nada
                    true
                }
                R.id.nav_explore -> {
                    // startActivity(Intent(this, ExploreActivity::class.java)) // Aún no implementado
                    true
                }
                R.id.nav_messages -> {
                    // startActivity(Intent(this, MessagesActivity::class.java)) // Aún no implementado
                    true
                }
                R.id.nav_profile -> {
                  //  startActivity(Intent(this, ProfileActivity::class.java)) // aun no
                    true
                }
                else -> false
            }
        }

        // Imagen de perfil: Ir a la pantalla de perfil
        val profileImage = findViewById<ImageView>(R.id.profileImage)
        profileImage.setOnClickListener {
          //  startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Botón "Crear Punto de Intercambio"
        val btnCreateExchangePoint = findViewById<Button>(R.id.btnCreateExchangePoint)
        btnCreateExchangePoint.setOnClickListener {
            // startActivity(Intent(this, CreateExchangePointActivity::class.java)) // Aún no implementado
        }
    }
}
