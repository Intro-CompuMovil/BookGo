package com.example.icm_proyecto01.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.ExchangeManager
import com.example.icm_proyecto01.databinding.ItemBookOfferBinding
import com.example.icm_proyecto01.model.BookOffer
import com.example.icm_proyecto01.notifications.ExchangeNotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class BookOfferAdapter(
    private val offers: List<BookOffer>,
    private val exchangePointId: String,
    private val creatorUserId: String
) : RecyclerView.Adapter<BookOfferAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: ItemBookOfferBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = offers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offer = offers[position]

        with(holder.binding) {
            tvTitle.text = offer.titulo
            tvGenre.text = "Género: ${offer.genero}"
            tvEstado.text = "Estado: ${offer.estado}"

            if (offer.portadaUrl.isNotBlank()) {
                Picasso.get().load(offer.portadaUrl)
                    .placeholder(com.example.icm_proyecto01.R.drawable.default_book)
                    .into(ivCover)
            } else {
                ivCover.setImageResource(com.example.icm_proyecto01.R.drawable.default_book)
            }
            btnAccept.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Confirmar intercambio")
                    .setMessage("¿Estás seguro de aceptar este libro?")
                    .setPositiveButton("Aceptar") { _, _ ->
                        ExchangeManager.aceptarOferta(offer, exchangePointId, holder.itemView.context, creatorUserId)

                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            btnReject.setOnClickListener {
                ExchangeManager.rechazarOferta(offer, exchangePointId, holder.itemView.context)
            }



        }
    }
}
