package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageAnalysis
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.icm_proyecto01.model.UserBook
import com.google.common.util.concurrent.ListenableFuture

class SearchHiddenBookActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_hidden_book)

        previewView = findViewById(R.id.previewView)
        val userBook = intent.getSerializableExtra("USER_BOOK") as? UserBook
        Log.d("CHECK_USERBOOK", "Recibido en SearchHiddenBook: $userBook")

        // Configurar cámara
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            imageAnalysis = ImageAnalysis.Builder().build()

            camera = cameraProvider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this))


        // Botón: "Libro en AR"
        val btnViewAR: Button = findViewById(R.id.btnViewAR)
        btnViewAR.setOnClickListener {
            val userBook = intent.getSerializableExtra("USER_BOOK") as? UserBook
            if (userBook != null) {
                Log.d("ARDebug", "Pasando a ARBookActivity con libro: ${userBook.titulo}")
                val intent = Intent(this, ARBookActivity::class.java)
                intent.putExtra("USER_BOOK", userBook)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No se pudo cargar el libro en AR", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
