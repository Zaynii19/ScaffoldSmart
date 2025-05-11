package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.ChatActivity
import com.example.scaffoldsmart.admin.admin_models.ChatUserModel
import com.example.scaffoldsmart.databinding.ConversationRcvItemBinding
import com.example.scaffoldsmart.util.DateFormater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database

class RecentChaRcvAdapter(
    val context: Context,
    private var chatList: ArrayList<ChatUserModel>,
    val senderUid: String?,
): RecyclerView.Adapter<RecentChaRcvAdapter.MyChatViewHolder>() {
    class MyChatViewHolder(val binding: ConversationRcvItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatViewHolder {
        return MyChatViewHolder(ConversationRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: MyChatViewHolder, position: Int) {
        val currentChat = chatList[position]
        holder.binding.userName.text = currentChat.userName
        holder.binding.message.text = currentChat.lastMsg
        holder.binding.message.isSelected = true
        holder.binding.lastMessageTime.text = DateFormater.formatTimestampForMsg(currentChat.lastMsgTime)
        if (currentChat.clientNewMsgCount == 0) {
            holder.binding.newMessageCount.visibility = View.GONE
        } else {
            holder.binding.newMessageCount.visibility = View.VISIBLE
            holder.binding.newMessageCount.text = currentChat.clientNewMsgCount.toString()
        }

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("UID", currentChat.uid)
            intent.putExtra("USERNAME", currentChat.userName)
            context.startActivity(intent)
        }

        holder.binding.root.setOnLongClickListener {
            showConfirmationDialog(currentChat, position)
        }
    }

    private fun showConfirmationDialog(currentUser: ChatUserModel, position: Int): Boolean {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Delete Chat")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.msg_view_received))
            .setMessage("Do you want to Delete Chat of ${currentUser.userName}?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteChat(currentUser, position)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.apply {
            show()
            // Set title text color
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            // Set message text color
            findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
            // Set button color
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

        return false
    }

    private fun deleteChat(currentUser: ChatUserModel, position: Int) {
        // Remove item from database
        currentUser.uid?.let { receiverId ->
            senderUid?.let { senderId ->
                val senderRoom = senderId + receiverId
                Firebase.database.reference.child("Chat").child(senderRoom)
                    .removeValue()
                    .addOnSuccessListener {
                        // Remove the item from the list
                        chatList.remove(currentUser)
                        notifyItemRemoved(position)
                        updateChatUser(currentUser)
                        Toast.makeText(context, "Chat deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to delete chat", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun updateChatUser(currentUser: ChatUserModel) {
        currentUser.uid?.let { receiverId ->
            val updates = mapOf(
                "lastMsg" to null,
                "lastMsgTime" to null,
                "clientNewMsgCount" to null
            )

            Firebase.database.reference.child("ChatUser").child(receiverId)
                .updateChildren(updates)
                .addOnSuccessListener {}
                .addOnFailureListener {}
        }

        /*currentUser.uid?.let { receiverId ->
            val userRef = Firebase.database.reference.child("ChatUser").child(receiverId)

            userRef.child("lastMsg").removeValue()
            userRef.child("lastMsgTime").removeValue()
            userRef.child("clientNewMsgCount").removeValue()
        }*/
    }

    fun updateList(newChat: ArrayList<ChatUserModel>) {
        chatList = newChat
        notifyDataSetChanged()
    }
}