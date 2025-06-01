package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapter.EventAdapter
import com.example.icm_proyecto01.databinding.ActivityExploreBinding
import com.example.icm_proyecto01.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ExploreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExploreBinding
    private lateinit var eventList: MutableList<Event>
    private lateinit var database: DatabaseReference
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference
        userName = getSharedPreferences("UserProfile", MODE_PRIVATE).getString("userName", "Jane Doe")
        eventList = mutableListOf()


        val userId = FirebaseAuth.getInstance().currentUser?.uid
        eventList.sortWith(compareByDescending { it.participants.containsKey(userId) })


        cargarEventos()

        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.btnCreateEvent.setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java))
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_explore
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_explore -> true
                R.id.nav_messages -> {
                    startActivity(Intent(this, ShowHiddenBooksActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userName", userName)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarEventos() {
        database.child("Events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()


                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    val id = eventSnapshot.key

                    if (event != null && !event.name.isNullOrBlank() && !id.isNullOrBlank()) {
                        event.id = id

                            eventList.add(event)

                    }
                }


                val userId = FirebaseAuth.getInstance().currentUser?.uid
                eventList.sortWith(compareByDescending { it.participants.containsKey(userId) })

                binding.rvEvents.adapter = EventAdapter(eventList) { selectedEvent ->
                    val intent = Intent(this@ExploreActivity, EventDetailActivity::class.java).apply {
                        putExtra("EVENT_ID", selectedEvent.id)
                        putExtra("EVENT_NAME", selectedEvent.name)
                        putExtra("EVENT_LOCATION", selectedEvent.location)
                        putExtra("EVENT_DATE", selectedEvent.date)
                        putExtra("EVENT_DESCRIPTION", selectedEvent.description)
                    }
                    startActivityForResult(intent, 1001)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            cargarEventos()
        }
    }

}
