package com.example.icm_proyecto01

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.databinding.ActivityRegisterHiddenBookBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.UUID

class RegisterHiddenBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterHiddenBookBinding
    private lateinit var osmMap: MapView
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var roadManager: RoadManager

    private var markerDestino: Marker? = null
    private var markerActual: Marker? = null
    private var roadOverlay: Polyline? = null
    private var currentLocation: GeoPoint? = null
    private var puntoSeleccionado: GeoPoint? = null

    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        binding = ActivityRegisterHiddenBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geocoder = Geocoder(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        roadManager = OSRMRoadManager(this, "ANDROID")

        val bookTitle = intent.getStringExtra("titulo") ?: ""
        val bookAuthor = intent.getStringExtra("autor") ?: ""
        val bookGenre = intent.getStringExtra("genero") ?: ""
        val bookState = intent.getStringExtra("estado") ?: ""
        val portadaUrl = intent.getStringExtra("portada") ?: ""

        binding.etBookTitle.text = bookTitle
        binding.etBookAuthor.text = bookAuthor
        binding.etBookGenre.text = bookGenre
        binding.etBookState.text = bookState

        if (portadaUrl.isNotEmpty()) {
            Picasso.get().load(portadaUrl).placeholder(R.drawable.default_book).into(binding.bookImage)
        } else {
            binding.bookImage.setImageResource(R.drawable.default_book)
        }

        osmMap = binding.osmMap
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)
        osmMap.setBuiltInZoomControls(true)

        pedirPermisosUbicacion()

        binding.searchAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                buscarDireccion(binding.searchAddress.text.toString())
                true
            } else false
        }

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        userName = sharedPref.getString("userName", "Jane Doe")

        // Botón registrar libro oculto
        binding.btnRegisterBook.setOnClickListener {
            val title = binding.etBookTitle.text.toString().trim()
            val author = binding.etBookAuthor.text.toString().trim()
            val genre = binding.etBookGenre.text.toString().trim()
            val state = binding.etBookState.text.toString().trim()
            val location = binding.etBookLocation.text.toString().trim()
            val punto = puntoSeleccionado
            val firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid
            val bookId = intent.getStringExtra("bookId") ?: UUID.randomUUID().toString()

            if (location.isEmpty()) {
                Toast.makeText(this, "Ingresa una ubicación para el libro oculto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (punto == null) {
                Toast.makeText(this, "Debes seleccionar un punto en el mapa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (firebaseUserId == null) {
                Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hiddenBookData = mapOf(
                "id" to bookId,
                "title" to title,
                "author" to author,
                "genre" to genre,
                "state" to state,
                "imageUrl" to portadaUrl,
                "hidden" to true,
                "latitude" to punto.latitude,
                "longitude" to punto.longitude,
                "locationHint" to location,
                "hiderUserId" to firebaseUserId,
                "finderUserId" to null
            )

            val database = FirebaseDatabase.getInstance().reference
            val userBookRef = database.child("Users").child(firebaseUserId.toString()).child("Books").child(bookId)
            val hiddenBooksRef = database.child("HiddenBooks").child(bookId)

            userBookRef.removeValue().addOnSuccessListener {
                hiddenBooksRef.setValue(hiddenBookData).addOnSuccessListener {
                    Toast.makeText(this, "Libro oculto correctamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userName", userName)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al registrar libro oculto en Firebase", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al remover el libro del perfil", Toast.LENGTH_SHORT).show()
            }
        }


        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
            finish()
        }
    }

    private fun pedirPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            obtenerUbicacionActual()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun obtenerUbicacionActual() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = GeoPoint(it.latitude, it.longitude)
                osmMap.controller.setZoom(16.0)
                osmMap.controller.setCenter(currentLocation)

                markerActual = Marker(osmMap).apply {
                    position = currentLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Mi ubicación"
                    icon = ContextCompat.getDrawable(this@RegisterHiddenBookActivity, R.drawable.ic_my_location)
                }
                osmMap.overlays.add(markerActual)
                osmMap.invalidate()
            }
        }
    }

    private fun buscarDireccion(query: String) {
        if (query.isBlank()) return
        val resultados = geocoder.getFromLocationName(query, 1)
        if (!resultados.isNullOrEmpty()) {
            val direccion = resultados[0]
            val punto = GeoPoint(direccion.latitude, direccion.longitude)
            puntoSeleccionado = punto

            markerDestino?.let { osmMap.overlays.remove(it) }
            markerDestino = Marker(osmMap).apply {
                position = punto
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = direccion.getAddressLine(0)
                icon = ContextCompat.getDrawable(this@RegisterHiddenBookActivity, R.drawable.ic_oculto)
            }
            osmMap.overlays.add(markerDestino)
            osmMap.controller.setCenter(punto)

            currentLocation?.let { drawRoute(it, punto) }
        } else {
            Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawRoute(inicio: GeoPoint, destino: GeoPoint) {
        roadOverlay?.let { osmMap.overlays.remove(it) }

        GlobalScope.launch(Dispatchers.Main) {
            val road: Road = withContext(Dispatchers.IO) {
                roadManager.getRoad(arrayListOf(inicio, destino))
            }
            roadOverlay = RoadManager.buildRoadOverlay(road)
            osmMap.overlays.add(roadOverlay)
            osmMap.invalidate()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(this, "Permiso de ubicación requerido", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
