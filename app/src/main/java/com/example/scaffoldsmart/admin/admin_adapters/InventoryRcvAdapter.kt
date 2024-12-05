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
import com.example.scaffoldsmart.admin.admin_models.RentalModel
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
        holder.binding.itemName.text = itemList[position].itemName
        holder.binding.itemPrice.text = buildString {
            append(itemList[position].price)
            append(" .Rs")
        }

        holder.binding.root.setOnClickListener {
            inventoryDetailsDialog()
        }
    }

    private fun inventoryDetailsDialog(){
        val customDialog = LayoutInflater.from(context).inflate(R.layout.inventory_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
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