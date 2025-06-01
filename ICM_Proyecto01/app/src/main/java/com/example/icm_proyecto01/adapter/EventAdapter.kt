package com.example.icm_proyecto01.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.R
import com.example.icm_proyecto01.model.Event
import com.google.firebase.auth.FirebaseAuth

class EventAdapter(
    private val eventList: List<Event>,
    private val onClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.bind(event, onClick)
    }

    override fun getItemCount() = eventList.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvEventName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvEventLocation)
        private val tvDate: TextView = itemView.findViewById(R.id.tvEventDate)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
        private val tvParticipants: TextView = itemView.findViewById(R.id.tvEventParticipants)
        private val btnDetails: Button = itemView.findViewById(R.id.btnDetails)
        private val cardView: CardView = itemView as CardView

        fun bind(event: Event, onClick: (Event) -> Unit) {
            tvName.text = event.name
            tvLocation.text = event.location
            tvDate.text = event.date
            tvDescription.text = event.description

            val count = event.participants.size
            tvParticipants.text = if (count == 1) "1 asistente" else "$count asistentes"
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val isParticipant = userId != null && event.participants.containsKey(userId)

            if (isParticipant) {
                cardView.setBackgroundResource(R.drawable.card_border_accepted)
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            }

            btnDetails.setOnClickListener { onClick(event) }
        }
    }
}
