package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.databinding.InventoryRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.InventoryItemIModel

class InventoryRcvAdapter (val context: Context, private var itemList: ArrayList<InventoryItemIModel>): RecyclerView.Adapter<InventoryRcvAdapter.MyItemViewHolder>() {
    class MyItemViewHolder(val binding: InventoryRcvItemBinding): RecyclerView.ViewHolder(binding.root)

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
    }
}