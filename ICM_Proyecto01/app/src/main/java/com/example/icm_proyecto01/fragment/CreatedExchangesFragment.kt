package com.example.icm_proyecto01.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapters.CreatedExchangesAdapter
import com.example.icm_proyecto01.databinding.FragmentCreatedExchangesBinding
import com.example.icm_proyecto01.model.ExchangePoint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CreatedExchangesFragment : Fragment() {

    private lateinit var binding: FragmentCreatedExchangesBinding
    private lateinit var adapter: CreatedExchangesAdapter
    private val exchangeList = mutableListOf<ExchangePoint>()
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatedExchangesBinding.inflate(inflater, container, false)
        binding.rvCreatedExchanges.layoutManager = LinearLayoutManager(requireContext())
        adapter = CreatedExchangesAdapter(exchangeList)
        binding.rvCreatedExchanges.adapter = adapter
        loadCreatedExchanges()
        return binding.root
    }

    private fun loadCreatedExchanges() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        dbRef.child("ExchangePoints").orderByChild("exchangeUserId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    exchangeList.clear()
                    snapshot.children.forEach { point ->
                        val pointId = point.key ?: return@forEach
                        val raw = point.child("Book")
                        val bookId = raw.child("id").value.toString()
                        val state = raw.child("state").value.toString()
                        val dateTime = point.child("date").value.toString().split("-")
                        val lat = point.child("lat").getValue(Double::class.java) ?: 0.0
                        val lon = point.child("lon").getValue(Double::class.java) ?: 0.0
                        val receiverUserId = point.child("receiverUserId").value?.toString() ?: ""


                        val address = point.child("resolvedAddress").value?.toString()
                            ?: point.child("address").value?.toString()
                            ?: "Dirección no disponible"

                        val fecha = dateTime.getOrNull(0)?.trim() ?: "-"
                        val hora = dateTime.getOrNull(1)?.trim() ?: "-"

                        fetchBookFromGoogleApi(bookId) { tituloLibro, portadaUrl ->
                            val receiverUserId = point.child("receiverUserId").value?.toString() ?: ""

                            exchangeList.add(
                                ExchangePoint(
                                    exchangePointId = pointId,
                                    tituloLibro = tituloLibro,
                                    estadoLibro = state,
                                    fecha = fecha,
                                    hora = hora,
                                    lat = lat,
                                    lon = lon,
                                    portadaUrl = portadaUrl,
                                    direccion = address,
                                    receiverUserId = receiverUserId
                                )
                            )
                            adapter.notifyItemInserted(exchangeList.size - 1)
                        }

                    }
                    adapter.notifyDataSetChanged()
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
