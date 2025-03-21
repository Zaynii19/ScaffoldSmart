package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ClientRcvItemBinding
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.databinding.ClientDetailsDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ClientRcvAdapter (val context: Context, private var clientList: ArrayList<ClientModel>): RecyclerView.Adapter<ClientRcvAdapter.MyClientViewHolder>() {
    class MyClientViewHolder(val binding: ClientRcvItemBinding): RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyClientViewHolder {
        return MyClientViewHolder(ClientRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return clientList.size
    }

    override fun onBindViewHolder(holder: MyClientViewHolder, position: Int) {
        val currentItem = clientList[position]
        holder.binding.clientName.text = currentItem.name
        holder.binding.root.setOnClickListener {
            clientDetailsDialog(currentItem)
        }

        /*holder.binding.delItem.setOnClickListener {
            dellClient(position)
        }*/

    }

    private fun clientDetailsDialog(currentItem: ClientModel) {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.client_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = ClientDetailsDialogBinding.bind(customDialog)

        binder.clientName.text = currentItem.name
        binder.email.text = currentItem.email
        binder.cnic.text = currentItem.cnic
        binder.address.text = currentItem.address
        binder.phoneNum.text = currentItem.phone

        builder.setView(customDialog)
            .setTitle("Client Details")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.msg_view_received))
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

    fun updateList(newClients: ArrayList<ClientModel>) {
        clientList = newClients
        notifyDataSetChanged()
    }
}