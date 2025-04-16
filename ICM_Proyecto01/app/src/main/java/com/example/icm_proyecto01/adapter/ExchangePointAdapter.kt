package com.example.icm_proyecto01.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.R
import com.example.icm_proyecto01.model.ExchangePoint

class ExchangePointAdapter(
    private val points: List<ExchangePoint>,
    private val onItemClick: (ExchangePoint) -> Unit
) : RecyclerView.Adapter<ExchangePointAdapter.ExchangePointViewHolder>() {

    inner class ExchangePointViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        private val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        private val tvBook: TextView = view.findViewById(R.id.tvBookInfo)

        fun bind(point: ExchangePoint) {
            tvLocation.text = point.direccion
            tvDateTime.text = "${point.fecha} - ${point.hora}"
            tvBook.text = "Libro: ${point.tituloLibro}"

            itemView.setOnClickListener { onItemClick(point) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangePointViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exchange_point, parent, false)
        return ExchangePointViewHolder(view)
    }

    override fun getItemCount(): Int = points.size

    override fun onBindViewHolder(holder: ExchangePointViewHolder, position: Int) {
        holder.bind(points[position])
    }
}
