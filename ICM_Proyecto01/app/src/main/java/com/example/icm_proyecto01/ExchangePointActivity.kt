package com.example.icm_proyecto01

import android.Manifest
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.databinding.ActivityExchangePointBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class ExchangePointActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExchangePointBinding
    private lateinit var osmMap: MapView
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var roadManager: RoadManager

    private var currentLocation: GeoPoint? = null
    private var puntoDestino: GeoPoint? = null
    private var markerDestino: Marker? = null
    private var roadOverlay: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        binding = ActivityExchangePointBinding.inflate(layoutInflater)
        setContentView(binding.root)

        osmMap = binding.osmMap
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)
        osmMap.setBuiltInZoomControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this)
        roadManager = OSRMRoadManager(this, "ANDROID")

        pedirPermisos()

        val extras = intent.extras
        val tituloLibro = extras?.getString("titulo") ?: "Libro"
        val direccion = extras?.getString("direccion") ?: "Dirección"
        val fecha = extras?.getString("fecha") ?: "Fecha"
        val hora = extras?.getString("hora") ?: "Hora"
        val lat = extras?.getDouble("lat") ?: 0.0
        val lon = extras?.getDouble("lon") ?: 0.0
        puntoDestino = GeoPoint(lat, lon)

        binding.tvTituloLibro.text = "Título: $tituloLibro"
        binding.tvDireccion.text = "Ubicación: $direccion"
        binding.tvFechaHora.text = "$fecha - $hora"

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.btnOfferBook.setOnClickListener {
            Toast.makeText(this, "Libro ofrecido correctamente!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun pedirPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            obtenerUbicacion()
        }
    }

    private fun obtenerUbicacion() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = GeoPoint(it.latitude, it.longitude)
                osmMap.controller.setZoom(16.0)
                osmMap.controller.setCenter(puntoDestino ?: currentLocation)

                puntoDestino?.let { destino ->
                    markerDestino = Marker(osmMap).apply {
                        position = destino
                        title = "Punto de intercambio"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ContextCompat.getDrawable(this@ExchangePointActivity, R.drawable.ic_exchange_point)
                    }
                    osmMap.overlays.add(markerDestino)

                    currentLocation?.let { drawRoute(it, destino) }
                    osmMap.invalidate()
                }
            }
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

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()
        val uiManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if (uiManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
            binding.osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.osmMap.onPause()
    }
}
