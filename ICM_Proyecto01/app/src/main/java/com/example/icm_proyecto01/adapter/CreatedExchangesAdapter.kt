package com.example.icm_proyecto01.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.ExchangeSummaryFinalActivity
import com.example.icm_proyecto01.OffersActivity
import com.example.icm_proyecto01.databinding.ItemExchangeCreatedBinding
import com.example.icm_proyecto01.model.ExchangePoint
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


class CreatedExchangesAdapter(private val exchanges: List<ExchangePoint>) :
    RecyclerView.Adapter<CreatedExchangesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemExchangeCreatedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemExchangeCreatedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = exchanges.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = exchanges[position]

        with(holder.binding) {
            tvLocation.text = item.direccion
            tvDescription.text = "Libro: ${item.tituloLibro}\nEstado: ${item.estadoLibro}"
            tvDateTime.text = "${item.fecha} - ${item.hora}"

            if (item.portadaUrl.isNotBlank()) {
                Picasso.get().load(item.portadaUrl)
                    .placeholder(com.example.icm_proyecto01.R.drawable.default_book)
                    .into(imgBookCover)
            } else {
                imgBookCover.setImageResource(com.example.icm_proyecto01.R.drawable.default_book)
            }

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val exchangePointId = item.exchangePointId

                if (item.receiverUserId.isNotBlank()) {
                    val dbRef = FirebaseDatabase.getInstance().reference
                    dbRef.child("ExchangePoints").child(exchangePointId).child("BookReceiver")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val libroOfrecidoId = snapshot.child("id").getValue(String::class.java) ?: ""
                                val libroOfrecidoEstado = snapshot.child("state").getValue(String::class.java) ?: ""

                                if (libroOfrecidoId.isNotBlank()) {
                                    val url = "https://www.googleapis.com/books/v1/volumes/$libroOfrecidoId"
                                    val requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context)

                                    val request = com.android.volley.toolbox.JsonObjectRequest(
                                        com.android.volley.Request.Method.GET, url, null,
                                        { response ->
                                            val volumeInfo = response.optJSONObject("volumeInfo")
                                            val tituloOfrecido = volumeInfo?.optString("title") ?: "Sin título"
                                            val portadaUrl = volumeInfo?.optJSONObject("imageLinks")
                                                ?.optString("thumbnail")?.replace("http://", "https://") ?: ""

                                            val intent = Intent(context, ExchangeSummaryFinalActivity::class.java).apply {
                                                putExtra("exchangePointId", item.exchangePointId)
                                                putExtra("libroOriginalTitulo", item.tituloLibro)
                                                putExtra("libroOriginalEstado", item.estadoLibro)
                                                putExtra("libroOriginalPortada", item.portadaUrl)

                                                putExtra("libroOfrecidoTitulo", tituloOfrecido)
                                                putExtra("libroOfrecidoEstado", libroOfrecidoEstado)
                                                putExtra("libroOfrecidoPortada", portadaUrl)

                                                putExtra("direccion", item.direccion)
                                                putExtra("fecha", item.fecha)
                                                putExtra("hora", item.hora)
                                            }
                                            context.startActivity(intent)
                                        },
                                        {
                                            Toast.makeText(context, "Error al obtener libro ofrecido", Toast.LENGTH_SHORT).show()
                                        }
                                    )

                                    requestQueue.add(request)
                                } else {
                                    Toast.makeText(context, "No hay libro ofrecido", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "Error al consultar libro ofrecido", Toast.LENGTH_SHORT).show()
                            }
                        })
                } else {
                    Toast.makeText(context, "Aún no se ha aceptado ninguna oferta", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}