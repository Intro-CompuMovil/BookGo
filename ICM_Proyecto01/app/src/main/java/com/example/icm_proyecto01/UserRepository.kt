import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.icm_proyecto01.model.UserBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserRepository(private val context: Context) {

    private val dbRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun fetchUserBooks(onBooksFetched: (List<UserBook>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val userBooksRef = dbRef.child("Users").child(uid).child("Books")

        userBooksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookIds = mutableListOf<String>()
                for (bookSnapshot in snapshot.children) {
                    bookSnapshot.key?.let { bookId ->
                        bookIds.add(bookId)
                    }
                }

                if (bookIds.isEmpty()) {
                    onBooksFetched(emptyList())
                    return
                }

                val books = mutableListOf<UserBook>()
                var processedBooks = 0

                for (id in bookIds) {
                    fetchBookFromGoogleApi(id, snapshot.child(id)) { book ->
                        book?.let { books.add(it) }
                        processedBooks++

                        // Cuando ya procesamos todos los libros, devolvemos la lista
                        if (processedBooks == bookIds.size) {
                            onBooksFetched(books)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading books: ${error.message}")
                onBooksFetched(emptyList())
            }
        })
    }

    fun fetchBookFromGoogleApi(id: String, firebaseBookData: DataSnapshot, onResult: (UserBook?) -> Unit) {
        val url = "https://www.googleapis.com/books/v1/volumes/$id"
        val requestQueue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val volumeInfo = response.getJSONObject("volumeInfo")
                    val title = volumeInfo.optString("title", "Sin título")
                    val authorsArray = volumeInfo.optJSONArray("authors")
                    val author = if (authorsArray != null && authorsArray.length() > 0) authorsArray.getString(0) else "Autor desconocido"
                    val genreArray = volumeInfo.optJSONArray("categories")
                    val genre = if (genreArray != null && genreArray.length() > 0) genreArray.getString(0) else "Género desconocido"
                    val imageLinks = volumeInfo.optJSONObject("imageLinks")
                    val thumbnailUrl = imageLinks?.optString("thumbnail")?.replace("http://", "https://") ?: ""

                    val hidden = firebaseBookData.child("hidden").getValue(Boolean::class.java) ?: false
                    val estado = firebaseBookData.child("state").getValue(String::class.java) ?: "Desconocido"

                    val book = UserBook(
                        id = id,
                        titulo = title,
                        autor = author,
                        genero = genre,
                        estado = estado,
                        portadaUrl = thumbnailUrl,
                        hidden = hidden)
                    onResult(book)
                } catch (e: Exception) {
                    Log.e("GoogleAPI", "Error parsing book $id: ${e.message}")
                    onResult(null)
                }
            },
            { error ->
                Log.e("GoogleAPI", "Error fetching book $id: ${error.message}")
                onResult(null)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}
