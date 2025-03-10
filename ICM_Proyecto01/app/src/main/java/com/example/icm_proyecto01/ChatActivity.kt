package com.example.icm_proyecto01

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatMessageAdapter
    private val messageList = ArrayList<String>() // Lista de mensajes
    private lateinit var sharedPreferences: SharedPreferences
    private var chatName: String = "Chat"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val rvChatMessages = findViewById<RecyclerView>(R.id.rvChatMessages)

        // Obtener el nombre del chat para identificar la conversación
        chatName = intent.getStringExtra("CHAT_NAME") ?: "Chat"

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE)

        // Cargar mensajes guardados
        loadMessages()

        // Configurar RecyclerView
        chatAdapter = ChatMessageAdapter(messageList)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = chatAdapter

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                messageList.add("Yo: $message") // Agregar mensaje a la lista
                chatAdapter.notifyItemInserted(messageList.size - 1) // Notificar al adaptador
                rvChatMessages.scrollToPosition(messageList.size - 1) // Auto-scroll al último mensaje
                etMessage.text.clear() // Limpiar campo de texto

                // Guardar mensajes en SharedPreferences
                saveMessages()
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
