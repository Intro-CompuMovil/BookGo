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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.databinding.ActivityRegisterHiddenBookBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

        // Obtener datos del intent
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
            val title = bookTitle.trim()
            val author = bookAuthor.trim()
            val genre = bookGenre.trim()
            val state = bookState.trim()
            val location = binding.etBookLocation.text.toString().trim()
            val punto = puntoSeleccionado

            if (location.isEmpty()) {
                Toast.makeText(this, "Ingresa una ubicación para el libro oculto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (punto == null) {
                Toast.makeText(this, "Debes seleccionar un punto en el mapa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedHidden = getSharedPreferences("HiddenBooks", Context.MODE_PRIVATE)
            val editor = sharedHidden.edit()
            val bookData = "$title | $author | $genre | $state | $portadaUrl | ${punto.latitude} | ${punto.longitude} | $location"
            editor.putString(title, bookData)
            editor.apply()

            Toast.makeText(this, "Libro oculto registrado correctamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
            finish()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(this, "Permiso de ubicación requerido", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
