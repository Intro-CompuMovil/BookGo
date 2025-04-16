package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.icm_proyecto01.adapters.BookSearchAdapter
import com.example.icm_proyecto01.databinding.ActivityBookSearchBinding
import com.example.icm_proyecto01.model.Book

class BookSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookSearchBinding
    private lateinit var adapter: BookSearchAdapter
    private val bookList = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = BookSearchAdapter(bookList) { selectedBook ->
            // Aquí se maneja el click de un libro seleccionado (ej: pasar a pantalla de registro)
            val intent = Intent(this, AddBookFromApiActivity::class.java).apply {
                putExtra("title", selectedBook.titulo)
                putExtra("author", selectedBook.autor)
                putExtra("genre", selectedBook.genero)
                putExtra("image", selectedBook.portadaUrl)
            }
            startActivity(intent)

        }

        binding.rvBookResults.layoutManager = LinearLayoutManager(this)
        binding.rvBookResults.adapter = adapter

        binding.searchView.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    buscarLibros(query)
                }
                true
            } else false
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun buscarLibros(query: String) {
        val url = "https://www.googleapis.com/books/v1/volumes?q=${query.replace(" ", "+")}&maxResults=10"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val items = response.optJSONArray("items")
                bookList.clear()
                if (items != null) {
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val volumeInfo = item.getJSONObject("volumeInfo")

                        val title = volumeInfo.optString("title", "Sin título")
                        val authorsArray = volumeInfo.optJSONArray("authors")
                        val authors = authorsArray?.join(", ") ?: "Autor desconocido"
                        val categoriesArray = volumeInfo.optJSONArray("categories")
                        val category = categoriesArray?.optString(0) ?: "Género desconocido"
                        val imageLinks = volumeInfo.optJSONObject("imageLinks")
                        val rawThumbnail = imageLinks?.optString("thumbnail") ?: ""
                        val thumbnail = rawThumbnail.replace("http://", "https://")

                        val book = Book(title, authors, category, thumbnail)
                        bookList.add(book)

                    }
                }

                adapter.notifyDataSetChanged()
            },
            {
                Toast.makeText(this, "Error al buscar libros", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}
