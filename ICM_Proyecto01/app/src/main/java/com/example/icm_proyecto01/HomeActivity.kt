package com.example.icm_proyecto01


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_ACTIVITY_RECOGNITION
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_BACKGROUND_LOCATION
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_FINE_LOCATION
import com.example.icm_proyecto01.Miscellaneous.Companion.PERMISSION_MULTIPLE
import com.example.icm_proyecto01.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import kotlinx.coroutines.*


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var userName: String? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var roadManager: RoadManager
    private var mMap: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private var lastMarker: Marker? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        userName = sharedPref.getString("userName", "Jane Doe")


        cargarImagenDePerfil()
        cargarPuntosDeIntercambio()
        iniciarMapa()

        binding.puntoIntercambio.setOnClickListener {
            val intent = Intent(this, ExchangePointActivity::class.java)
            startActivity(intent)
        }


        binding.profileImage.setOnClickListener {

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userName", userName) // Reenviamos el nombre a ProfileActivity
            startActivity(intent)
        }

        // Botón "Crear Punto de Intercambio"
        binding.btnCreateExchangePoint.setOnClickListener {
            val intent = Intent(this, CreateExchangePointActivity::class.java)
            intent.putExtra("userName", userName) // Reenviamos el nombre a ProfileActivity
            startActivity(intent)
        }


        //BARRA DE NAVEGACION
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Ya estamos en HomeActivity, no hacer nada
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_messages -> {
                    val intent = Intent(this, MessagesActivity::class.java)
                    intent.putExtra("userName", userName) // Reenviamos el nombre a ProfileActivity
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userName", userName) // Reenviamos el nombre a ProfileActivity
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }



        // PEDIR PERMISOS
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED -> {
                // Si ambos permisos están concedidos, iniciar tracking de ubicación y pasos. por ahora esta vacío previo a su implementacion
                //iniciarConteoPasos()
            }
            else -> {
                // Pedir ambos permisos juntos en un solo request
                pedirPermiso(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACTIVITY_RECOGNITION
                    ),
                    PERMISSION_MULTIPLE
                )
            }
        }



    }


    private fun cargarImagenDePerfil() {
        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val savedImageUri = sharedPref.getString("profileImageUri", null)

        if (savedImageUri != null) {
            binding.profileImage.setImageURI(Uri.parse(savedImageUri))
        }
    }


    private fun cargarPuntosDeIntercambio() {
        val sharedPref = getSharedPreferences("ExchangePoints", MODE_PRIVATE)
        val points = sharedPref.getStringSet("points", null)

        if (points != null) {
            for (punto in points) {
                val datos = punto.split("|")
                if (datos.size == 4) {
                    val cardView = layoutInflater.inflate(R.layout.item_exchange_point, binding.puntosCercanosContainer, false)

                    val tvLocation = cardView.findViewById<TextView>(R.id.tvLocation)
                    val tvDescription = cardView.findViewById<TextView>(R.id.tvDescription)
                    val tvDateTime = cardView.findViewById<TextView>(R.id.tvDateTime)

                    tvLocation.text = datos[1] // Dirección
                    tvDescription.text = "Libro: ${datos[0]}" // Título del libro
                    tvDateTime.text = "${datos[2]} - ${datos[3]}" // Fecha y Hora

                    binding.puntosCercanosContainer.addView(cardView)
                }
            }
        }
    }
    


    private fun pedirPermiso(context: Activity, permisos: Array<String>, idCode: Int) {
        if (permisos.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(context, permisos, idCode)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_MULTIPLE) {
            var permisoUbicacionConcedido = false
            var permisoPasosConcedido = false

            for (i in permissions.indices) {
                when (permissions[i]) {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            permisoUbicacionConcedido = true
                            // se utiliza sharedPreferences para guardar la decisión y cambiar la imagen de mapa
                            val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putBoolean("ubicacionPermitida", true)
                                apply()
                            }
                        }
                    }
                    android.Manifest.permission.ACTIVITY_RECOGNITION -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            permisoPasosConcedido = true
                        }
                    }
                }
            }
            iniciarMapa()

            if (!permisoUbicacionConcedido) {
                Toast.makeText(this, "Permiso de ubicación requerido para esta función", Toast.LENGTH_SHORT).show()
            }
            if (!permisoPasosConcedido) {
                Toast.makeText(this, "Permiso de actividad física requerido para rastrear pasos", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //por ahora se maneja actualizar imagen del mapa, pero se utilizará para funcionalidades de mapa futuras
    private fun iniciarMapa() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap

            geocoder = Geocoder(this)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            roadManager = OSRMRoadManager(this, "ANDROID")

            configurarSensorLuz()
            obtenerUbicacion()

            mMap!!.uiSettings.isZoomControlsEnabled = true
            mMap!!.uiSettings.isCompassEnabled = true

            mMap!!.setOnMapLongClickListener { latLng ->
                val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (address != null && address.isNotEmpty()) {
                    val direccion = address[0].getAddressLine(0)
                    lastMarker?.remove()
                    lastMarker = mMap!!.addMarker(MarkerOptions().position(latLng).title(direccion))
                    currentLocation?.let {
                        drawRoute(GeoPoint(it.latitude, it.longitude), GeoPoint(latLng.latitude, latLng.longitude))
                    }
                }
            }

            binding.searchAddress.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    val texto = binding.searchAddress.text.toString()
                    if (texto.isNotBlank()) {
                        val results = geocoder.getFromLocationName(texto, 1)
                        if (!results.isNullOrEmpty()) {
                            val address = results[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            lastMarker?.remove()
                            lastMarker = mMap!!.addMarker(MarkerOptions().position(latLng).title(address.getAddressLine(0)))
                            currentLocation?.let {
                                drawRoute(GeoPoint(it.latitude, it.longitude), GeoPoint(latLng.latitude, latLng.longitude))
                            }
                        }
                        binding.searchAddress.text.clear()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.searchAddress.windowToken, 0)
                    }
                    true
                } else false
            }
        }
    }


    private fun obtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    mMap?.addMarker(MarkerOptions().position(currentLocation!!).title("Mi ubicación"))
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
                }
            }
        }
    }


    private fun configurarSensorLuz() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.values[0] < 100) {
                    mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@HomeActivity, R.raw.dark))
                } else {
                    mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@HomeActivity, R.raw.retro))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }


    private fun drawRoute(start: GeoPoint, finish: GeoPoint) {
        GlobalScope.launch(Dispatchers.Main) {
            val road = withContext(Dispatchers.IO) {
                roadManager.getRoad(arrayListOf(start, finish))
            }

            val polylineOptions = PolylineOptions()
            road.mRouteHigh.forEach {
                polylineOptions.add(LatLng(it.latitude, it.longitude))
            }

            polylineOptions.color(Color.RED).width(10F)
            mMap?.addPolyline(polylineOptions)
        }
    }




}