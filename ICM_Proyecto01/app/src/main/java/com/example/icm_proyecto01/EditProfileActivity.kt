package com.example.icm_proyecto01

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.icm_proyecto01.databinding.ActivityEditProfileBinding
import com.example.icm_proyecto01.helpers.StorageHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatosUsuario()

        binding.btnSaveProfile.setOnClickListener { guardarCambios() }
        binding.btnBack.setOnClickListener { volver() }

        binding.btnChooseGallery.setOnClickListener { abrirGaleria() }
        binding.btnTakePhoto.setOnClickListener { abrirCamara() }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

    }

    private fun cargarDatosUsuario() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (currentUser != null && userId != null) {
            binding.etUserEmail.setText(currentUser.email)

            val database = FirebaseDatabase.getInstance().reference
            database.child("Users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    val nombre = snapshot.child("name").getValue(String::class.java)
                    val fotoUrl = snapshot.child("profilePictureUrl").getValue(String::class.java)

                    binding.etUserName.setText(nombre ?: "")

                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.icono_perfil)
                            .error(R.drawable.icono_perfil)
                            .into(binding.profileImage)
                    } else {
                        binding.profileImage.setImageResource(R.drawable.icono_perfil)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    binding.profileImage.setImageResource(R.drawable.icono_perfil)
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            binding.profileImage.setImageResource(R.drawable.icono_perfil)
        }
    }


    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePhotoLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                binding.profileImage.setImageURI(selectedImageUri)
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    val file = File(cacheDir, "${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { output ->
                        it.compress(Bitmap.CompressFormat.JPEG, 100, output)
                    }
                    selectedImageUri = Uri.fromFile(file)
                    binding.profileImage.setImageURI(selectedImageUri)
                }
            }
        }

    private fun guardarCambios() {
        val newUserName = binding.etUserName.text.toString().trim()
        val newUserEmail = binding.etUserEmail.text.toString().trim()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (newUserName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }
        if (newUserEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUser != null && userId != null) {
            val database = FirebaseDatabase.getInstance().reference
            val updates = mutableMapOf<String, Any>("name" to newUserName)
            if (selectedImageUri != null) {
                StorageHelper.uploadProfileImage(
                    userId, selectedImageUri!!,
                    onSuccess = { imageUrl ->
                        updates["profilePictureUrl"] = imageUrl
                        actualizarFirebase(database, userId, updates, currentUser, newUserEmail)
                    },
                    onFailure = {
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                actualizarFirebase(database, userId, updates, currentUser, newUserEmail)
            }
        }
    }

    private fun actualizarFirebase(
        database: DatabaseReference,
        userId: String,
        updates: Map<String, Any>,
        currentUser: FirebaseUser,
        newUserEmail: String
    ) {
        database.child("Users").child(userId).updateChildren(updates)
            .addOnSuccessListener {
                if (newUserEmail != currentUser.email) {
                    currentUser.updateEmail(newUserEmail)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            volver()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al actualizar correo", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    volver()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun volver() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
