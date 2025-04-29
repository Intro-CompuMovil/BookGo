package com.example.icm_proyecto01

import UserRepository
import ExchangePointRepository
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
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
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
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject

import org.osmdroid.api.IMapController
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.TilesOverlay

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlin.math.log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var osmMap: MapView
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var roadManager: RoadManager
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: GeoPoint? = null
    private var roadOverlay: Polyline? = null
    private var userName: String? = null
    private var marker: Marker? = null


    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepCount: Int = 0
    private var isStepSensorActive: Boolean = false

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                stepCount = event.values[0].toInt()
                // Guardar el recuento para RewardsActivity
                val sharedPref = getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
                sharedPref.edit().putInt("steps", stepCount).apply()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = getSharedPreferences("UserProfile", MODE_PRIVATE).getString("userName", "Jane Doe")
        geocoder = Geocoder(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    actualizarUbicacionEnMapa(location)
                }
            }
        }


        roadManager = OSRMRoadManager(this, "ANDROID")


        inicializarMapa()
        pedirPermisos()

        cargarPuntosDeIntercambio()
        Log.d("PuntoIntercambio", "se llamo a punto")
        cargarEventosEnMapa()
        mostrarLibrosOcultos()

        val focusLat = intent.getDoubleExtra("focusLat", Double.NaN)
        val focusLon = intent.getDoubleExtra("focusLon", Double.NaN)

        if (!focusLat.isNaN() && !focusLon.isNaN()) {
            val focusPoint = GeoPoint(focusLat, focusLon)
            osmMap.controller.setCenter(focusPoint)
            osmMap.controller.setZoom(17.0)

            osmMap.invalidate()
        }


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
                    val intent = Intent(this, ShowHiddenBooksActivity::class.java)
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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun iniciarActualizacionesDeUbicacion() {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            5000 // cada 5 segundos
        ).build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }



    private fun inicializarMapa() {
        osmMap = findViewById(R.id.osmMap)
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)
        osmMap.setBuiltInZoomControls(true)

    }

    private fun cargarEventosDesdeFirebase(onEventosListos: (JSONArray) -> Unit) {
        val eventosRef = FirebaseDatabase.getInstance().getReference("Events")

        eventosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventosList = mutableListOf<JSONObject>()

                for (eventoSnapshot in snapshot.children) {
                    try {
                        val eventoObj = JSONObject()

                        eventoObj.put("name", eventoSnapshot.child("name").getValue(String::class.java) ?: "Evento")
                        eventoObj.put("location", eventoSnapshot.child("location").getValue(String::class.java) ?: "Dirección desconocida")
                        eventoObj.put("date", eventoSnapshot.child("date").getValue(String::class.java) ?: "")
                        eventoObj.put("lat", eventoSnapshot.child("lat").getValue(Double::class.java) ?: Double.NaN)
                        eventoObj.put("lon", eventoSnapshot.child("lon").getValue(Double::class.java) ?: Double.NaN)

                        eventosList.add(eventoObj)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val eventosArray = JSONArray(eventosList)
                onEventosListos(eventosArray)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al cargar eventos: ${error.message}")
            }
        })
    }


    //Para el evento
    private fun cargarEventosEnMapa() {
        cargarEventosDesdeFirebase { eventosArray ->
            try {
                for (i in 0 until eventosArray.length()) {
                    val evento = eventosArray.getJSONObject(i)
                    val lat = evento.optDouble("lat", Double.NaN)
                    val lon = evento.optDouble("lon", Double.NaN)
                    val nombre = evento.optString("name", "Evento")
                    val direccion = evento.optString("location", "Dirección desconocida")
                    val fechaHora = evento.optString("date", "")

                    if (!lat.isNaN() && !lon.isNaN()) {
                        val marcadorEvento = Marker(osmMap).apply {
                            position = GeoPoint(lat, lon)
                            title = "$nombre\n$direccion\n$fechaHora"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_event_point)
                        }
                        osmMap.overlays.add(marcadorEvento)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            osmMap.invalidate()
        }
    }


    //Para el intercambio
    private fun cargarPuntosDeIntercambio() {
        val exchangePointRepository = ExchangePointRepository(this)
        Log.d("PrimerLog", "llamado a puntoIntercambio")
        exchangePointRepository.sincronizarPuntosDeFirebase { points ->
            Log.d("PrimerLog", "llamado a $points")
            // Verificamos si los puntos existen
            if (points.isNotEmpty()) {
                Log.d("PrimerLog", "no esta empty $points")
                binding.puntosCercanosContainer.removeAllViews()

                for (punto in points) {
                    val datos = punto.split("|")
                    if (datos.size >= 5) {
                        Log.d("PuntoIntercambio", "datos = $datos")
                        val tituloLibro = datos[0]
                        Log.d("PuntoIntercambio:", "Titulo $tituloLibro" )
                        val fecha = datos[1]
                        val hora = datos[2]
                        val lat = datos[3].toDoubleOrNull()
                        val lon = datos[4].toDoubleOrNull()
                        Log.d("PuntoIntercambio:", "Lon $lon" )
                        val estadoLibro = if (datos.size >= 6) datos[5] else "No disponible"
                        val portadaUrl = if (datos.size >= 7) datos[6] else ""
                        val idPunto = if (datos.size >= 8) datos[7] else "404"



                            val direccion = obtenerDireccionDesdeGeoPoint(lat, lon)

                            val cardView = layoutInflater.inflate(
                                R.layout.item_exchange_point_home,
                                binding.puntosCercanosContainer,
                                false
                            )

                            val tvLocation = cardView.findViewById<TextView>(R.id.tvLocation)
                            val tvDescription =
                                cardView.findViewById<TextView>(R.id.tvDescription)
                            val tvDateTime = cardView.findViewById<TextView>(R.id.tvDateTime)

                            tvLocation.text = direccion
                            tvDescription.text = "Libro: $tituloLibro\nEstado: $estadoLibro"
                            tvDateTime.text = "$fecha - $hora"


                            val imgCover = cardView.findViewById<ImageView>(R.id.imgBookCover)
                            if (portadaUrl.isNotEmpty()) {
                                Picasso.get().load(portadaUrl)
                                    .placeholder(R.drawable.default_book).into(imgCover)
                            } else {
                                imgCover.setImageResource(R.drawable.default_book)
                            }


                            cardView.setOnClickListener {
                                if (lat != null && lon != null) {
                                    Log.d("PuntoIntercambio", "lon y lat no null")
                                    val intent =
                                        Intent(this, ExchangePointActivity::class.java).apply {
                                            putExtra("titulo", tituloLibro)
                                            putExtra("direccion", direccion)
                                            putExtra("fecha", fecha)
                                            putExtra("hora", hora)
                                            putExtra("lat", lat)
                                            putExtra("lon", lon)
                                            Log.d("PuntoIntercambio", "puso lon")
                                            putExtra("estadoLibro", estadoLibro)
                                            putExtra("portadaUrl", portadaUrl)
                                            putExtra("idPunto", idPunto)
                                        }
                                    startActivity(intent)

                                }
                            }

                            binding.puntosCercanosContainer.addView(cardView)

                            if (lat != null && lon != null) {
                                val marcador = Marker(osmMap).apply {
                                    Log.d("PuntoIntercambioOSM", "Lon en OSM=$lon")
                                    position = GeoPoint(lat, lon)
                                    title = "$tituloLibro\n$direccion\n$fecha $hora"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    icon = ContextCompat.getDrawable(
                                        this@HomeActivity,
                                        R.drawable.ic_exchange_point
                                    )
                                }
                                osmMap.overlays.add(marcador)
                            }
                        }

                    }
                    osmMap.invalidate()
            }else{
                Log.d("PrimerLog", "si esta empty $points")
            }

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

    private fun actualizarUbicacionEnMapa(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if (marker == null) {
            marker = Marker(osmMap).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(osmMap.context, R.drawable.ic_my_location)
                title = "Mi ubicación"
            }
            osmMap.overlays.add(marker)
        }

        marker?.position = geoPoint
        osmMap.invalidate()
    }






    private fun pedirPermisos() {
        val permisos = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (permisos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 1)
        } else {
            obtenerUbicacion()
            activarSensorDePasos()
            iniciarActualizacionesDeUbicacion()

        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun obtenerUbicacion() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = GeoPoint(it.latitude, it.longitude)
                osmMap.controller.setZoom(16.0)
                osmMap.controller.setCenter(currentLocation)

                if (marker == null) {
                    marker = Marker(osmMap).apply {
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ContextCompat.getDrawable(osmMap.context, R.drawable.ic_my_location)
                        title = "Mi ubicación"
                    }
                    osmMap.overlays.add(marker)
                }

                marker?.position = currentLocation

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
            //colocarMarcador(punto, direccion.getAddressLine(0))
            //currentLocation?.let { drawRoute(it, punto) }
            osmMap.controller.setCenter(punto)
        } else {
            Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
        }

        binding.searchAddress.text.clear()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchAddress.windowToken, 0)
    }

    //para podometro


    private fun activarSensorDePasos() {
        stepSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
            isStepSensorActive = true
        }
    }


    //Para libros ocultos
    private fun mostrarLibroOculto(lat: Double, lon: Double, titulo: String, autor: String, instruccion: String) {
        val punto = GeoPoint(lat, lon)

        val marcador = Marker(osmMap).apply {
            position = punto
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "📖 $titulo"
            subDescription = "Autor: $autor\n🔍 Instrucciones: $instruccion"
            icon = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_oculto)
        }

        osmMap.overlays.add(marcador)
    }


    private fun mostrarLibrosOcultos() {
        val shared = getSharedPreferences("HiddenBooks", Context.MODE_PRIVATE)
        val all = shared.all

        for ((_, data) in all) {
            val partes = data.toString().split("|")
            if (partes.size >= 7) {
                val titulo = partes[0].trim()
                val autor = partes[1].trim()
                val lat = partes[5].toDoubleOrNull()
                val lon = partes[6].toDoubleOrNull()
                val instruccion = if (partes.size >= 8) partes[7].trim() else "Sin instrucciones"

                if (lat != null && lon != null) {
                    mostrarLibroOculto(lat, lon, titulo, autor, instruccion)
                }
            }
        }

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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty()) {
                var permisoUbicacionConcedido = false
                var permisoActividadConcedido = false

                for (i in permissions.indices) {
                    when (permissions[i]) {
                        Manifest.permission.ACCESS_FINE_LOCATION -> {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                permisoUbicacionConcedido = true
                            }
                        }
                        Manifest.permission.ACTIVITY_RECOGNITION -> {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                permisoActividadConcedido = true
                            }
                        }
                    }
                }

                if (permisoUbicacionConcedido) {
                    obtenerUbicacion()
                } else {
                    Toast.makeText(this, "Permiso de ubicación requerido para mapas", Toast.LENGTH_SHORT).show()
                }

                if (permisoActividadConcedido) {
                    activarSensorDePasos()
                } else {
                    Toast.makeText(this, "Permiso de actividad física requerido para contar pasos", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    override fun onPause() {
        super.onPause()
        osmMap.onPause()
        if (isStepSensorActive) {
            sensorManager.unregisterListener(sensorListener)
        }
        osmMap.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (isStepSensorActive) {
            activarSensorDePasos()
        }
        binding.osmMap.onResume()
        osmMap.onResume()
        val uiManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if (uiManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
            binding.osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
        cargarPuntosDeIntercambio()
        mostrarLibrosOcultos()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}