package com.example.icm_proyecto01.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.ExchangeSummaryFinalActivity
import com.example.icm_proyecto01.R
import com.example.icm_proyecto01.databinding.ItemExchangeOfferedBinding
import com.example.icm_proyecto01.model.ExchangePoint
import com.squareup.picasso.Picasso

class OfferedExchangesAdapter(private val exchanges: List<ExchangePoint>) :
    RecyclerView.Adapter<OfferedExchangesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemExchangeOfferedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExchangeOfferedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = exchanges.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = exchanges[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvLocation.text = item.direccion
            tvDescription.text = "Libro: ${item.tituloLibro}\nEstado: ${item.estadoLibro}"
            tvDateTime.text = "${item.fecha} - ${item.hora}"

            if (item.portadaUrl.isNotBlank()) {
                Picasso.get().load(item.portadaUrl)
                    .placeholder(R.drawable.default_book)
                    .into(imgBookCover)
            } else {
                imgBookCover.setImageResource(R.drawable.default_book)
            }

            if (item.receiverUserId.isNotBlank()) {
                holder.itemView.setBackgroundResource(R.drawable.card_border_accepted)
                holder.itemView.setOnClickListener {
                    val intent = Intent(context, ExchangeSummaryFinalActivity::class.java).apply {
                        putExtra("exchangePointId", item.exchangePointId)
                        putExtra("libroOfrecidoTitulo", item.tituloLibro)
                        putExtra("libroOfrecidoEstado", item.estadoLibro)
                        putExtra("libroOfrecidoPortada", item.portadaUrl)
                        putExtra("direccion", item.direccion)
                        putExtra("fecha", item.fecha)
                        putExtra("hora", item.hora)
                    }
                    context.startActivity(intent)
                }
            } else {
                holder.itemView.setBackgroundResource(R.drawable.card_border_pending)
                holder.itemView.setOnClickListener {
                    Toast.makeText(context, "Tu oferta aún no ha sido aceptada.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
