package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.HomeActivity
import androidx.camera.view.PreviewView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.LifecycleOwner
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

        // Inicializar la vista previa de la cámara
        previewView = findViewById(R.id.previewView)

        // Configurar la cámara
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Selección de la cámara trasera (o delantera si lo prefieres)
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            // Crear un preview de la cámara
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            // Inicializar ImageCapture y ImageAnalysis (si lo necesitas)
            imageCapture = ImageCapture.Builder().build()
            imageAnalysis = ImageAnalysis.Builder().build()

            // Unir el uso de la cámara al ciclo de vida de la actividad
            camera = cameraProvider.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis
            )

        }, ContextCompat.getMainExecutor(this))

        // Configurar el botón "Libro encontrado"
        val btnFoundBook: Button = findViewById(R.id.btnFoundBook)
        btnFoundBook.setOnClickListener {
            // Mostrar el Toast
            Toast.makeText(this, "Libro encontrado", Toast.LENGTH_SHORT).show()

            // Navegar a HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            // Finalizar la actividad actual para evitar que el usuario regrese a esta pantalla
            finish()
        }
    }
}
