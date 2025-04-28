import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.icm_proyecto01.model.UserBook

class UserRepository {

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

                // Now fetch book details from Google Books API
                CoroutineScope(Dispatchers.IO).launch {
                    val books = mutableListOf<UserBook>()
                    for (id in bookIds) {
                        val book = fetchBookFromGoogleApi(id, snapshot.child(id))
                        if (book != null) {
                            books.add(book)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onBooksFetched(books)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading books: ${error.message}")
            }
        })
    }

    private fun fetchBookFromGoogleApi(id: String, firebaseBookData: DataSnapshot): UserBook? {
        try {
            val url = URL("https://www.googleapis.com/books/v1/volumes/$id")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connect()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val stream = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(stream)
                val volumeInfo = jsonObject.getJSONObject("volumeInfo")

                val titulo = volumeInfo.optString("title", "Sin título")
                val autoresArray = volumeInfo.optJSONArray("authors")
                val autor = if (autoresArray != null && autoresArray.length() > 0) autoresArray.getString(0) else "Autor desconocido"
                val generoArray = volumeInfo.optJSONArray("categories")
                val genero = if (generoArray != null && generoArray.length() > 0) generoArray.getString(0) else "Género desconocido"
                val portadaUrl = volumeInfo.getJSONObject("imageLinks").optString("thumbnail", "")

                val hidden = firebaseBookData.child("hidden").getValue(Boolean::class.java) ?: false
                val estado = firebaseBookData.child("state").getValue(String::class.java) ?: "Desconocido"

                return UserBook(
                    id = id,
                    titulo = titulo,
                    autor = autor,
                    genero = genero,
                    estado = estado,
                    portadaUrl = portadaUrl,
                    hidden = hidden,
                    status = estado
                )
            }
        } catch (e: Exception) {
            Log.e("GoogleAPI", "Error fetching book $id: ${e.message}")
        }
        return null
    }
}
