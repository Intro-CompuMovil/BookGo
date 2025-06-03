package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapters.BookSearchAdapter
import com.example.icm_proyecto01.databinding.ActivityShowHiddenBooksBinding
import com.example.icm_proyecto01.model.Book
import com.example.icm_proyecto01.model.UserBook
import com.google.firebase.database.*

class ShowHiddenBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowHiddenBooksBinding
    private lateinit var adapter: BookSearchAdapter
    private val hiddenBooks: MutableList<Book> = mutableListOf()
    private val hiddenBooksRef = FirebaseDatabase.getInstance().reference.child("HiddenBooks")

    private lateinit var tvEmptyMessage: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowHiddenBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.booksRecyclerView.layoutManager = LinearLayoutManager(this)
        tvEmptyMessage = binding.tvEmptyMessage


        adapter = BookSearchAdapter(hiddenBooks) { selectedBook ->
            val userBook = UserBook(
                id = selectedBook.id,
                titulo = selectedBook.titulo,
                autor = selectedBook.autor,
                genero = selectedBook.genero,
                estado = selectedBook.estado,
                portadaUrl = selectedBook.portadaUrl,
                hidden = true,
            )


                Log.d("ARDebug", "Pasando a ARBookActivity con libro: ${userBook.titulo}")
                val intent = Intent(this, ARBookActivity::class.java)
                intent.putExtra("USER_BOOK", userBook)
                startActivity(intent)

        }

        binding.booksRecyclerView.adapter = adapter
        cargarLibrosOcultosDesdeFirebase()

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

    private fun cargarLibrosOcultosDesdeFirebase() {
        hiddenBooksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hiddenBooks.clear()
                for (bookSnapshot in snapshot.children) {
                    val finderUserId = bookSnapshot.child("finderUserId").getValue(String::class.java)
                    if (!finderUserId.isNullOrBlank()) continue

                    val id = bookSnapshot.child("id").getValue(String::class.java) ?: continue
                    val title = bookSnapshot.child("title").getValue(String::class.java) ?: ""
                    val author = bookSnapshot.child("author").getValue(String::class.java) ?: ""
                    val genre = bookSnapshot.child("genre").getValue(String::class.java) ?: ""
                    val imageUrl = bookSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                    val state = bookSnapshot.child("state").getValue(String::class.java) ?: "Desconocido"

                    val book = Book(id, title, author, genre, imageUrl, estado = state)
                    hiddenBooks.add(book)
                }

                adapter.notifyDataSetChanged()

                if (hiddenBooks.isEmpty()) {
                    tvEmptyMessage.visibility = View.VISIBLE
                    binding.booksRecyclerView.visibility = View.GONE
                    Toast.makeText(
                        this@ShowHiddenBooksActivity,
                        "No hay libros ocultos disponibles",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    tvEmptyMessage.visibility = View.GONE
                    binding.booksRecyclerView.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ShowHiddenBooksActivity,
                    "Error al cargar libros ocultos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}
