package com.example.icm_proyecto01


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_ACTIVITY_RECOGNITION
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_BACKGROUND_LOCATION
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_FINE_LOCATION
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_MULTIPLE
import com.example.icm_proyecto01.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName")

        cargarImagenDePerfil()

        binding.profileImage.setOnClickListener {

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userName) // Reenviamos el nombre a ProfileActivity
            startActivity(intent)
        }

        // Botón "Crear Punto de Intercambio"
        binding.btnCreateExchangePoint.setOnClickListener {
            startActivity(Intent(this, CreateExchangePointActivity::class.java))
        }


        //BARRA DE NAVEGACION
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



        // PEDIR PERMISOS
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED -> {
                // Si ambos permisos están concedidos, iniciar tracking de ubicación y pasos
                //iniciarConteoPasos()
            }
            else -> {
                // Pedir ambos permisos juntos en un solo request
                pedirPermiso(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACTIVITY_RECOGNITION
                    ),
                    PERMISSION_MULTIPLE
                )
            }
        }





    }


    private fun cargarImagenDePerfil() {
        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val savedImageUri = sharedPref.getString("profileImageUri", null)

        if (savedImageUri != null) {
            binding.profileImage.setImageURI(Uri.parse(savedImageUri))
        }
    }

    private fun pedirPermiso(context: Activity, permisos: Array<String>, idCode: Int) {
        if (permisos.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(context, permisos, idCode)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_MULTIPLE) {
            var permisoUbicacionConcedido = false
            var permisoPasosConcedido = false

            for (i in permissions.indices) {
                when (permissions[i]) {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            permisoUbicacionConcedido = true
                        }
                    }

                    android.Manifest.permission.ACTIVITY_RECOGNITION -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            permisoPasosConcedido = true
                        }
                    }
                }
            }

            if (permisoUbicacionConcedido && permisoPasosConcedido) {
                //iniciarConteoPasos()
            } else {
                if (permisoUbicacionConcedido) {
                    binding.mapView.setImageResource(R.drawable.mapita)
                } else {
                    binding.mapView.setImageResource(R.drawable.gray_map)
                    Toast.makeText(this, "Permiso de ubicación requerido para esta función", Toast.LENGTH_SHORT).show()
                }
                if (!permisoPasosConcedido) {
                    Toast.makeText(
                        this,
                        "Permiso de actividad física requerido para rastrear pasos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }





}