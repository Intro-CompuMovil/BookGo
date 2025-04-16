package com.example.icm_proyecto01

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_CAMERA
import com.example.icm_proyecto01.adapters.UserBooksAdapter
import com.example.icm_proyecto01.databinding.ActivityProfileBinding
import com.example.icm_proyecto01.model.UserBook
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private var fromExchange: Boolean = false
    private var exchangeData: Bundle? = null

    override fun onResume() {
        super.onResume()
        cargarLibrosUsuario()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verifica si viene desde punto de intercambio
        fromExchange = intent.getBooleanExtra("fromExchange", false)
        exchangeData = intent.extras  // contiene lat, lon, direccion, etc.

        val userName = intent.getStringExtra("userName") ?: "Jane Doe"
        binding.tvUserName.text = userName

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val savedImageUri = sharedPref.getString("profileImageUri", null)
        savedImageUri?.let {
            binding.profileImage.setImageURI(Uri.parse(it))
        }

        cargarLibrosUsuario()

        binding.tvEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java).apply {
                putExtra("userName", userName)
            })
        }

        binding.btnRegisterBook.setOnClickListener {
            startActivity(Intent(this, BookSearchActivity::class.java))
        }

        binding.btnRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }



        binding.btnSearchHiddenBook.setOnClickListener {
            startActivity(Intent(this, SearchHiddenBookActivity::class.java))
        }

        binding.profileImage.setOnClickListener {
            if (checkCameraPermission()) openCamera()
            else requestCameraPermission()
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_profile
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, MessagesActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun cargarLibrosUsuario() {
        val sharedPref = getSharedPreferences("UserBooks", MODE_PRIVATE)
        val allBooks = sharedPref.all
        val userBooks = mutableListOf<UserBook>()

        for ((_, value) in allBooks) {
            val data = value as? String ?: continue
            val parts = data.split("|").map { it.trim() }

            if (parts.size >= 5) {
                val book = UserBook(
                    titulo = parts[0],
                    autor = parts[1],
                    genero = parts[2],
                    estado = parts[3],
                    portadaUrl = parts[4]
                )
                userBooks.add(book)
            }
        }

        val adapter = if (fromExchange) {
            UserBooksAdapter(userBooks) { selectedBook ->
                val intent = Intent(this, ExchangeSummaryActivity::class.java).apply {
                    putExtra("selectedBook", selectedBook)
                    putExtra("titulo", exchangeData?.getString("titulo"))
                    putExtra("direccion", exchangeData?.getString("direccion"))
                    putExtra("fecha", exchangeData?.getString("fecha"))
                    putExtra("hora", exchangeData?.getString("hora"))
                    putExtra("lat", exchangeData?.getDouble("lat") ?: 0.0)
                    putExtra("lon", exchangeData?.getDouble("lon") ?: 0.0)
                }
                startActivity(intent)
                finish()  // vuelve al flujo principal
            }
        } else {
            UserBooksAdapter(userBooks) { libroSeleccionado ->
                val intent = Intent(this, BookDetailActivity::class.java).apply {
                    putExtra("book", libroSeleccionado)
                }
                startActivity(intent)

            }
        }

        binding.booksScroll.layoutManager = LinearLayoutManager(this)
        binding.booksScroll.adapter = adapter
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), PERMISSION_CAMERA
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CAMERA &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                binding.profileImage.setImageBitmap(it)
                saveImageToStorage(it)
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun saveImageToStorage(bitmap: Bitmap) {
        val file = File(getExternalFilesDir(null), "profile_image.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        val imageUri = Uri.fromFile(file)
        getSharedPreferences("UserProfile", MODE_PRIVATE).edit()
            .putString("profileImageUri", imageUri.toString())
            .apply()
    }




}
