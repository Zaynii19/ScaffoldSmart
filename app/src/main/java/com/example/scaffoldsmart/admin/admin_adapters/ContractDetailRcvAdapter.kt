package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.admin.admin_models.RentalItem
import com.example.scaffoldsmart.databinding.ContractRcvItemBinding
import com.example.scaffoldsmart.util.DateFormater

class ContractDetailRcvAdapter(
    val context: Context,
    private val itemList: ArrayList<RentalItem>,
) : RecyclerView.Adapter<ContractDetailRcvAdapter.MyItemViewHolder>() {

    class MyItemViewHolder(val binding: ContractRcvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(ContractRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.binding.itemName.text = currentItem.itemName
        holder.binding.itemName.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        holder.binding.itemQuantity.text = "${currentItem.itemQuantity}"
        holder.binding.itemQuantity.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        val isPipe = currentItem.itemName?.lowercase()?.contains("pipe") == true
        if (isPipe) {
            holder.binding.itemLength.text = buildString {
                append(currentItem.pipeLength)
                append(" feet")
            }
            holder.binding.itemLength.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        } else {
            holder.binding.itemLength.visibility = View.GONE
        }
    }
}
