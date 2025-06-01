package com.example.icm_proyecto01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapters.BookSearchAdapter
import com.example.icm_proyecto01.databinding.ActivityShowHiddenBooksBinding
import com.example.icm_proyecto01.model.Book
import com.example.icm_proyecto01.model.UserBook
import com.google.android.material.bottomnavigation.BottomNavigationView

class ShowHiddenBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowHiddenBooksBinding
    private lateinit var adapter: BookSearchAdapter
    private lateinit var hiddenBooks: MutableList<Book>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowHiddenBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.booksRecyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("HiddenBooks", Context.MODE_PRIVATE)
        hiddenBooks = mutableListOf()

        for (entry in sharedPreferences.all) {
            val bookData = entry.value.toString().split(" | ")
            val bookTitle = bookData[0]
            val bookAuthor = bookData[1]
            val bookGenre = bookData[2]
            val bookCoverUrl = bookData.getOrNull(4) ?: ""
            val bookId = entry.key

            val book = Book(bookId, bookTitle, bookAuthor, bookGenre, bookCoverUrl)
            hiddenBooks.add(book)
        }

        adapter = BookSearchAdapter(hiddenBooks) { selectedBook ->
            Toast.makeText(this, "Seleccionaste el libro: ${selectedBook.titulo}", Toast.LENGTH_SHORT).show()

            // Convertir Book a UserBook y pasarlo completo
            val userBook = UserBook(
                id = selectedBook.id,
                titulo = selectedBook.titulo,
                autor = selectedBook.autor,
                genero = selectedBook.genero,
                estado = "Desconocido", // puedes ajustar esto si lo guardas en SharedPreferences
                portadaUrl = selectedBook.portadaUrl,
                hidden = true,
                status = "oculto"
            )

            val intent = Intent(this, SearchHiddenBookActivity::class.java)
            intent.putExtra("USER_BOOK", userBook)
            startActivity(intent)
        }

        binding.booksRecyclerView.adapter = adapter

        binding.bottomNavigation.selectedItemId = R.id.nav_messages
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_messages -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
