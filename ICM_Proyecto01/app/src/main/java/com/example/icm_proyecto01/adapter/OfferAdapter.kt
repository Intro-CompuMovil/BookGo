package com.example.icm_proyecto01.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.ExchangeManager
import com.example.icm_proyecto01.databinding.ItemOfferBinding
import com.example.icm_proyecto01.model.BookOffer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class OfferAdapter(
    private val offers: List<BookOffer>,
    private val exchangePointId: String,
    private val creatorUserId: String
) : RecyclerView.Adapter<OfferAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: ItemOfferBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = offers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offer = offers[position]
        with(holder.binding) {
            tvBookId.text = "Libro ofrecido: ${offer.bookId}"
            tvEstado.text = "Estado: ${offer.estado}"
            tvTimestamp.text = "Ofrecido el: ${offer.timestamp}"

            btnAccept.setOnClickListener {
                ExchangeManager.aceptarOferta(offer, exchangePointId, holder.itemView.context, creatorUserId)

            }

            btnReject.setOnClickListener {
                ExchangeManager.rechazarOferta(offer, exchangePointId, holder.itemView.context)
            }

        }
    }
}

