package com.example.icm_proyecto01

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.icm_proyecto01.databinding.ActivityExchangeRouteBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.bonuspack.routing.RoadNode
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class ExchangeRouteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeRouteBinding
    private lateinit var osmMap: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var roadManager: RoadManager
    private lateinit var geocoder: Geocoder

    private var currentLocation: GeoPoint? = null
    private var destino: GeoPoint? = null
    private var roadOverlay: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        binding = ActivityExchangeRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        osmMap = binding.osmMap
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)
        osmMap.setBuiltInZoomControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        roadManager = OSRMRoadManager(this, "ANDROID")
        geocoder = Geocoder(this)

        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        destino = GeoPoint(lat, lon)

        binding.tvTitulo.text = intent.getStringExtra("titulo") ?: "Sin título"
        binding.tvDireccion.text = intent.getStringExtra("direccion") ?: "Sin dirección"
        binding.tvFechaHora.text = "${intent.getStringExtra("fecha") ?: "-"} - ${intent.getStringExtra("hora") ?: "-"}"

        pedirPermisosUbicacion()
    }

    private fun pedirPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            obtenerUbicacionYMostrarRuta()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun obtenerUbicacionYMostrarRuta() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = GeoPoint(it.latitude, it.longitude)
                osmMap.controller.setZoom(15.0)
                osmMap.controller.setCenter(currentLocation)

                destino?.let { destinoPoint ->
                    mostrarRuta(currentLocation!!, destinoPoint)
                }
            }
        }
    }

    private fun mostrarRuta(inicio: GeoPoint, fin: GeoPoint) {
        GlobalScope.launch(Dispatchers.Main) {
            val road: Road = withContext(Dispatchers.IO) {
                roadManager.getRoad(arrayListOf(inicio, fin))
            }

            roadOverlay?.let { osmMap.overlays.remove(it) }
            roadOverlay = RoadManager.buildRoadOverlay(road)
            osmMap.overlays.add(roadOverlay)

            val roadMarkers = FolderOverlay()
            val iconNode: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_my_location, null)

            for (i in road.mNodes.indices) {
                val node: RoadNode = road.mNodes[i]
                val marker = Marker(osmMap).apply {
                    position = node.mLocation
                    icon = iconNode
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = "Paso $i"
                    snippet = node.mInstructions
                    subDescription = Road.getLengthDurationText(this@ExchangeRouteActivity, node.mLength, node.mDuration)
                    image = ResourcesCompat.getDrawable(resources, R.drawable.ic_book_register, null)
                }
                roadMarkers.add(marker)
            }

            osmMap.overlays.add(roadMarkers)
            osmMap.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.osmMap.onPause()
    }
}
