package com.example.scaffoldsmart.client.client_adapters

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
import com.example.scaffoldsmart.client.client_models.ClientInventoryItemIModel
import com.example.scaffoldsmart.databinding.ClientInventoryRcvItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ClientInventoryRcvAdapter (val context: Context, private var itemList: ArrayList<ClientInventoryItemIModel>): RecyclerView.Adapter<ClientInventoryRcvAdapter.MyItemViewHolder>() {
    class MyItemViewHolder(val binding: ClientInventoryRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(ClientInventoryRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
}