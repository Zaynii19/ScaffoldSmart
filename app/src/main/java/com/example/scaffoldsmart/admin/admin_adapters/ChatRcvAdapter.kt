package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.admin.ChatActivity
import com.example.scaffoldsmart.databinding.ConversationRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.ChatModel

class ChatRcvAdapter(val context: Context, private var chatList: ArrayList<ChatModel>): RecyclerView.Adapter<ChatRcvAdapter.MyChatViewHolder>() {
    class MyChatViewHolder(val binding: ConversationRcvItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(chat: ChatModel) {
            // Bind your chat item to UI elements here.
            binding.userName.text = chat.userName
        }
    }

    private var originalChatList = ArrayList(chatList) // Store the original list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatViewHolder {
        return MyChatViewHolder(ConversationRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: MyChatViewHolder, position: Int) {
        holder.binding.userName.text = chatList[position].userName
        holder.binding.message.text = chatList[position].message
        holder.binding.lastMessageTime.text = chatList[position].time
        holder.binding.newMessageCount.text = chatList[position].newMsg.toString()

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("USERNAME", chatList[position].userName)
            context.startActivity(intent)
        }
    }

    // Method to filter chat list based on the search query
    fun filter(query: String) {
        chatList.clear()
        if (query.isEmpty()) {
            chatList.addAll(originalChatList) // Reset to original if query is empty
        } else {
            val filteredList = originalChatList.filter {
                it.userName.contains(query, ignoreCase = true) // Case insensitive matching
            }
            chatList.addAll(filteredList) // Add the filtered results
        }
        notifyDataSetChanged() // Notify the adapter
    }
}