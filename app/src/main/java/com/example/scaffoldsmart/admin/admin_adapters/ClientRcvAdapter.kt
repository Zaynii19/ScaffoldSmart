package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.ChatActivity
import com.example.scaffoldsmart.databinding.ClientRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.ClientModel
import com.example.scaffoldsmart.admin.admin_models.InventoryItemIModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ClientRcvAdapter (val context: Context, private var clientList: ArrayList<ClientModel>): RecyclerView.Adapter<ClientRcvAdapter.MyClientViewHolder>() {
    class MyClientViewHolder(val binding: ClientRcvItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(client: ClientModel) {
            // Bind your chat item to UI elements here.
            binding.clientName.text = client.clientName
        }
    }

    private var originalClientList = ArrayList(clientList) // Store the original list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyClientViewHolder {
        return MyClientViewHolder(ClientRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return clientList.size
    }

    override fun onBindViewHolder(holder: MyClientViewHolder, position: Int) {
        holder.binding.clientName.text = clientList[position].clientName

        holder.binding.root.setOnClickListener {
            clientDetailsDialog()
        }

    }

    private fun clientDetailsDialog(){
        val customDialog = LayoutInflater.from(context).inflate(R.layout.client_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(customDialog)
            .setTitle("Client Details")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.curved_msg_view_client))
            .setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }.create().apply {
                show()
                // Set title text color
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
                // Set button color
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            }


    }

    // Method to filter client list based on the search query
    fun filter(query: String) {
        clientList.clear()
        if (query.isEmpty()) {
            clientList.addAll(originalClientList) // Reset to original if query is empty
        } else {
            val filteredList = originalClientList.filter {
                it.clientName.contains(query, ignoreCase = true) // Case insensitive matching
            }
            clientList.addAll(filteredList) // Add the filtered results
        }
        notifyDataSetChanged() // Notify the adapter
    }
}