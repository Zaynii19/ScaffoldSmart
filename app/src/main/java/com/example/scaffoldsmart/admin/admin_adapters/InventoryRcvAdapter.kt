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
import com.example.scaffoldsmart.databinding.InventoryRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.InventoryItemIModel
import com.example.scaffoldsmart.databinding.InventoryDetailsDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InventoryRcvAdapter (val context: Context, private var itemList: ArrayList<InventoryItemIModel>): RecyclerView.Adapter<InventoryRcvAdapter.MyItemViewHolder>() {
    class MyItemViewHolder(val binding: InventoryRcvItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(inventory: InventoryItemIModel) {
            // Bind your chat item to UI elements here.
            binding.itemName.text = inventory.itemName
        }
    }

    private var originalInventoryList = ArrayList(itemList) // Store the original list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(InventoryRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        // Get the current inventory item based on the position
        val currentItem = itemList[position]
        holder.binding.itemName.text = currentItem.itemName
        holder.binding.itemPrice.text = buildString {
            append(currentItem.price)
            append(" .Rs")
        }

        holder.binding.root.setOnClickListener {
            inventoryDetailsDialog(currentItem)
        }
    }

    private fun inventoryDetailsDialog(item: InventoryItemIModel){
        val customDialog = LayoutInflater.from(context).inflate(R.layout.inventory_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = InventoryDetailsDialogBinding.bind(customDialog)

        // Set the values of the dialog elements based on the inventory item
        binder.itemName.text = item.itemName
        binder.itemQuantity.text = item.quantity
        binder.availability.text = item.availability
        val validItemNames = listOf("pipes", "pipe", "scaffolding pipe")
        if (validItemNames.any { it.equals(item.itemName, ignoreCase = true) }) {
            binder.itemWeight.text = context.getString(R.string._1_kg_per_feet)
            binder.itemPrice.text = buildString {
                append(item.price)
                append(" .Rs per day (per feet)")
            }
        }else {
            binder.itemWeight.text = ""
            binder.itemPrice.text = buildString {
                append(item.price)
                append(" .Rs per day")
            }
        }

        builder.setView(customDialog)
            .setTitle("Inventory Details")
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

    // Method to filter inventory list based on the search query
    fun filter(query: String) {
        itemList.clear()
        if (query.isEmpty()) {
            itemList.addAll(originalInventoryList) // Reset to original if query is empty
        } else {
            val filteredList = originalInventoryList.filter {
                it.itemName.contains(query, ignoreCase = true) // Case insensitive matching
            }
            itemList.addAll(filteredList) // Add the filtered results
        }
        notifyDataSetChanged() // Notify the adapter
    }
}