package com.example.scaffoldsmart.admin.admin_adapters

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.InventoryRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.InventoryItemIModel
import com.example.scaffoldsmart.databinding.InventoryDetailsDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class InventoryRcvAdapter(
    val context: Context,
    private var itemList: ArrayList<InventoryItemIModel>,
    private val listener: OnItemActionListener
) : RecyclerView.Adapter<InventoryRcvAdapter.MyItemViewHolder>() {

    interface OnItemActionListener {
        fun onEditButtonClick(item: InventoryItemIModel)
    }

    class MyItemViewHolder(val binding: InventoryRcvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(inventory: InventoryItemIModel) {
            binding.itemName.text = inventory.itemName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(InventoryRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.binding.itemName.text = currentItem.itemName
        holder.binding.itemPrice.text = buildString {
            append(currentItem.price)
            append(" .Rs")
        }

        holder.binding.root.setOnClickListener {
            inventoryDetailsDialog(currentItem)
        }

        holder.binding.delItem.setOnClickListener {
            dellInventoryItem(position)
        }

        holder.binding.editItem.setOnClickListener {
            listener.onEditButtonClick(currentItem)
        }
    }

    private fun inventoryDetailsDialog(item: InventoryItemIModel) {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.inventory_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = InventoryDetailsDialogBinding.bind(customDialog)

        binder.itemName.text = item.itemName
        binder.itemQuantity.text = item.quantity
        binder.availability.text = item.availability
        val validItemNames = listOf("pipes", "pipe", "scaffolding pipe")
        if (validItemNames.any { it.equals(item.itemName, ignoreCase = true) }) {
            binder.cardView3.visibility = View.VISIBLE
            binder.itemWeight.visibility = View.VISIBLE
            binder.itemWeight.text = context.getString(R.string._1_kg_per_feet)
            binder.itemPrice.text = buildString {
                append(item.price)
                append(" .Rs per day (per feet)")
            }
        } else {
            binder.itemWeight.text = ""
            binder.itemPrice.text = buildString {
                append(item.price)
                append(" .Rs per day")
            }
        }

        builder.setView(customDialog)
            .setTitle("Inventory Details")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.curved_msg_view_client))
            .setPositiveButton("Ok") { dialog, _ ->
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

    private fun dellInventoryItem(position: Int) {
        val itemToDelete = itemList[position]
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Delete ${itemToDelete.itemName}")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.curved_msg_view_client))
            .setMessage("Do you want to delete the item?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Remove item from database
                Firebase.database.reference.child("Inventory")
                    .child(Firebase.auth.currentUser!!.uid)
                    .child(itemToDelete.itemId)
                    .removeValue()
                    .addOnSuccessListener {
                        // Remove the item from the list
                        itemList.remove(itemToDelete)
                        notifyDataSetChanged()

                        undoDeleteItem(position, itemToDelete)
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

    private fun undoDeleteItem(position: Int, itemToDelete: InventoryItemIModel) {
        val snackbar: Snackbar = Snackbar.make((context as Activity).findViewById(android.R.id.content),
            "Item deleted successfully", Snackbar.LENGTH_LONG)
        snackbar.setAction("UNDO") {
            itemList.add(position, itemToDelete)
            notifyItemInserted(position)
            Firebase.database.reference.child("Inventory")
                .child(Firebase.auth.currentUser!!.uid)
                .child(itemToDelete.itemId)
                .setValue(itemToDelete) // Add it back to the database
        }
        snackbar.show()
        snackbar.setTextColor(Color.BLACK)
        snackbar.setActionTextColor(Color.BLUE)
        snackbar.setBackgroundTint(Color.WHITE)
    }

    fun updateList(newItems: ArrayList<InventoryItemIModel>) {
        itemList = newItems
        notifyDataSetChanged()
    }
}
