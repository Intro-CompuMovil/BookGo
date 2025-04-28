import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.icm_proyecto01.model.UserBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ExchangePointRepository(private val context: Context) {

    private val dbRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // Función modificada para incluir la llamada a la API de Google Books
    fun sincronizarPuntosDeFirebase(onPointsFetched: (Set<String>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val exchangePointsRef = dbRef.child("ExchangePoints")

        Log.d("Firebase", "Iniciando sincronización de puntos de intercambio para el usuario: $uid")

        exchangePointsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pointsSet = mutableSetOf<String>()
                Log.d("Firebase", "Puntos de intercambio obtenidos: ${snapshot.childrenCount}")

                val puntosProcesados = snapshot.children.count()
                var puntosCargados = 0

                snapshot.children.forEach { pointSnapshot ->
                    val receiverId = pointSnapshot.child("receiverUserId").value.toString()
                    val idLibro = pointSnapshot.child("Book").child("id").value.toString()
                    val state = pointSnapshot.child("Book").child("state").value.toString()

                    Log.d("Firebase", "Procesando punto: libroId=$idLibro, receiverId=$receiverId, estado=$state")

                        val date = pointSnapshot.child("date").value.toString()
                        val lat = pointSnapshot.child("lat").getValue(Double::class.java) ?: 0.0
                        val lon = pointSnapshot.child("lon").getValue(Double::class.java) ?: 0.0

                        Log.d("Firebase", "Punto válido, lat=$lat, lon=$lon, fecha=$date")

                        // Llamada a la API de Google Books para obtener detalles del libro
                        fetchBookFromGoogleApi(idLibro, pointSnapshot) { book ->
                            book?.let {
                                val portadaUrl = it.portadaUrl
                                val fecha = date.split(" ")[0]
                                val hora = if (date.contains("-")) date.split("-").getOrNull(1)?.trim() ?: "00:00" else "00:00"

                                // Formateamos el punto para agregarlo al set
                                val puntoFormateado = "${it.titulo}|$fecha|$hora|$lat|$lon|$state|$portadaUrl"
                                Log.d("Firebase", "Punto formateado: $puntoFormateado")

                                // Agregamos el punto formateado al conjunto
                                pointsSet.add(puntoFormateado)
                            }

                            // Aumentamos el contador de puntos cargados
                            puntosCargados++

                            // Si hemos procesado todos los puntos, llamamos al callback
                            if (puntosCargados == puntosProcesados) {
                                Log.d("Firebase", "Sincronización completa, puntos encontrados: ${pointsSet.size}")
                                Log.d("Firebase Log", "array = $pointsSet")
                                onPointsFetched(pointsSet)
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al cargar los puntos de intercambio: ${error.message}")
                onPointsFetched(emptySet())
            }
        })
    }

    // Función para obtener la información del libro desde la API de Google Books
    private fun fetchBookFromGoogleApi(id: String, firebaseBookData: DataSnapshot, onResult: (UserBook?) -> Unit) {
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
                        hidden = hidden,
                        status = estado
                    )
                    onResult(book)
                } catch (e: Exception) {
                    Log.e("GoogleAPI", "Error al procesar el libro $id: ${e.message}")
                    onResult(null)
                }
            },
            { error ->
                Log.e("GoogleAPI", "Error al obtener el libro $id: ${error.message}")
                onResult(null)
            }
        )
        requestQueue.add(jsonObjectRequest)
    }
}
