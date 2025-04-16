package com.example.icm_proyecto01

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.icm_proyecto01.databinding.ItemChatBinding

class ChatAdapter(private val chatList: List<Chat>, private val onClick: (Chat) -> Unit) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position], onClick)
    }

    override fun getItemCount() = chatList.size

    class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat, onClick: (Chat) -> Unit) {
            binding.tvChatName.text = chat.name
            binding.tvLastMessage.text = chat.lastMessage
            binding.root.setOnClickListener { onClick(chat) }
        }
    }
}
