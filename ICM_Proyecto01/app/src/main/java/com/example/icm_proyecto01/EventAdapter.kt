package com.example.icm_proyecto01


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private val eventList: List<Event>, private val onClick: (Event) -> Unit) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

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

        fun bind(event: Event, onClick: (Event) -> Unit) {
            tvName.text = event.name
            tvLocation.text = event.location
            tvDate.text = event.date
            itemView.setOnClickListener { onClick(event) }
        }
    }
}