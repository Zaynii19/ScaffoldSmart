package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.admin.ChatActivity
import com.example.scaffoldsmart.admin.admin_models.ChatUserModel
import com.example.scaffoldsmart.databinding.ConversationRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.RecentChatModel
import com.example.scaffoldsmart.util.DateFormater

class RecentChaRcvAdapter(
    val context: Context,
    private var chatList: ArrayList<ChatUserModel>,
    private val dateFormatter: DateFormater?
): RecyclerView.Adapter<RecentChaRcvAdapter.MyChatViewHolder>() {
    class MyChatViewHolder(val binding: ConversationRcvItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatViewHolder {
        return MyChatViewHolder(ConversationRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: MyChatViewHolder, position: Int) {
        val chatItem = chatList[position]
        holder.binding.userName.text = chatItem.userName
        holder.binding.message.text = chatItem.lastMsg
        holder.binding.lastMessageTime.text = dateFormatter!!.formatTimestampForMsg(chatItem.lastMsgTime)
        if (chatItem.clientNewMsgCount == 0) {
            holder.binding.newMessageCount.visibility = View.GONE
        } else {
            holder.binding.newMessageCount.visibility = View.VISIBLE
            holder.binding.newMessageCount.text = chatItem.clientNewMsgCount.toString()
        }

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("UID", chatItem.uid)
            intent.putExtra("USERNAME", chatItem.userName)
            context.startActivity(intent)
        }
    }

    fun updateList(newChat: ArrayList<ChatUserModel>) {
        chatList = newChat
        notifyDataSetChanged()
    }
}