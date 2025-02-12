package com.example.scaffoldsmart.admin.admin_adapters

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.databinding.ClientRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.RentalClientModel
import com.example.scaffoldsmart.databinding.ClientDetailsDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class ClientRcvAdapter (val context: Context, private var clientList: ArrayList<RentalClientModel>): RecyclerView.Adapter<ClientRcvAdapter.MyClientViewHolder>() {
    class MyClientViewHolder(val binding: ClientRcvItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(client: RentalClientModel) {
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
        val currentItem = clientList[position]
        holder.binding.clientName.text = currentItem.clientName
        holder.binding.root.setOnClickListener {
            clientDetailsDialog(currentItem)
        }

        /*holder.binding.delItem.setOnClickListener {
            dellClient(position)
        }*/

    }

    private fun clientDetailsDialog(currentItem: RentalClientModel) {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.client_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = ClientDetailsDialogBinding.bind(customDialog)

        binder.clientName.text = currentItem.clientName
        binder.email.text = currentItem.email
        binder.cnic.text = currentItem.cnic
        binder.address.text = currentItem.address
        binder.phoneNum.text = currentItem.phone

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

/*    private fun dellClient(position: Int) {
        val clientToDelete = clientList[position]
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Delete ${clientToDelete.clientName}")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.curved_msg_view_client))
            .setMessage("Do you want to delete the client?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Remove item from database
                Firebase.database.reference.child("Rentals")
                    .child(Firebase.auth.currentUser!!.uid)
                    .child(clientToDelete.rentalId)
                    .removeValue()
                    .addOnSuccessListener {
                        // Remove the item from the list
                        clientList.remove(clientToDelete)
                        notifyDataSetChanged()

                        undoDeleteClient(position, clientToDelete)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                    }
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
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
        }
    }

    private fun undoDeleteClient(position: Int, clientToDelete: RentalClientModel) {
        val snackbar: Snackbar = Snackbar.make((context as Activity).findViewById(android.R.id.content),
            "Client deleted successfully", Snackbar.LENGTH_LONG)
        snackbar.setAction("UNDO") {
            clientList.add(position, clientToDelete)
            notifyItemInserted(position)
            Firebase.database.reference.child("Rentals")
                .child(Firebase.auth.currentUser!!.uid)
                .child(clientToDelete.rentalId)
                .setValue(clientToDelete) // Add it back to the database
        }
        snackbar.show()
        snackbar.setTextColor(Color.BLACK)
        snackbar.setActionTextColor(Color.BLUE)
        snackbar.setBackgroundTint(Color.WHITE)
    }*/

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

    fun updateList(newItems: ArrayList<RentalClientModel>) {
        clientList = newItems
        notifyDataSetChanged()
    }
}