package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityExchangeSummaryFinalBinding
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class ExchangeSummaryFinalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeSummaryFinalBinding
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExchangeSummaryFinalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val exchangePointId = intent.getStringExtra("exchangePointId") ?: ""
        val libroOfrecidoTitulo = intent.getStringExtra("libroOfrecidoTitulo") ?: "Sin título"
        val libroOfrecidoEstado = intent.getStringExtra("libroOfrecidoEstado") ?: "-"
        val libroOfrecidoPortada = intent.getStringExtra("libroOfrecidoPortada") ?: ""

        val direccion = intent.getStringExtra("direccion") ?: "Ubicación no disponible"
        val fecha = intent.getStringExtra("fecha") ?: "-"
        val hora = intent.getStringExtra("hora") ?: "-"


        binding.tvOfferedTitle.text = libroOfrecidoTitulo
        binding.tvOfferedState.text = "Estado: $libroOfrecidoEstado"
        if (libroOfrecidoPortada.isNotEmpty()) {
            Picasso.get().load(libroOfrecidoPortada).placeholder(R.drawable.default_book)
                .into(binding.imgOfferedCover)
        } else {
            binding.imgOfferedCover.setImageResource(R.drawable.default_book)
        }

        binding.tvAddress.text = direccion
        binding.tvDateTime.text = "$fecha - $hora"

        binding.btnComoLlegar.setOnClickListener {
            val url = "https://www.google.com/maps/search/?api=1&query=$direccion"
            val intent = Intent(this, ExchangeWebRouteActivity::class.java).apply {
                putExtra("url", url)
            }
            startActivity(intent)
        }

        binding.btnVolverInicio.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }


        Toast.makeText(this, "Intercambio confirmado. ¡Prepara tu libro!", Toast.LENGTH_LONG).show()
        cargarLibroOriginal(exchangePointId)
    }

    private fun cargarLibroOriginal(exchangePointId: String) {
        dbRef.child("ExchangePoints").child(exchangePointId).child("Book")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookId = snapshot.child("id").getValue(String::class.java)
                    val estado = snapshot.child("state").getValue(String::class.java) ?: "-"

                    if (bookId == null) {
                        Toast.makeText(this@ExchangeSummaryFinalActivity, "Error: libro original no encontrado", Toast.LENGTH_SHORT).show()
                        return
                    }

                    obtenerInfoGoogleBooks(bookId) { titulo, portadaUrl ->
                        runOnUiThread {
                            binding.tvOriginalTitle.text = titulo
                            binding.tvOriginalState.text = "Estado: $estado"


                            if (portadaUrl.isNotEmpty()) {
                                Picasso.get().load(portadaUrl).placeholder(R.drawable.default_book)
                                    .into(binding.imgOriginalCover)
                            } else {
                                binding.imgOriginalCover.setImageResource(R.drawable.default_book)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ExchangeSummaryFinalActivity, "Error al cargar libro original", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun obtenerInfoGoogleBooks(bookId: String, callback: (String, String) -> Unit) {
        thread {
            try {
                val url = "https://www.googleapis.com/books/v1/volumes/$bookId"
                val response = URL(url).readText()
                val json = JSONObject(response)
                val info = json.getJSONObject("volumeInfo")
                val titulo = info.optString("title", "Sin título")
                val imagen = info.optJSONObject("imageLinks")?.optString("thumbnail", "") ?: ""
                val imagenHttps = imagen.replace("http://", "https://")
                callback(titulo, imagenHttps)
            } catch (e: Exception) {
                callback("Sin título", "")
            }
        }
    }
}
