package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityExchangeSummaryBinding
import com.example.icm_proyecto01.model.UserBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ExchangeSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExchangeSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val exchangePointId = intent.getStringExtra("exchangePointId") ?: ""
        val userBook = intent.getSerializableExtra("selectedBook") as? UserBook

        Log.d("ExchangeSummary", "exchangePointId recibido: $exchangePointId")
        if (userBook != null) Log.d("ExchangeSummary", "Libro ofrecido: ${userBook.titulo}")

        if (exchangePointId.isNotBlank()) {
            val dbRef = FirebaseDatabase.getInstance().reference
            val pointRef = dbRef.child("ExchangePoints").child(exchangePointId)

            // Cargar información del libro del punto
            dbRef.child("ExchangePoints").child(exchangePointId).child("Book")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val titulo = snapshot.child("titulo").getValue(String::class.java)
                        val estado = snapshot.child("state").getValue(String::class.java) ?: "Desconocido"
                        val portada = snapshot.child("portadaUrl").getValue(String::class.java)
                        val bookId = snapshot.child("id").getValue(String::class.java)

                        binding.tvBookExchangeState.text = "Estado: $estado"

                        if (!titulo.isNullOrBlank() && !portada.isNullOrBlank()) {
                            Log.d("ExchangeSummary", "Libro punto: $titulo ($estado)")
                            binding.tvBookExchangeTitle.text = titulo
                            Picasso.get().load(portada).placeholder(R.drawable.default_book)
                                .into(binding.bookImageExchange)
                        } else if (!bookId.isNullOrBlank()) {
                            Log.d("ExchangeSummary", "Consultando libro por ID desde Google Books: $bookId")
                            obtenerLibroDesdeGoogleBooks(bookId) { apiTitulo, apiPortada ->
                                binding.tvBookExchangeTitle.text = apiTitulo
                                Picasso.get().load(apiPortada).placeholder(R.drawable.default_book)
                                    .into(binding.bookImageExchange)
                            }
                        } else {
                            Log.w("ExchangeSummary", "No hay información del libro en Firebase")
                            binding.tvBookExchangeTitle.text = "Sin título"
                            binding.bookImageExchange.setImageResource(R.drawable.default_book)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ExchangeSummaryActivity, "Error al cargar el libro original", Toast.LENGTH_SHORT).show()
                    }
                })

            // Cargar información del punto (dirección, fecha, hora)
            Log.d("ExchangeSummary", "Cargando datos del punto de intercambio...")
            dbRef.child("ExchangePoints").child(exchangePointId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val direccion = snapshot.child("resolvedAddress").getValue(String::class.java)
                            ?: snapshot.child("address").getValue(String::class.java) ?: "Ubicación desconocida"
                        val fechaHora = snapshot.child("date").getValue(String::class.java) ?: "- -"
                        val fecha = fechaHora.split("-").getOrNull(0)?.trim() ?: "-"
                        val hora = fechaHora.split("-").getOrNull(1)?.trim() ?: "-"

                        Log.d("ExchangeSummary", "Punto: $direccion - $fecha $hora")

                        binding.tvExchangeLocation.text = direccion
                        binding.tvExchangeDateTime.text = "$fecha - $hora"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ExchangeSummaryActivity, "Error al cargar información del punto", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Mostrar el libro ofrecido por el usuario
        if (userBook != null) {
            binding.tvBookUserTitle.text = userBook.titulo
            binding.tvBookUserGenre.text = "Género: ${userBook.genero}"
            binding.tvBookUserState.text = "Estado: ${userBook.estado}"

            if (userBook.portadaUrl.isNotEmpty()) {
                Picasso.get().load(userBook.portadaUrl)
                    .placeholder(R.drawable.default_book)
                    .into(binding.bookImageUser)
            } else {
                binding.bookImageUser.setImageResource(R.drawable.default_book)
            }
        }

        // Confirmar intercambio (crear oferta)
        binding.btnConfirmExchange.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (exchangePointId.isNotEmpty() && userBook != null && userId != null) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val pointRef = dbRef.child("ExchangePoints").child(exchangePointId)

                val offerData = mapOf(
                    "userId" to userId,
                    "bookId" to userBook.id,
                    "estado" to userBook.estado,
                    "titulo" to userBook.titulo,
                    "portadaUrl" to userBook.portadaUrl,
                    "genero" to userBook.genero
                )

                val offerRef = dbRef.child("BookOffers").child(exchangePointId).push()

                offerRef.setValue(offerData)
                    .addOnSuccessListener {
                                Toast.makeText(this, "Intercambio confirmado exitosamente", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al confirmar intercambio", Toast.LENGTH_SHORT).show()
                            }

                    .addOnFailureListener {
                        Log.e("ExchangeSummary", "Error al guardar oferta: ${it.message}")
                        Toast.makeText(this, "Error al guardar oferta", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun obtenerLibroDesdeGoogleBooks(bookId: String, callback: (String, String) -> Unit) {
        val url = "https://www.googleapis.com/books/v1/volumes/$bookId"
        val requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(this)

        val request = com.android.volley.toolbox.JsonObjectRequest(
            com.android.volley.Request.Method.GET, url, null,
            { response ->
                try {
                    val volumeInfo = response.getJSONObject("volumeInfo")
                    val titulo = volumeInfo.getString("title")
                    val portada = volumeInfo
                        .getJSONObject("imageLinks")
                        .getString("thumbnail")
                        .replace("http://", "https://")

                    callback(titulo, portada)
                } catch (e: Exception) {
                    Log.e("GoogleBooksAPI", "Error parseando respuesta: ${e.message}")
                    callback("Título desconocido", "")
                }
            },
            { error ->
                Log.e("GoogleBooksAPI", "Error al consultar libro: ${error.message}")
                callback("Título desconocido", "")
            }
        )

        requestQueue.add(request)
    }
}
