package com.example.icm_proyecto01

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.icm_proyecto01.databinding.ActivityCreateEventBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocation: GeoPoint? = null
    private var selectedLat: Double? = null
    private var selectedLon: Double? = null
    private var selectedAddress: String = ""

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



        binding.btnBack.setOnClickListener { finish() }

        binding.btnSelectDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                binding.tvSelectedDate.text = "Fecha: $d/${m + 1}/$y"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSelectTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                binding.tvSelectedTime.text = "Hora: $h:$m"
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.btnCreateEvent.setOnClickListener {
            createEvent()
        }

        binding.searchAddress.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                buscarDireccion(v.text.toString().trim())
                true
            } else {
                false
            }
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
            binding.osmMap.invalidate()

            selectedLat = punto.latitude
            selectedLon = punto.longitude
            selectedAddress = direccion.getAddressLine(0)
        } else {
            Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
        }
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
            saveEventToJSON(name, selectedAddress, "$date - $time", description, selectedLat!!, selectedLon!!)
            Toast.makeText(this, "Evento creado con éxito", Toast.LENGTH_SHORT).show()
            binding.btnCreateEvent.postDelayed({
                startActivity(Intent(this, ExploreActivity::class.java))
                finish()
            }, 1000)
        }
    }

    private fun saveEventToJSON(name: String, location: String, date: String, description: String, lat: Double, lon: Double) {
        val sharedPreferences = getSharedPreferences("EventsData", Context.MODE_PRIVATE)
        val eventsJsonString = sharedPreferences.getString("events", "[]")
        val eventsArray = JSONArray(eventsJsonString)

        val newEvent = JSONObject().apply {
            put("name", name)
            put("location", location)
            put("date", date)
            put("description", description)
            put("lat", lat)
            put("lon", lon)
        }

        eventsArray.put(newEvent)

        with(sharedPreferences.edit()) {
            putString("events", eventsArray.toString())
            apply()
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
