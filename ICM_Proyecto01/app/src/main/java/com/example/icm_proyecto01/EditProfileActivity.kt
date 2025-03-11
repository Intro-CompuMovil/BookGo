package com.example.icm_proyecto01

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_GALLERY
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_CAMERA
import com.example.icm_proyecto01.databinding.ActivityEditProfileBinding
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val userName = sharedPref.getString("userName", "Jane Doe")
        binding.etUserName.setText(userName)

        val savedImageUri = sharedPref.getString("profileImageUri", null)
        if (savedImageUri != null) {
            binding.profileImage.setImageURI(Uri.parse(savedImageUri))
        }

        binding.btnSaveProfile.setOnClickListener {
            val newUserName = binding.etUserName.text.toString()
            with(sharedPref.edit()) {
                putString("userName", newUserName)
                apply()
            }

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", newUserName)
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            val userNameBack = intent.getStringExtra("userName") ?: ""
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userNameBack)
            startActivity(intent)
        }

        binding.btnChooseGallery.setOnClickListener {
            handlePermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                Manifest.permission.READ_MEDIA_IMAGES,
                PERMISSION_GALLERY,
                galleryLauncher
            )
        }

        binding.btnTakePhoto.setOnClickListener {
            handlePermission(
                Manifest.permission.CAMERA,
                false,
                "",
                PERMISSION_CAMERA,
                cameraLauncher
            )
        }
    }


    private fun handlePermission(
        permission: String,
        hasAlternativePermission: Boolean,
        alternativePermission: String,
        requestCode: Int,
        launcher: androidx.activity.result.ActivityResultLauncher<Intent>
    ) {
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED ||
                    (hasAlternativePermission && ContextCompat.checkSelfPermission(this, alternativePermission) == PackageManager.PERMISSION_GRANTED) -> {
                when (requestCode) {
                    PERMISSION_GALLERY -> openGallery()
                    PERMISSION_CAMERA -> openCamera()
                }
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    binding.profileImage.setImageURI(it)
                    saveImageUri(it)
                }
            }
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


    private fun saveImageToStorage(bitmap: Bitmap) {
        val file = File(getExternalFilesDir(null), "profile_image.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        val imageUri = Uri.fromFile(file)
        saveImageUri(imageUri)
    }

    private fun saveImageUri(uri: Uri) {
        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE).edit()
        sharedPref.putString("profileImageUri", uri.toString())
        sharedPref.apply()
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_GALLERY -> openGallery()
                PERMISSION_CAMERA -> openCamera()
            }
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }
}
