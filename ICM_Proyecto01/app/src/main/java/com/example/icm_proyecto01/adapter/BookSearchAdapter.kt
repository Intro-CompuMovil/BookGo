package com.example.icm_proyecto01.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.R
import com.example.icm_proyecto01.model.Book
import com.squareup.picasso.Picasso

class BookSearchAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookSearchAdapter.BookViewHolder>() {

    inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgThumbnail: ImageView = view.findViewById(R.id.imgBookThumbnail)
        private val tvTitle: TextView = view.findViewById(R.id.tvBookTitle)
        private val tvAuthor: TextView = view.findViewById(R.id.tvBookAuthor)
        private val tvCategory: TextView = view.findViewById(R.id.tvBookCategory)
        private val tvState: TextView = view.findViewById(R.id.tvBookState)

        fun bind(book: Book) {
            tvTitle.text = book.titulo
            tvAuthor.text = book.autor
            tvCategory.text = book.genero
            tvState.text = "Estado: ${book.estado}"

            if (book.portadaUrl.isNotEmpty()) {
                Picasso.get()
                    .load(book.portadaUrl)
                    .placeholder(R.drawable.default_book)
                    .into(imgThumbnail)
            } else {
                imgThumbnail.setImageResource(R.drawable.default_book)
            }

            itemView.setOnClickListener {
                onBookClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = books.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }
}
