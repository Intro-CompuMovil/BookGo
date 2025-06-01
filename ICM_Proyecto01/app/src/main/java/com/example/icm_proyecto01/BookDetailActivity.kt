package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityBookDetailBinding
import com.example.icm_proyecto01.model.UserBook
import com.squareup.picasso.Picasso

class BookDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedBook = intent.getSerializableExtra("book") as? UserBook

        selectedBook?.let { book ->
            binding.tvBookTitle.text = book.titulo
            binding.tvBookAuthor.text = "Autor: ${book.autor}"
            binding.tvBookGenre.text = "Género: ${book.genero}"
            binding.tvBookState.text = "Estado: ${book.estado}"

            if (book.portadaUrl.isNotEmpty()) {
                Picasso.get().load(book.portadaUrl).placeholder(R.drawable.default_book).into(binding.imgBookCover)
            } else {
                binding.imgBookCover.setImageResource(R.drawable.default_book)
            }

            binding.btnOcultar.text = "Ocultar"
            binding.btnOcultar.setOnClickListener {
                val intent = Intent(this, RegisterHiddenBookActivity::class.java).apply {
                    putExtra("titulo", book.titulo)
                    putExtra("autor", book.autor)
                    putExtra("genero", book.genero)
                    putExtra("estado", book.estado)
                    putExtra("portada", book.portadaUrl)
                    putExtra("bookId", book.id)
                }
                startActivity(intent)
            }

            binding.btnIntercambiar.setOnClickListener {
                val intent = Intent(this, CreateExchangePointActivity::class.java)
                intent.putExtra("selectedBook", book)
                startActivity(intent)
            }

        } ?: Toast.makeText(this, "No se pudo cargar el libro", Toast.LENGTH_SHORT).show()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
