package com.example.icm_proyecto01

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_GALLERY
import com.example.icm_proyecto01.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("userName") ?: ""
        binding.etUserName.setText(userName)

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val savedImageUri = sharedPref.getString("profileImageUri", null)
        if (savedImageUri != null) {
            binding.profileImage.setImageURI(Uri.parse(savedImageUri))
        }

        binding.btnSaveProfile.setOnClickListener {
            val newUserName = binding.etUserName.text.toString()

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", newUserName)
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener{
            val userNameBack = intent.getStringExtra("userName") ?: ""
            binding.etUserName.setText(userName)
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userNameBack)
            startActivity(intent)
        }

        binding.btnChooseGallery.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
                    -> {
                    abrirGaleria()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    pedirPermiso(this, android.Manifest.permission.READ_EXTERNAL_STORAGE, "", PERMISSION_GALLERY)
                }
                else -> {
                    pedirPermiso(this, android.Manifest.permission.READ_EXTERNAL_STORAGE, "", PERMISSION_GALLERY)
                }
            }
        }


    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PERMISSION_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_GALLERY && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                binding.profileImage.setImageURI(imageUri) // Muestra la imagen en la UI

                // Guardar la URI en SharedPreferences
                val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("profileImageUri", imageUri.toString())
                    apply()
                }
            }
        }
    }





    private fun pedirPermiso(context: Activity, permiso: String, justificacion: String, idCode: Int) {
        if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permiso), idCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_GALLERY -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    abrirGaleria()
                } else {
                    Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}