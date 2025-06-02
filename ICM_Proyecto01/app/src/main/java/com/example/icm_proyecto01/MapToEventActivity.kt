package com.example.icm_proyecto01

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager

class MapToEventActivity : AppCompatActivity() {

    private lateinit var osmMap: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var roadManager: RoadManager
    private var roadOverlay: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_map_to_event)

        // Inicializar vistas
        osmMap = findViewById(R.id.map)
        osmMap.setMultiTouchControls(true)

        val eventTitle = intent.getStringExtra("EVENT_NAME") ?: "Evento"
        val locationStr = intent.getStringExtra("EVENT_LOCATION") ?: ""

        findViewById<TextView>(R.id.tvEventTitle).text = eventTitle
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this)
        roadManager = OSRMRoadManager(this, "ANDROID")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            drawRouteToEvent(locationStr)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun drawRouteToEvent(locationStr: String) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userPoint = GeoPoint(location.latitude, location.longitude)
                val addressList = geocoder.getFromLocationName(locationStr, 1)
                if (!addressList.isNullOrEmpty()) {
                    val eventLat = addressList[0].latitude
                    val eventLon = addressList[0].longitude
                    val eventPoint = GeoPoint(eventLat, eventLon)

                    // Añadir marcadores
                    val userMarker = Marker(osmMap).apply {
                        position = userPoint
                        title = "Tu ubicación"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = resources.getDrawable(R.drawable.ic_my_location, null)
                    }

                    val eventMarker = Marker(osmMap).apply {
                        position = eventPoint
                        title = "Evento"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = resources.getDrawable(R.drawable.ic_event_point, null)
                    }

                    osmMap.overlays.add(userMarker)
                    osmMap.overlays.add(eventMarker)
                    osmMap.controller.setZoom(15.0)
                    osmMap.controller.setCenter(userPoint)

                    drawRoute(userPoint, eventPoint)
                } else {
                    Toast.makeText(this, "No se encontró la dirección del evento", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Ubicación actual no disponible", Toast.LENGTH_SHORT).show()
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
}
