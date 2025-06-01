package com.example.icm_proyecto01

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapters.BookOfferAdapter
import com.example.icm_proyecto01.databinding.ActivityOffersBinding
import com.example.icm_proyecto01.model.BookOffer
import com.google.firebase.database.*

class OffersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOffersBinding
    private lateinit var adapter: BookOfferAdapter
    private val offersList = mutableListOf<BookOffer>()
    private val dbRef = FirebaseDatabase.getInstance().reference
    private var exchangePointId: String = ""
    private var creatorUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOffersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exchangePointId = intent.getStringExtra("EXCHANGE_POINT_ID") ?: ""
        Log.d("OffersActivity", "ExchangePointId recibido: $exchangePointId")

        dbRef.child("ExchangePoints").child(exchangePointId).child("creatorUserId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    creatorUserId = snapshot.getValue(String::class.java) ?: ""

                    Log.d("OffersActivity", "CreatorUserId: $creatorUserId")

                    adapter = BookOfferAdapter(offersList, exchangePointId, creatorUserId)
                    binding.rvBookOffers.layoutManager = LinearLayoutManager(this@OffersActivity)
                    binding.rvBookOffers.adapter = adapter

                    loadBookOffers()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OffersActivity", "Error obteniendo creatorUserId: ${error.message}")
                    Toast.makeText(
                        this@OffersActivity,
                        "Error al obtener el creador del punto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadBookOffers() {
        Log.d("OffersActivity", "Cargando ofertas para punto: $exchangePointId")

        dbRef.child("BookOffers").child(exchangePointId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    offersList.clear()
                    Log.d("OffersActivity", "Snapshot exists: ${snapshot.exists()}")

                    if (!snapshot.exists()) {
                        binding.tvNoOffers.visibility = View.VISIBLE
                        return
                    }

                    var count = 0
                    for (offerSnapshot in snapshot.children) {
                        val bookOffer = offerSnapshot.getValue(BookOffer::class.java)
                        if (bookOffer != null) {
                            bookOffer.offerId = offerSnapshot.key ?: ""
                            offersList.add(bookOffer)
                            Log.d("OffersActivity", "Oferta añadida: ${bookOffer.titulo}")
                            count++
                        } else {
                            Log.e("OffersActivity", "Error al convertir: ${offerSnapshot.key}")
                        }
                    }


                    binding.tvNoOffers.visibility = if (offersList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.notifyDataSetChanged()
                    Log.d("OffersActivity", "Total de ofertas cargadas: $count")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OffersActivity", "Error en Firebase: ${error.message}")
                    Toast.makeText(this@OffersActivity, "Error al cargar ofertas", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
