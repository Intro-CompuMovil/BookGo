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
import com.google.android.material.bottomnavigation.BottomNavigationView

class ShowHiddenBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowHiddenBooksBinding
    private lateinit var adapter: BookSearchAdapter
    private lateinit var hiddenBooks: MutableList<Book>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración del ViewBinding
        binding = ActivityShowHiddenBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar RecyclerView
        binding.booksRecyclerView.layoutManager = LinearLayoutManager(this)

        // Obtener los datos de los libros ocultos desde SharedPreferences
        val sharedPreferences = getSharedPreferences("HiddenBooks", Context.MODE_PRIVATE)
        hiddenBooks = mutableListOf()

        // Recoger los datos de los libros ocultos
        for (entry in sharedPreferences.all) {
            val bookData = entry.value.toString().split(" | ")
            val bookTitle = bookData[0]
            val bookAuthor = bookData[1]
            val bookGenre = bookData[2]
            val bookCoverUrl = bookData.getOrNull(3) ?: ""

            // Crear un objeto Book y añadirlo a la lista
            val book = Book(bookTitle, bookAuthor, bookGenre, bookCoverUrl)
            hiddenBooks.add(book)
        }

        // Configurar el adaptador
        adapter = BookSearchAdapter(hiddenBooks) { selectedBook ->
            // Acción cuando se selecciona un libro
            Toast.makeText(this, "Seleccionaste el libro: ${selectedBook.titulo}", Toast.LENGTH_SHORT).show()

            // Abrir la actividad de búsqueda o cámara para ese libro
            val intent = Intent(this, SearchHiddenBookActivity::class.java)
            intent.putExtra("selectedBook", selectedBook.titulo)  // Pasar el título del libro seleccionado
            startActivity(intent)
        }

        // Asignar el adaptador al RecyclerView
        binding.booksRecyclerView.adapter = adapter

        // Configuración de la barra de navegación inferior
        binding.bottomNavigation.selectedItemId = R.id.nav_messages  // Asegúrate de que "nav_messages" esté seleccionado por defecto
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
                R.id.nav_messages -> true  // Si el botón actual es "nav_messages", solo regresamos true
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
