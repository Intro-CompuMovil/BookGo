package com.example.icm_proyecto01.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.OffersActivity
import com.example.icm_proyecto01.databinding.ItemExchangeCreatedBinding
import com.example.icm_proyecto01.model.ExchangePoint
import com.squareup.picasso.Picasso


class CreatedExchangesAdapter(private val exchanges: List<ExchangePoint>) :
    RecyclerView.Adapter<CreatedExchangesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemExchangeCreatedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExchangeCreatedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                Picasso.get()
                    .load(item.portadaUrl)
                    .placeholder(com.example.icm_proyecto01.R.drawable.default_book)
                    .into(imgBookCover)
            } else {
                imgBookCover.setImageResource(com.example.icm_proyecto01.R.drawable.default_book)
            }

            root.setOnClickListener {
                val intent = Intent(holder.itemView.context, OffersActivity::class.java)
                intent.putExtra("EXCHANGE_POINT_ID", item.exchangePointId) // <- corregido aquí
                holder.itemView.context.startActivity(intent)
            }
        }
    }
}
