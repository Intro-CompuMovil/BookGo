package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityExchangeSummaryBinding
import com.example.icm_proyecto01.model.UserBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class ExchangeSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExchangeSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Libros
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

        // Libro ofrecido por el usuario
        if (userBook != null) {
            binding.tvBookUserTitle.text = userBook.titulo
            //binding.tvBookUserAuthor.text = "Autor: ${userBook.autor}"
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

            if (idPunto != null && userBook != null) {
                val databaseRef = FirebaseDatabase.getInstance().reference.child("ExchangePoints").child(idPunto)

                val updates = mapOf(
                    "BookExchange/id" to userBook.id,
                    "BookExchange/state" to userBook.estado,
                    "receiverUserId" to FirebaseAuth.getInstance().currentUser?.uid
                )

                databaseRef.updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Intercambio confirmado exitosamente", Toast.LENGTH_SHORT).show()
                        finish() // o puedes navegar a otra pantalla si quieres
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al confirmar intercambio: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "No se pudo confirmar el intercambio", Toast.LENGTH_SHORT).show()
            }
        }





        binding.btnBack.setOnClickListener { finish() }
    }

}