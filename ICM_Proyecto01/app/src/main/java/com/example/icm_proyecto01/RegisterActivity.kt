package com.example.icm_proyecto01

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityRegisterBinding
import com.example.icm_proyecto01.helpers.StorageHelper
import com.example.icm_proyecto01.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val nombre = binding.etNewUserName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val repetirPassword = binding.etRepetirPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || repetirPassword.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repetirPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registrarUsuario(nombre, email, password)
        }

        binding.btnSelectImage.setOnClickListener {
            abrirGaleria()
        }

        binding.btnTakePhoto.setOnClickListener {
            abrirCamara()
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registrarUsuario(nombre: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        if (selectedImageUri != null) {
                            StorageHelper.uploadProfileImage(uid, selectedImageUri!!,
                                onSuccess = { urlImagen ->
                                    guardarEnDatabase(uid, nombre, email, password, urlImagen)
                                },
                                onFailure = {
                                    Toast.makeText(this, "Error subiendo imagen", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            guardarEnDatabase(uid, nombre, email, password, "")
                        }
                    }
                } else {
                    Toast.makeText(this, "Error al registrar usuario: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun guardarEnDatabase(uid: String, nombre: String, email: String, password: String, fotoUrl: String) {
        val database = FirebaseDatabase.getInstance().reference
        val user = User(
            uid = uid,
            nombre = nombre,
            correo = email,
            contraseña = password,
            fotoPerfilUrl = fotoUrl,
            readerLvl = 0,
            libros = emptyList()
        )

        database.child("Users").child(uid).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar en base de datos", Toast.LENGTH_SHORT).show()
            }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivProfilePicture.setImageURI(selectedImageUri)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                val file = File(cacheDir, "${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { output ->
                    it.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }
                selectedImageUri = Uri.fromFile(file)
                binding.ivProfilePicture.setImageURI(selectedImageUri)
            }
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }
}
