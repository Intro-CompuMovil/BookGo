package com.example.icm_proyecto01

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.icm_proyecto01.adapter.ChatMessageAdapter
import com.example.icm_proyecto01.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatMessageAdapter
    private val messageList = ArrayList<String>() // Lista de mensajes
    private lateinit var sharedPreferences: SharedPreferences
    private var chatName: String = "Chat"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatName = intent.getStringExtra("CHAT_NAME") ?: "Chat"

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE)
        loadMessages()

        chatAdapter = ChatMessageAdapter(messageList)
        binding.rvChatMessages.layoutManager = LinearLayoutManager(this)
        binding.rvChatMessages.adapter = chatAdapter

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                messageList.add("Yo: $message") // Agregar mensaje a la lista
                chatAdapter.notifyItemInserted(messageList.size - 1) // Notificar al adaptador
                binding.rvChatMessages.scrollToPosition(messageList.size - 1) // Auto-scroll al último mensaje
                binding.etMessage.text.clear() // Limpiar campo de texto

                // Guardar mensajes en SharedPreferences
                saveMessages()
            } else {
                Toast.makeText(this, "Escribe un mensaje para enviar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMessages() {
        val savedMessages = sharedPreferences.getStringSet(chatName, emptySet()) ?: emptySet()
        messageList.addAll(savedMessages) // Cargar mensajes previos
    }

    private fun saveMessages() {
        val editor = sharedPreferences.edit()
        editor.putStringSet(chatName, messageList.toSet()) // Guardar la conversación
        editor.apply()
    }
}
