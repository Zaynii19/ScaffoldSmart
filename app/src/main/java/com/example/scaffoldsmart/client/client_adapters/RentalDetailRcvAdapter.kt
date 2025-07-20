package com.example.scaffoldsmart.client.client_adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.admin.admin_models.RentalItem
import com.example.scaffoldsmart.admin.admin_models.ScaffoldInfoModel
import com.example.scaffoldsmart.databinding.EntryRcvItemBinding

class RentalDetailRcvAdapter(
    val context: Context,
    private var itemList: ArrayList<RentalItem>,
) : RecyclerView.Adapter<RentalDetailRcvAdapter.MyItemViewHolder>() {

    class MyItemViewHolder(val binding: EntryRcvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(EntryRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.binding.itemName.text = buildString {
            append(currentItem.itemName)
            append(": ")
        }

        holder.binding.itemQty.text = "${currentItem.itemQuantity}"

        val isPipe = currentItem.itemName?.lowercase()?.contains("pipe") == true
        if (isPipe) {
            holder.binding.lengthEntry.visibility = ViewGroup.VISIBLE
            holder.binding.itemLength.text = buildString {
                append(currentItem.pipeLength)
                append(" feet")
            }
        } else {
            holder.binding.lengthEntry.visibility = ViewGroup.GONE
        }
    }

    fun updateList(newItems: ArrayList<RentalItem>) {
        itemList = newItems
        notifyDataSetChanged()
    }
}
