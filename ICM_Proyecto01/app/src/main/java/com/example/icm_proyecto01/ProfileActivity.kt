package com.example.icm_proyecto01

import UserRepository
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
import com.bumptech.glide.Glide
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_CAMERA
import com.example.icm_proyecto01.adapters.UserBooksAdapter
import com.example.icm_proyecto01.databinding.ActivityProfileBinding
import com.example.icm_proyecto01.model.UserBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var userRepository: UserRepository

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

        database = FirebaseDatabase.getInstance().reference

        fromExchange = intent.getBooleanExtra("fromExchange", false)
        exchangeData = intent.extras

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            database.child("Users").child(uid).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val userName = dataSnapshot.child("name").value.toString()
                    val email = dataSnapshot.child("email").value.toString()
                    val profilePicUrl = dataSnapshot.child("profilePictureUrl").value.toString()
                    val expValue = dataSnapshot.child("readerLvl").getValue(Int::class.java) ?: 0

                    val readerLevel = when {
                        expValue < 100 -> 1
                        expValue < 1000 -> 2
                        else -> 3
                    }

                    binding.tvUserName.text = userName
                    binding.tvUserEmail.text = email
                    binding.tvUserLevel.text = "Nivel de lector: $readerLevel"

                    Glide.with(this)
                        .load(profilePicUrl)
                        .placeholder(R.drawable.icono_perfil)
                        .error(R.drawable.icono_perfil)
                        .into(binding.profileImage)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }

        }

        binding.tvEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.btnRegisterBook.setOnClickListener {
            startActivity(Intent(this, BookSearchActivity::class.java))
        }

        binding.btnRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
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
                    startActivity(Intent(this, ShowHiddenBooksActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }

        cargarLibrosUsuario()
    }

    private fun cargarLibrosUsuario() {
        val repository = UserRepository(this)  // 👈 le pasas el Context (this)

        repository.fetchUserBooks { userBooks ->
            if (userBooks.isNotEmpty()) {
                val adapter = UserBooksAdapter(userBooks) { libroSeleccionado ->
                    val intent = Intent(this, BookDetailActivity::class.java).apply {
                        putExtra("book", libroSeleccionado)
                    }
                    startActivity(intent)
                }

                binding.booksScroll.layoutManager = LinearLayoutManager(this)
                binding.booksScroll.adapter = adapter
            } else {
                Toast.makeText(this, "No tienes libros aún", Toast.LENGTH_SHORT).show()
            }
        }
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
