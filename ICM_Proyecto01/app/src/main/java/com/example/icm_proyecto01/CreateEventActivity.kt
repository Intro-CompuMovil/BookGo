package com.example.icm_proyecto01

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.icm_proyecto01.databinding.ActivityCreateEventBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.util.*
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.views.overlay.Polyline


class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocation: GeoPoint? = null
    private var selectedLat: Double? = null
    private var selectedLon: Double? = null
    private var selectedAddress: String = ""
    private var roadOverlay: Polyline? = null


    private var markerActual: Marker? = null
    private var markerDestino: Marker? = null

    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        iniciarMapa()
        pedirPermisosUbicacion()

        binding.topAppBar.setOnClickListener { finish() }
        binding.btnSelectDate.setOnClickListener { abrirSelectorFecha() }
        binding.btnSelectTime.setOnClickListener { abrirSelectorHora() }
        binding.btnCreateEvent.setOnClickListener { createEvent() }

        binding.searchAddress.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                buscarDireccion(v.text.toString().trim())
                true
            } else false
        }
    }

    private fun iniciarMapa() {
        val map = binding.osmMap
        map.setMultiTouchControls(true)
        map.controller.setZoom(16.0)
    }

    private fun pedirPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
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
                binding.osmMap.controller.setCenter(currentLocation)

                markerActual?.let { binding.osmMap.overlays.remove(it) }
                markerActual = Marker(binding.osmMap).apply {
                    position = currentLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Mi ubicación"
                    icon = ContextCompat.getDrawable(this@CreateEventActivity, R.drawable.ic_my_location)
                }
                binding.osmMap.overlays.add(markerActual)
                binding.osmMap.invalidate()

                selectedLat = it.latitude
                selectedLon = it.longitude

                try {
                    val addr = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    selectedAddress = addr?.firstOrNull()?.getAddressLine(0) ?: "Ubicación actual"
                } catch (_: Exception) {
                    selectedAddress = "Ubicación actual"
                }
            }
        }
    }

    private fun buscarDireccion(query: String) {
        if (query.isBlank()) return
        val resultados = geocoder.getFromLocationName(query, 1)
        if (!resultados.isNullOrEmpty()) {
            val direccion = resultados[0]
            val punto = GeoPoint(direccion.latitude, direccion.longitude)

            markerDestino?.let { binding.osmMap.overlays.remove(it) }
            markerDestino = Marker(binding.osmMap).apply {
                position = punto
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = direccion.getAddressLine(0)
                icon = ContextCompat.getDrawable(this@CreateEventActivity, R.drawable.ic_event_point)
            }
            binding.osmMap.overlays.add(markerDestino)
            binding.osmMap.controller.animateTo(punto)

            currentLocation?.let { origen ->
                generarRuta(origen, punto)
            }
            binding.osmMap.invalidate()

            selectedLat = punto.latitude
            selectedLon = punto.longitude
            selectedAddress = direccion.getAddressLine(0)
        } else {
            Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generarRuta(origen: GeoPoint, destino: GeoPoint) {
        roadOverlay?.let { binding.osmMap.overlays.remove(it) }

        val roadManager = OSRMRoadManager(this, "AndroidApp")
        val waypoints = arrayListOf<GeoPoint>()
        waypoints.add(origen)
        waypoints.add(destino)

        Thread {
            val road: Road = roadManager.getRoad(waypoints)

            runOnUiThread {
                roadOverlay = RoadManager.buildRoadOverlay(road)
                binding.osmMap.overlays.add(roadOverlay)
                binding.osmMap.invalidate()
            }
        }.start()
    }


    private fun abrirSelectorFecha() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            binding.tvSelectedDate.text = "Fecha: $d/${m + 1}/$y"
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun abrirSelectorHora() {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            binding.tvSelectedTime.text = "Hora: $h:$m"
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun createEvent() {
        val name = binding.etEventName.text.toString().trim()
        val date = binding.tvSelectedDate.text.toString().replace("Fecha: ", "").trim()
        val time = binding.tvSelectedTime.text.toString().replace("Hora: ", "").trim()
        val description = binding.etEventDescription.text.toString().trim()

        if (name.isEmpty() || date == "No seleccionada" || time == "No seleccionada" ||
            description.isEmpty() || selectedLat == null || selectedLon == null
        ) {
            Toast.makeText(this, "Por favor, completa todos los campos y selecciona ubicación", Toast.LENGTH_SHORT).show()
        } else {
            guardarEventoEnFirebase(name, selectedAddress, "$date - $time", description, selectedLat!!, selectedLon!!)
        }
    }

    private fun guardarEventoEnFirebase(name: String, location: String, date: String, description: String, lat: Double, lon: Double) {
        val database = FirebaseDatabase.getInstance().reference
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "spagZw9k5cNfsMUeA5moorwddj72"

        val newEvent = mapOf(
            "userId" to userId,
            "name" to name,
            "location" to location,
            "date" to date,
            "description" to description,
            "lat" to lat,
            "lon" to lon
        )

        database.child("Events").push().setValue(newEvent)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento creado exitosamente", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ExploreActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear evento", Toast.LENGTH_SHORT).show()
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
