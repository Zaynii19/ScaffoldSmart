package com.example.scaffoldsmart.client.client_adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.databinding.ClientInventoryRcvItemBinding
import com.example.scaffoldsmart.databinding.InventoryDetailsDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ClientInventoryRcvAdapter (val context: Context, private var itemList: ArrayList<InventoryModel>): RecyclerView.Adapter<ClientInventoryRcvAdapter.MyItemViewHolder>() {
    class MyItemViewHolder(val binding: ClientInventoryRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(ClientInventoryRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
    }

    private fun inventoryDetailsDialog(item: InventoryModel) {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.inventory_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = InventoryDetailsDialogBinding.bind(customDialog)

        binder.cardView4.visibility = View.GONE
        binder.availability.visibility = View.GONE

        binder.itemName.text = item.itemName
        binder.itemQuantity.text = item.quantity
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

    fun updateList(newItems: ArrayList<InventoryModel>) {
        itemList = newItems
        notifyDataSetChanged()
    }
}