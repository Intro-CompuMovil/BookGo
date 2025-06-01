package com.example.icm_proyecto01

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.model.BookOffer
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

import com.google.firebase.database.FirebaseDatabase

object ExchangeManager {


    fun rechazarOferta(
        offer: BookOffer,
        exchangePointId: String,
        context: Context,
        adapter: RecyclerView.Adapter<*>,
        offersList: MutableList<BookOffer>,
        position: Int
    ) {
        val dbRef = FirebaseDatabase.getInstance().reference
        if (offer.offerId.isBlank()) {
            Toast.makeText(context, "ID de la oferta no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        dbRef.child("BookOffers").child(exchangePointId).child(offer.offerId).removeValue()
            .addOnSuccessListener {
                offersList.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(context, "Oferta rechazada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al rechazar oferta", Toast.LENGTH_SHORT).show()
            }
    }



    fun aceptarOferta(
        offer: BookOffer,
        exchangePointId: String,
        context: Context,
        creatorUserId: String
    ) {
        val dbRef = FirebaseDatabase.getInstance().reference

        val updates = mapOf(
            "ExchangePoints/$exchangePointId/receiverUserId" to offer.userId,
            "ExchangePoints/$exchangePointId/BookExchange/id" to offer.bookId,
            "ExchangePoints/$exchangePointId/BookExchange/state" to offer.estado,
            "ExchangePoints/$exchangePointId/BookReceiver/id" to offer.bookId,
            "ExchangePoints/$exchangePointId/BookReceiver/state" to offer.estado,
            "ExchangePoints/$exchangePointId/BookReceiver/titulo" to offer.titulo,
            "ExchangePoints/$exchangePointId/BookReceiver/portadaUrl" to offer.portadaUrl
        )

        dbRef.updateChildren(updates).addOnSuccessListener {
            dbRef.child("BookOffers").child(exchangePointId).child(offer.bookId).removeValue()

            dbRef.child("ExchangePoints").child(exchangePointId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val bookOriginalId = snapshot.child("Book").child("id").getValue(String::class.java)
                        val bookExchangeId = offer.bookId
                        val receiverUserId = offer.userId
                        if (!bookOriginalId.isNullOrBlank()) {
                            dbRef.child("Users").child(creatorUserId).child("Books").child(bookOriginalId).removeValue()
                        }
                        if (!bookExchangeId.isNullOrBlank()) {
                            dbRef.child("Users").child(receiverUserId).child("Books").child(bookExchangeId).removeValue()
                        }

                        val originalTitulo = snapshot.child("Book").child("titulo").getValue(String::class.java) ?: "Sin título"
                        val originalEstado = snapshot.child("Book").child("state").getValue(String::class.java) ?: "Desconocido"
                        val originalPortada = snapshot.child("Book").child("portadaUrl").getValue(String::class.java) ?: ""

                        val direccion = snapshot.child("resolvedAddress").getValue(String::class.java)
                            ?: snapshot.child("address").getValue(String::class.java) ?: "Ubicación desconocida"
                        val fechaHora = snapshot.child("date").getValue(String::class.java) ?: "- -"
                        val fecha = fechaHora.split("-").getOrNull(0)?.trim() ?: "-"
                        val hora = fechaHora.split("-").getOrNull(1)?.trim() ?: "-"

                        val intent = Intent(context, ExchangeSummaryFinalActivity::class.java).apply {
                            putExtra("exchangePointId", exchangePointId)
                            putExtra("libroOriginalTitulo", originalTitulo)
                            putExtra("libroOriginalEstado", originalEstado)
                            putExtra("libroOriginalPortada", originalPortada)

                            putExtra("libroOfrecidoTitulo", offer.titulo)
                            putExtra("libroOfrecidoEstado", offer.estado)
                            putExtra("libroOfrecidoPortada", offer.portadaUrl)

                            putExtra("direccion", direccion)
                            putExtra("fecha", fecha)
                            putExtra("hora", hora)
                        }

                        context.startActivity(intent)
                        Toast.makeText(context, "Intercambio confirmado. Libros removidos.", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error al acceder al punto de intercambio", Toast.LENGTH_SHORT).show()
                    }
                })

        }.addOnFailureListener {
            Toast.makeText(context, "Error al confirmar intercambio", Toast.LENGTH_SHORT).show()
        }
    }

}
