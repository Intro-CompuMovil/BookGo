package com.example.icm_proyecto01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.databinding.ActivityExploreBinding
import org.json.JSONArray
import org.json.JSONObject

class ExploreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExploreBinding
    private lateinit var eventList: MutableList<Event>
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar el nombre de usuario desde SharedPreferences
        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        userName = sharedPref.getString("userName", "Jane Doe")

        eventList = loadEventsFromJSON().toMutableList()

        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.rvEvents.adapter = EventAdapter(eventList) { selectedEvent ->
            val intent = Intent(this, EventDetailActivity::class.java).apply {
                putExtra("EVENT_NAME", selectedEvent.name)
                putExtra("EVENT_LOCATION", selectedEvent.location)
                putExtra("EVENT_DATE", selectedEvent.date)
                putExtra("EVENT_DESCRIPTION", selectedEvent.description)
            }
            startActivity(intent)
        }

        // Botón para crear un nuevo evento
        binding.btnCreateEvent.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java)
            startActivity(intent)
        }

        // Configuración del menú inferior usando binding
        binding.bottomNavigation.selectedItemId = R.id.nav_explore

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_explore -> true
                R.id.nav_messages -> {
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userName", userName)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadEventsFromJSON(): List<Event> {
        val eventList = mutableListOf<Event>()
        val sharedPreferences = getSharedPreferences("EventsData", Context.MODE_PRIVATE)
        val eventsJsonString = sharedPreferences.getString("events", "[]") ?: "[]"

        try {
            val eventsArray = JSONArray(eventsJsonString)

            for (i in 0 until eventsArray.length()) {
                val jsonObject = eventsArray.getJSONObject(i)
                val event = Event(
                    name = jsonObject.getString("name"),
                    location = jsonObject.getString("location"),
                    date = jsonObject.getString("date"),
                    description = jsonObject.getString("description")
                )
                eventList.add(event)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return eventList
    }
}
