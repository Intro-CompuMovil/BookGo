package com.example.icm_proyecto01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.databinding.ActivityMessagesBinding

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private var userName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)
        userName = sharedPref.getString("userName", "Jane Doe")

        // Supuestos chats
        val chatList = listOf(
            Chat("Juan", "Hola, ¿cómo estás?"),
            Chat("Vale", "¡Te tengo un libro para intercambiar!")
        )

        // Configuración del RecyclerView usando binding
        binding.rvChats.layoutManager = LinearLayoutManager(this)
        binding.rvChats.adapter = ChatAdapter(chatList) { selectedChat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("CHAT_NAME", selectedChat.name)
            startActivity(intent)
        }

        // Configuración del menú inferior usando binding
        binding.bottomNavigation.selectedItemId = R.id.nav_messages
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_messages -> true

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
}
