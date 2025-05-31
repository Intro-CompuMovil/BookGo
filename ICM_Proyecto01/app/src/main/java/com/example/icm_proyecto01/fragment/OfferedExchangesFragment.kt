package com.example.icm_proyecto01.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        dbRef.child("ExchangePoints")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    offeredList.clear()
                    snapshot.children.forEach { point ->
                        val receiverId = point.child("receiverUserId").value.toString()
                        if (receiverId == userId) {
                            val rawBook = point.child("Book")
                            val bookId = rawBook.child("id").value.toString()
                            val state = rawBook.child("state").value.toString()
                            val date = point.child("date").value.toString().split("-")
                            val lat = point.child("lat").getValue(Double::class.java) ?: 0.0
                            val lon = point.child("lon").getValue(Double::class.java) ?: 0.0

                            val address = point.child("resolvedAddress").value?.toString()
                                ?: point.child("address").value?.toString()
                                ?: "Dirección no disponible"

                            val fecha = date.getOrNull(0)?.trim() ?: "-"
                            val hora = date.getOrNull(1)?.trim() ?: "-"

                            fetchBookFromGoogleApi(bookId) { titulo, portada ->
                                offeredList.add(
                                    ExchangePoint(
                                        tituloLibro = titulo,
                                        estadoLibro = state,
                                        fecha = fecha,
                                        hora = hora,
                                        lat = lat,
                                        lon = lon,
                                        portadaUrl = portada,
                                        direccion = address
                                    )
                                )
                                adapter.notifyItemInserted(offeredList.size - 1)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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
