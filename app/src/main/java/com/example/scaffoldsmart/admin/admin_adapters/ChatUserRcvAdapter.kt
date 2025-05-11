package com.example.scaffoldsmart.admin.admin_adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.ChatActivity
import com.example.scaffoldsmart.admin.admin_models.ChatUserModel
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.databinding.ChatUserItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.database.database

class ChatUserRcvAdapter(val context: Context, private var userList: ArrayList<ChatUserModel>): RecyclerView.Adapter<ChatUserRcvAdapter.MyChatUserViewHolder>() {
    class MyChatUserViewHolder(val binding: ChatUserItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatUserViewHolder {
        return MyChatUserViewHolder(ChatUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: MyChatUserViewHolder, position: Int) {
        val currentUser = userList[position]

        // Handle null or empty userName
        val userName = currentUser.userName ?: "Unknown User"
        val name = userName.split("\\s".toRegex()).firstOrNull() ?: userName

        holder.binding.userName.text = name

        // Handle status
        if (currentUser.status.equals("Online", ignoreCase = true)) {
            holder.binding.statusOnline.setImageResource(R.drawable.onlinestatus)
        } else {
            holder.binding.statusOnline.setImageResource(R.drawable.offlinestatus)
        }

        // Set click listener
        holder.binding.root.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("UID", currentUser.uid)
            intent.putExtra("USERNAME", currentUser.userName)
            context.startActivity(intent)
        }
    }

    fun updateList(newUser: ArrayList<ChatUserModel>) {
        userList = newUser
        notifyDataSetChanged()
    }
}