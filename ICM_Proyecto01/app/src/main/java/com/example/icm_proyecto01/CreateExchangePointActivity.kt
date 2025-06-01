package com.example.icm_proyecto01

import android.Manifest
import android.util.Log
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.content.pm.PackageManager
import android.location.Location
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.databinding.ActivityCreateExchangePointBinding
import com.example.icm_proyecto01.model.UserBook
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


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


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
    private var selectedBookId: String? = null



    private val selectBookLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedBookId = data?.getStringExtra("selectedBookId")
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


        intent.getSerializableExtra("selectedBook")?.let { serializable ->
            val book = serializable as UserBook
            selectedBookId = book.id
            selectedBookTitle = book.titulo
            selectedBookState = book.estado
            selectedBookCoverUrl = book.portadaUrl

            binding.tvSelectedBook.text = selectedBookTitle ?: "Libro no seleccionado"
            binding.tvEstadoLibro.text = "Estado: ${selectedBookState ?: "-"}"
        }


        binding.tvSelectedBook.setOnClickListener {

            val intent = Intent(this, SelectUserBookActivity::class.java)
            intent.putExtra("from", "createExchange")
            selectBookLauncher.launch(intent)
        }


        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
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
                val fecha = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.btnSelectDate.text = fecha
            }, year, month, day)


            datePicker.show()
        }

        // Selector de hora
        binding.btnSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val hora = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.btnSelectTime.text = hora
            }, hour, minute, true)


            timePicker.show()
        }


        binding.btnConfirm.setOnClickListener {
            Log.d("CreateExchange", "Confirm button clicked")

            val bookId = selectedBookId ?: ""
            val bookTitle = selectedBookTitle ?: ""
            val bookState = selectedBookState ?: ""
            val addressInput = binding.searchAddress.text.toString()
            val date = binding.btnSelectDate.text.toString()
            val time = binding.btnSelectTime.text.toString()


            if (bookId.isBlank() || bookState.isBlank() ||
                date == "Fecha: No seleccionada" || time == "Hora: No seleccionada" || puntoSeleccionado == null
            ) {
                Toast.makeText(this, "Por favor, completa todos los campos y selecciona un libro", Toast.LENGTH_SHORT).show()
                Log.e("CreateExchange", "Missing required data, cannot continue")
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                Log.e("CreateExchange", "User not authenticated")
                return@setOnClickListener
            }

            val cleanedDate = date.replace("Fecha: ", "").trim()
            val cleanedTime = time.replace("Hora: ", "").trim()
            val fechaCompleta = "$cleanedDate - $cleanedTime"

            val resolvedAddress = try {
                val addresses = geocoder.getFromLocation(puntoSeleccionado!!.latitude, puntoSeleccionado!!.longitude, 1)
                addresses?.firstOrNull()?.getAddressLine(0) ?: "Dirección no disponible"
            } catch (e: Exception) {
                "Dirección no disponible"
            }

            val exchangePoint = hashMapOf(
                "Book" to hashMapOf(
                    "id" to bookId,
                    "state" to bookState
                ),
                "BookReceiver" to hashMapOf(
                    "id" to "",
                    "state" to ""
                ),
                "address" to addressInput,
                "resolvedAddress" to resolvedAddress,
                "date" to fechaCompleta,
                "exchangeUserId" to userId,
                "creatorUserId" to userId,
                "lat" to puntoSeleccionado!!.latitude,
                "lon" to puntoSeleccionado!!.longitude,
                "receiverUserId" to ""
            )

            val dbRef = FirebaseDatabase.getInstance().reference
            val newExchangePointRef = dbRef.child("ExchangePoints").push()

            newExchangePointRef.setValue(exchangePoint)
                .addOnSuccessListener {
                    Log.i("CreateExchange", "Exchange point successfully created in database")
                    Toast.makeText(this, "Punto de intercambio creado!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("userName", userName ?: "Jane Doe")
                    intent.putExtra("focusLat", puntoSeleccionado!!.latitude)
                    intent.putExtra("focusLon", puntoSeleccionado!!.longitude)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("CreateExchange", "Error al crear punto de intercambio: ${e.message}")
                    Toast.makeText(this, "Error al crear punto de intercambio: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
                    title = "Tu ubicación"
                    icon = ContextCompat.getDrawable(this@CreateExchangePointActivity, R.drawable.ic_my_location)
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
                icon = ContextCompat.getDrawable(this@CreateExchangePointActivity, R.drawable.ic_exchange_point)
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
