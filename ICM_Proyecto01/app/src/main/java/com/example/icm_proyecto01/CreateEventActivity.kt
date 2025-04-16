package com.example.icm_proyecto01

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.icm_proyecto01.databinding.ActivityCreateEventBinding
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding
    private var selectedLat: Double? = null
    private var selectedLon: Double? = null
    private var selectedAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val map = binding.osmMap
        val geocoder = Geocoder(this, Locale.getDefault())
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        map.controller.setCenter(GeoPoint(4.6482837, -74.2478947)) // Bogotá

        fun addMarkerAt(point: GeoPoint, label: String = "Ubicación seleccionada") {
            map.overlays.clear()
            val marker = Marker(map).apply {
                position = point
                title = label
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            map.overlays.add(marker)
            map.invalidate()
            selectedLat = point.latitude
            selectedLon = point.longitude
            try {
                val addr = geocoder.getFromLocation(point.latitude, point.longitude, 1)
                selectedAddress = addr?.firstOrNull()?.getAddressLine(0) ?: label
            } catch (_: Exception) {
                selectedAddress = label
            }
        }

        binding.searchAddress.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    try {
                        val addresses = geocoder.getFromLocationName(query, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val location = addresses[0]
                            val point = GeoPoint(location.latitude, location.longitude)
                            map.controller.animateTo(point)
                            addMarkerAt(point, location.getAddressLine(0))
                        } else {
                            Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error buscando dirección", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            } else false
        }

        map.setOnTouchListener { _, event ->
            val projection = map.projection
            val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
            addMarkerAt(geoPoint)
            false
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSelectDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d -> binding.tvSelectedDate.text = "Fecha: $d/${m + 1}/$y" },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSelectTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m -> binding.tvSelectedTime.text = "Hora: $h:$m" },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.btnCreateEvent.setOnClickListener {
            val name = binding.etEventName.text.toString().trim()
            val date = binding.tvSelectedDate.text.toString().replace("Fecha: ", "").trim()
            val time = binding.tvSelectedTime.text.toString().replace("Hora: ", "").trim()
            val description = binding.etEventDescription.text.toString().trim()

            if (name.isEmpty() || date == "No seleccionada" || time == "No seleccionada" ||
                description.isEmpty() || selectedLat == null || selectedLon == null) {
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
    }

    private fun saveEventToJSON(
        name: String, location: String, date: String,
        description: String, lat: Double, lon: Double
    ) {
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
}
