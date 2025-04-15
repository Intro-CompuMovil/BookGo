package com.example.icm_proyecto01

import android.Manifest
import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.views.overlay.TilesOverlay

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var osmMap: MapView
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var roadManager: RoadManager

    private var currentLocation: GeoPoint? = null
    private var marker: Marker? = null
    private var roadOverlay: Polyline? = null
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = getSharedPreferences("UserProfile", MODE_PRIVATE).getString("userName", "Jane Doe")
        geocoder = Geocoder(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        roadManager = OSRMRoadManager(this, "ANDROID")

        inicializarMapa()
        pedirPermisos()

        cargarPuntosDeIntercambio()


        binding.searchAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                buscarDireccion(binding.searchAddress.text.toString())
                true
            } else false
        }

        binding.profileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

        binding.btnCreateExchangePoint.setOnClickListener {
            val intent = Intent(this, CreateExchangePointActivity::class.java)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }

        binding.puntoIntercambio.setOnClickListener {
            startActivity(Intent(this, ExchangePointActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_messages -> {
                    val intent = Intent(this, MessagesActivity::class.java)
                    intent.putExtra("userName", userName)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userName", userName)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }


    }

    private fun inicializarMapa() {
        osmMap = findViewById(R.id.osmMap)
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)
        osmMap.setBuiltInZoomControls(true)

    }


    private fun cargarPuntosDeIntercambio() {
        val sharedPref = getSharedPreferences("ExchangePoints", MODE_PRIVATE)
        val points = sharedPref.getStringSet("points", null)

        if (points != null) {
            binding.puntosCercanosContainer.removeAllViews()

            for (punto in points) {
                val datos = punto.split("|")
                if (datos.size == 5) {
                    val tituloLibro = datos[0]
                    val fecha = datos[1]
                    val hora = datos[2]
                    val lat = datos[3].toDoubleOrNull()
                    val lon = datos[4].toDoubleOrNull()

                    val direccion = obtenerDireccionDesdeGeoPoint(lat, lon)

                    val cardView = layoutInflater.inflate(R.layout.item_exchange_point, binding.puntosCercanosContainer, false)

                    val tvLocation = cardView.findViewById<TextView>(R.id.tvLocation)
                    val tvDescription = cardView.findViewById<TextView>(R.id.tvDescription)
                    val tvDateTime = cardView.findViewById<TextView>(R.id.tvDateTime)

                    tvLocation.text = direccion
                    tvDescription.text = "Libro: $tituloLibro"
                    tvDateTime.text = "$fecha - $hora"

                    cardView.setOnClickListener {
                        if (lat != null && lon != null) {
                            val intent = Intent(this, ExchangePointActivity::class.java).apply {
                                putExtra("titulo", tituloLibro)
                                putExtra("direccion", direccion)
                                putExtra("fecha", fecha)
                                putExtra("hora", hora)
                                putExtra("lat", lat)
                                putExtra("lon", lon)
                            }
                            startActivity(intent)
                        }
                    }

                    binding.puntosCercanosContainer.addView(cardView)

                    if (lat != null && lon != null) {
                        val marcador = Marker(osmMap).apply {
                            position = GeoPoint(lat, lon)
                            title = "$tituloLibro\n$direccion\n$fecha $hora"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_exchange_point)
                        }
                        osmMap.overlays.add(marcador)
                    }
                }
            }

            osmMap.invalidate()
        }
    }


    private fun obtenerDireccionDesdeGeoPoint(lat: Double?, lon: Double?): String {
        return if (lat != null && lon != null) {
            try {
                val geocoder = Geocoder(this)
                val address = geocoder.getFromLocation(lat, lon, 1)
                address?.get(0)?.getAddressLine(0) ?: "Dirección desconocida"
            } catch (e: Exception) {
                "Dirección desconocida"
            }
        } else {
            "Dirección desconocida"
        }
    }



    private fun pedirPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            obtenerUbicacion()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun obtenerUbicacion() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = GeoPoint(it.latitude, it.longitude)
                osmMap.controller.setZoom(16.0)
                osmMap.controller.setCenter(currentLocation)

                val markerUbicacion = Marker(osmMap)
                markerUbicacion.position = currentLocation
                markerUbicacion.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                markerUbicacion.title = "Mi ubicación"
                markerUbicacion.icon = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_my_location)
                osmMap.overlays.add(markerUbicacion)
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
            colocarMarcador(punto, direccion.getAddressLine(0))
            currentLocation?.let { drawRoute(it, punto) }
            osmMap.controller.setCenter(punto)
        } else {
            Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
        }

        binding.searchAddress.text.clear()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchAddress.windowToken, 0)
    }

    private fun colocarMarcador(punto: GeoPoint, titulo: String) {
        marker?.let { osmMap.overlays.remove(it) }
        marker = Marker(osmMap).apply {
            position = punto
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = titulo
            icon = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_exchange_point)
        }
        osmMap.overlays.add(marker)
        osmMap.invalidate()
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
            obtenerUbicacion()
        } else {
            Toast.makeText(this, "Permiso de ubicación requerido para mapas", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()
        val uiManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if(uiManager.nightMode == UiModeManager.MODE_NIGHT_YES)
            binding.osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
    }
}
