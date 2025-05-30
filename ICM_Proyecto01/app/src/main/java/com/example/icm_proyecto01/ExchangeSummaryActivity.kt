package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityExchangeSummaryBinding
import com.example.icm_proyecto01.model.UserBook
import com.example.icm_proyecto01.notifications.ExchangeNotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class ExchangeSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExchangeSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userProfilePicUrl = intent.getStringExtra("userProfilePicUrl") ?: ""
        val userBook = intent.getSerializableExtra("selectedBook") as? UserBook
        val tituloIntercambio = intent.getStringExtra("titulo") ?: "Sin título"
        val direccion = intent.getStringExtra("direccion") ?: "Sin dirección"
        val fecha = intent.getStringExtra("fecha") ?: "-"
        val hora = intent.getStringExtra("hora") ?: "-"
        val estadoLibroDisponible = intent.getStringExtra("estadoLibroDisponible") ?: "-"
        val idPunto = intent.getStringExtra("idPunto")

        binding.tvBookExchangeTitle.text = tituloIntercambio
        binding.tvBookExchangeState.text = "Estado: $estadoLibroDisponible"
        binding.tvExchangeLocation.text = direccion
        binding.tvExchangeDateTime.text = "$fecha - $hora"

        if (userProfilePicUrl.isNotEmpty() && userProfilePicUrl != "null") {
            Picasso.get().load(userProfilePicUrl)
                .placeholder(R.drawable.default_book)
                .into(binding.bookImageExchange)
        } else {
            binding.bookImageExchange.setImageResource(R.drawable.default_book)
        }

        if (userBook != null) {
            binding.tvBookUserTitle.text = userBook.titulo
            binding.tvBookUserGenre.text = "Género: ${userBook.genero}"
            binding.tvBookUserState.text = "Estado: ${userBook.estado}"

            if (userBook.portadaUrl.isNotEmpty()) {
                Picasso.get().load(userBook.portadaUrl).placeholder(R.drawable.default_book)
                    .into(binding.bookImageUser)
            } else {
                binding.bookImageUser.setImageResource(R.drawable.default_book)
            }
        }

        binding.btnConfirmExchange.setOnClickListener {
            val idPunto = intent.getStringExtra("idPunto")
            val userBook = intent.getSerializableExtra("selectedBook") as? UserBook
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (idPunto != null && userBook != null && userId != null) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val databaseRef = dbRef.child("ExchangePoints").child(idPunto)

                databaseRef.child("creatorUserId").get().addOnSuccessListener { snapshot ->
                    val creatorUserId = snapshot.getValue(String::class.java)

                    if (!creatorUserId.isNullOrEmpty() && creatorUserId != userId) {
                        ExchangeNotificationManager.sendNotificationToUser(
                            userId = creatorUserId,
                            title = "Nuevo libro ofrecido",
                            message = "Un usuario ha ofrecido un libro para tu punto de intercambio.",
                            context = this
                        )
                    }

                    val updates = mapOf(
                        "BookExchange/id" to userBook.id,
                        "BookExchange/state" to userBook.estado,
                        "receiverUserId" to userId
                    )

                    databaseRef.updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Intercambio confirmado exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al confirmar intercambio: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al consultar el creador del punto", Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.btnBack.setOnClickListener { finish() }
        }
    }

