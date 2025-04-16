package com.example.icm_proyecto01

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.content.pm.PackageManager
import android.location.Location
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.databinding.ActivityCreateExchangePointBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import java.util.*

class CreateExchangePointActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateExchangePointBinding
    private var userName: String? = null

    private lateinit var osmMap: MapView
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var roadManager: RoadManager

    private var currentLocation: GeoPoint? = null
    private var markerDestino: Marker? = null
    private var markerActual: Marker? = null
    private var roadOverlay: Polyline? = null
    private var puntoSeleccionado: GeoPoint? = null

    private var selectedBookTitle: String? = null
    private var selectedBookState: String? = null
    private var selectedBookCoverUrl: String? = null


    private val selectBookLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedBookTitle = data?.getStringExtra("selectedBookTitle")
            selectedBookState = data?.getStringExtra("selectedBookState")
            selectedBookCoverUrl = data?.getStringExtra("selectedBookCoverUrl")


            binding.tvSelectedBook.text = selectedBookTitle ?: "Libro no seleccionado"
            binding.tvEstadoLibro.text = "Estado: ${selectedBookState ?: "-"}"
        }
    }







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateExchangePointBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this)
        roadManager = OSRMRoadManager(this, "ANDROID")

        inicializarMapa()
        pedirPermisos()

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        userName = sharedPref.getString("userName", "Jane Doe")


        binding.tvSelectedBook.setOnClickListener {
            val intent = Intent(this, SelectUserBookActivity::class.java)
            intent.putExtra("from", "createExchange")
            selectBookLauncher.launch(intent)
        }



        binding.btnBack.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.searchAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                buscarDireccion(binding.searchAddress.text.toString())
                true
            } else false
        }

        // Selector de fecha
        binding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                binding.tvSelectedDate.text = "Fecha: $selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day)

            datePicker.show()
        }

        // Selector de hora
        binding.btnSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                binding.tvSelectedTime.text = "Hora: $selectedHour:$selectedMinute"
            }, hour, minute, true)

            timePicker.show()
        }


        binding.btnConfirm.setOnClickListener {
            val bookTitle = selectedBookTitle ?: ""
            val date = binding.tvSelectedDate.text.toString()
            val time = binding.tvSelectedTime.text.toString()

            if (selectedBookTitle.isNullOrEmpty() || selectedBookState.isNullOrEmpty() ||
                date == "Fecha: No seleccionada" || time == "Hora: No seleccionada" || puntoSeleccionado == null
            ) {
                Toast.makeText(this, "Por favor, completa todos los campos y selecciona un libro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val sharedPrefExchange = getSharedPreferences("ExchangePoints", MODE_PRIVATE)
            val editor = sharedPrefExchange.edit()

            val portadaUrlFinal = if (selectedBookCoverUrl != null && selectedBookCoverUrl != "null") selectedBookCoverUrl else ""
            val punto = "$selectedBookTitle|$date|$time|${puntoSeleccionado!!.latitude}|${puntoSeleccionado!!.longitude}|$selectedBookState|$portadaUrlFinal"



            val existingPoints = sharedPrefExchange.getStringSet("points", mutableSetOf()) ?: mutableSetOf()
            existingPoints.add(punto)
            editor.putStringSet("points", existingPoints)
            editor.apply()

            Toast.makeText(this, "Punto de intercambio creado!", Toast.LENGTH_SHORT).show()

            val sharedPrefUser = getSharedPreferences("UserProfile", MODE_PRIVATE)
            with(sharedPrefUser.edit()) {
                putString("userName", userName ?: "Jane Doe")
                apply()
            }

            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("userName", userName ?: "Jane Doe")
            intent.putExtra("focusLat", puntoSeleccionado!!.latitude)
            intent.putExtra("focusLon", puntoSeleccionado!!.longitude)
            startActivity(intent)
            finish()

        }
    }


    private fun inicializarMapa() {
        osmMap = binding.osmMap
        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)
        osmMap.setBuiltInZoomControls(true)
    }

    private fun pedirPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
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


}
