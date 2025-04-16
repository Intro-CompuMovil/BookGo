package com.example.icm_proyecto01.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.R
import com.example.icm_proyecto01.model.UserBook
import com.squareup.picasso.Picasso

class UserBooksAdapter(
    private val books: List<UserBook>,
    private val onClick: ((UserBook) -> Unit)? = null  // Callback opcional
) : RecyclerView.Adapter<UserBooksAdapter.BookViewHolder>() {

    inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgCover: ImageView = view.findViewById(R.id.imgBookCover)
        private val tvTitle: TextView = view.findViewById(R.id.tvBookTitle)
        private val tvAuthor: TextView = view.findViewById(R.id.tvBookAuthor)
        private val tvGenre: TextView = view.findViewById(R.id.tvBookGenre)
        private val tvState: TextView = view.findViewById(R.id.tvBookState)

        fun bind(book: UserBook) {
            tvTitle.text = book.titulo
            tvAuthor.text = book.autor
            tvGenre.text = book.genero
            tvState.text = "Estado: ${book.estado}"

            if (book.portadaUrl.isNotEmpty()) {
                Picasso.get()
                    .load(book.portadaUrl)
                    .placeholder(R.drawable.default_book)
                    .into(imgCover)
            } else {
                imgCover.setImageResource(R.drawable.default_book)
            }

            itemView.setOnClickListener {
                onClick?.invoke(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_book, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = books.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }
}
