package com.example.icm_proyecto01.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapters.OfferedExchangesAdapter
import com.example.icm_proyecto01.databinding.FragmentOfferedExchangesBinding
import com.example.icm_proyecto01.model.ExchangePoint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
class OfferedExchangesFragment : Fragment() {

    private lateinit var binding: FragmentOfferedExchangesBinding
    private lateinit var adapter: OfferedExchangesAdapter
    private val offeredList = mutableListOf<ExchangePoint>()
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOfferedExchangesBinding.inflate(inflater, container, false)
        binding.rvOfferedExchanges.layoutManager = LinearLayoutManager(requireContext())
        adapter = OfferedExchangesAdapter(offeredList)
        binding.rvOfferedExchanges.adapter = adapter
        loadOfferedExchanges()
        return binding.root
    }

    private fun loadOfferedExchanges() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        offeredList.clear()

        dbRef.child("BookOffers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(bookOffersSnapshot: DataSnapshot) {
                var totalOffersFound = 0
                val totalExpected = contarTotalOfertasDelUsuario(bookOffersSnapshot, userId)

                if (totalExpected == 0) {
                    binding.tvEmptyMessage.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "No has ofrecido libros para intercambiar", Toast.LENGTH_SHORT).show()
                    return
                }

                for (exchangeSnapshot in bookOffersSnapshot.children) {
                    val exchangePointId = exchangeSnapshot.key ?: continue

                    for (offerSnapshot in exchangeSnapshot.children) {
                        val offerUserId = offerSnapshot.child("userId").value.toString()
                        val offerEstado = offerSnapshot.child("estado").value.toString()
                        val offerTitulo = offerSnapshot.child("titulo").value.toString()
                        val portadaUrl = offerSnapshot.child("portadaUrl").value.toString()

                        if (offerUserId == userId) {
                            dbRef.child("ExchangePoints").child(exchangePointId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(pointSnapshot: DataSnapshot) {
                                        totalOffersFound++
                                        val receiverId = pointSnapshot.child("receiverUserId").value.toString()
                                        val isAccepted = receiverId == userId

                                        val date = pointSnapshot.child("date").value.toString().split("-")
                                        val lat = pointSnapshot.child("lat").getValue(Double::class.java) ?: 0.0
                                        val lon = pointSnapshot.child("lon").getValue(Double::class.java) ?: 0.0
                                        val address = pointSnapshot.child("resolvedAddress").value?.toString()
                                            ?: pointSnapshot.child("address").value?.toString()
                                            ?: "Dirección no disponible"

                                        val fecha = date.getOrNull(0)?.trim() ?: "-"
                                        val hora = date.getOrNull(1)?.trim() ?: "-"

                                        offeredList.add(
                                            ExchangePoint(
                                                exchangePointId = exchangePointId,
                                                tituloLibro = offerTitulo,
                                                estadoLibro = offerEstado,
                                                fecha = fecha,
                                                hora = hora,
                                                lat = lat,
                                                lon = lon,
                                                portadaUrl = portadaUrl,
                                                direccion = address,
                                                receiverUserId = if (isAccepted) userId else ""
                                            )
                                        )

                                        if (totalOffersFound == totalExpected) {
                                            offeredList.sortByDescending { it.receiverUserId.isNotBlank() }
                                            adapter.notifyDataSetChanged()
                                            binding.tvEmptyMessage.visibility =
                                                if (offeredList.isEmpty()) View.VISIBLE else View.GONE
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
                }
            }


            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun contarTotalOfertasDelUsuario(snapshot: DataSnapshot, userId: String): Int {
        var count = 0
        for (exchangeSnapshot in snapshot.children) {
            for (offerSnapshot in exchangeSnapshot.children) {
                if (offerSnapshot.child("userId").value.toString() == userId) {
                    count++
                }
            }
        }
        return count
    }


    private fun fetchBookFromGoogleApi(bookId: String, callback: (String, String) -> Unit) {
        val url = "https://www.googleapis.com/books/v1/volumes/$bookId"
        val requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(requireContext())

        val request = com.android.volley.toolbox.JsonObjectRequest(
            com.android.volley.Request.Method.GET, url, null,
            { response ->
                try {
                    val volumeInfo = response.getJSONObject("volumeInfo")
                    val title = volumeInfo.optString("title", "Sin título")
                    val imageLinks = volumeInfo.optJSONObject("imageLinks")
                    val thumbnail = imageLinks?.optString("thumbnail")?.replace("http://", "https://") ?: ""
                    callback(title, thumbnail)
                } catch (e: Exception) {
                    callback("Sin título", "")
                }
            },
            {
                callback("Sin título", "")
            }
        )

        requestQueue.add(request)
    }
}
