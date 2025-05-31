package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
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

        val userProfilePicUrl = intent.getStringExtra("userProfilePicUrl") ?: ""
        val userBook = intent.getSerializableExtra("selectedBook") as? UserBook
        val tituloIntercambio = intent.getStringExtra("libroOriginalTitulo") ?: "Sin título"
        val direccion = intent.getStringExtra("direccion") ?: "Sin dirección"
        val fecha = intent.getStringExtra("fecha") ?: "-"
        val hora = intent.getStringExtra("hora") ?: "-"
        val estadoLibroDisponible = intent.getStringExtra("libroOriginalEstado") ?: "-"
        val portadaLibroDisponible = intent.getStringExtra("libroOriginalPortada") ?: ""
        val libroOfrecidoTitulo = intent.getStringExtra("libroOfrecidoTitulo")
        val libroOfrecidoEstado = intent.getStringExtra("libroOfrecidoEstado")
        val libroOfrecidoPortada = intent.getStringExtra("libroOfrecidoPortada")
        val exchangePointId = intent.getStringExtra("exchangePointId") ?: ""
        val receiverUserId = intent.getStringExtra("receiverUserId") ?: ""

        binding.tvBookExchangeTitle.text = tituloIntercambio
        binding.tvBookExchangeState.text = "Estado: $estadoLibroDisponible"
        binding.tvExchangeLocation.text = direccion
        binding.tvExchangeDateTime.text = "$fecha - $hora"

        if (portadaLibroDisponible.isNotEmpty()) {
            Picasso.get().load(portadaLibroDisponible)
                .placeholder(R.drawable.default_book)
                .into(binding.bookImageExchange)
        } else {
            binding.bookImageExchange.setImageResource(R.drawable.default_book)
        }

        if (!libroOfrecidoTitulo.isNullOrBlank()) {
            binding.tvBookUserTitle.text = libroOfrecidoTitulo
            binding.tvBookUserState.text = "Estado: $libroOfrecidoEstado"
            if (!libroOfrecidoPortada.isNullOrBlank()) {
                Picasso.get().load(libroOfrecidoPortada)
                    .placeholder(R.drawable.default_book)
                    .into(binding.bookImageUser)
            } else {
                binding.bookImageUser.setImageResource(R.drawable.default_book)
            }
        } else if (receiverUserId.isNotBlank() && exchangePointId.isNotBlank()) {
            val dbRef = FirebaseDatabase.getInstance().reference
            dbRef.child("UserBook").child(exchangePointId).child(receiverUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val titulo = snapshot.child("titulo").getValue(String::class.java) ?: "Sin título"
                        val estado = snapshot.child("estado").getValue(String::class.java) ?: "Desconocido"
                        val portada = snapshot.child("portadaUrl").getValue(String::class.java) ?: ""

                        binding.tvBookUserTitle.text = titulo
                        binding.tvBookUserState.text = "Estado: $estado"
                        if (portada.isNotBlank()) {
                            Picasso.get().load(portada)
                                .placeholder(R.drawable.default_book)
                                .into(binding.bookImageUser)
                        } else {
                            binding.bookImageUser.setImageResource(R.drawable.default_book)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ExchangeSummaryActivity, "Error al cargar el libro ofrecido", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.btnConfirmExchange.setOnClickListener {
            val userBook = intent.getSerializableExtra("selectedBook") as? UserBook
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (exchangePointId.isNotEmpty() && userBook != null && userId != null) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val databaseRef = dbRef.child("ExchangePoints").child(exchangePointId)

                val offerData = mapOf(
                    "userId" to userId,
                    "bookId" to userBook.id,
                    "estado" to userBook.estado,
                    "titulo" to userBook.titulo,
                    "portadaUrl" to userBook.portadaUrl,
                    "genero" to userBook.genero
                )

                val bookOffersRef = dbRef.child("BookOffers").child(exchangePointId).push()

                bookOffersRef.setValue(offerData)
                    .addOnSuccessListener {
                        databaseRef.child("creatorUserId").get().addOnSuccessListener { snapshot ->
                            val creatorUserId = snapshot.getValue(String::class.java)

                            val updates = mapOf("receiverUserId" to userId)
                            databaseRef.updateChildren(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Intercambio confirmado exitosamente", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomeActivity::class.java))
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error al confirmar intercambio", Toast.LENGTH_SHORT).show()
                                }

                        }.addOnFailureListener {
                            Toast.makeText(this, "Error al obtener datos del creador", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar la oferta", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }
}
