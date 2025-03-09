package com.example.icm_proyecto01


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_BACKGROUND_LOCATION
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_FINE_LOCATION
import com.example.icm_proyecto01.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //manejo de binding:
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName")


        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userName", userName) // Reenviamos el nombre a ProfileActivity
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Imagen de perfil: Ir a la pantalla de perfil
        val profileImage = findViewById<ImageView>(R.id.profileImage)
        binding.profileImage.setOnClickListener {
          //  startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Botón "Crear Punto de Intercambio"
        binding.btnCreateExchangePoint.setOnClickListener {
            // startActivity(Intent(this, CreateExchangePointActivity::class.java)) // Aún no implementado
        }

        //dentro del onCreate

        // Pedir permiso de ubicación (en dos pasos)
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
            // Permiso de ubicación en primer plano concedido, ahora pedimos el de fondo si es necesario
            pedirPermisoBackground()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Mostrar UI educativa
                pedirPermiso(this, android.Manifest.permission.ACCESS_FINE_LOCATION, "", PERMISSION_FINE_LOCATION )

            }
            else -> {
                // You can directly ask for the permission.
                pedirPermiso(this, android.Manifest.permission.ACCESS_FINE_LOCATION, "", PERMISSION_FINE_LOCATION )
            }
        }
    }


    private fun pedirPermiso(context: Activity, permiso: String, justificacion: String,
                             idCode: Int) {
        if (ContextCompat.checkSelfPermission(context, permiso)
            != PackageManager.PERMISSION_GRANTED
        ) {
            //metodo de android
            requestPermissions(arrayOf(permiso), idCode)
        }

    }

    private fun pedirPermisoBackground() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            pedirPermiso(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION, "", PERMISSION_BACKGROUND_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {

            PERMISSION_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Ahora pedimos el permiso de background si es Android 10+
                    pedirPermisoBackground()
                }
            }

            PERMISSION_BACKGROUND_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permiso de ubicación en segundo plano concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Experiencia reducida sin ubicación en segundo plano", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}