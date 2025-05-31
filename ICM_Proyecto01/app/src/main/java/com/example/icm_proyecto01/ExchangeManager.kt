package com.example.icm_proyecto01

import android.content.Context
import android.widget.Toast
import com.example.icm_proyecto01.model.BookOffer
import com.example.icm_proyecto01.notifications.ExchangeNotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object ExchangeManager {

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
            "ExchangePoints/$exchangePointId/BookReceiver/state" to offer.estado
        )

        dbRef.updateChildren(updates).addOnSuccessListener {
            dbRef.child("BookOffers").child(exchangePointId).child(offer.bookId).removeValue()

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid



            Toast.makeText(context, "Libro aceptado", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error al confirmar intercambio", Toast.LENGTH_SHORT).show()
        }
    }

    fun rechazarOferta(offer: BookOffer, exchangePointId: String, context: Context) {
        val dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("BookOffers").child(exchangePointId).child(offer.bookId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Oferta rechazada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al rechazar oferta", Toast.LENGTH_SHORT).show()
            }
    }

    fun obtenerCreatorUserId(
        exchangePointId: String,
        onResult: (String?) -> Unit
    ) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("ExchangePoints")
            .child(exchangePointId)
            .child("creatorUserId")

        dbRef.get().addOnSuccessListener { snapshot ->
            val creatorUserId = snapshot.getValue(String::class.java)
            onResult(creatorUserId)
        }.addOnFailureListener {
            onResult(null)
        }
    }

}
