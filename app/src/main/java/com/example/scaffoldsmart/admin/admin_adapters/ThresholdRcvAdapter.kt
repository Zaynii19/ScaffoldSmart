package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.databinding.ThresholdRcvItemBinding

class ThresholdRcvAdapter(
    val context: Context,
    private val itemList: ArrayList<InventoryModel>,
    private val listener: OnItemActionListener
) : RecyclerView.Adapter<ThresholdRcvAdapter.MyItemViewHolder>() {

    class MyItemViewHolder(val binding: ThresholdRcvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        // Track text changes per view holder
        var isTextChanged: Boolean = false
        var newThreshold: Int = 0
    }

    interface OnItemActionListener {
        fun onEditTextTyped(newThresholdQuantity: Int, currentItem: InventoryModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(ThresholdRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.binding.item.hint = "${currentItem.itemName} Threshold"

        // Remove any existing TextWatcher to avoid duplicates
        holder.binding.itemThresholdQuantity.tag?.let {
            holder.binding.itemThresholdQuantity.removeTextChangedListener(it as TextWatcher)
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                holder.isTextChanged = true
                holder.newThreshold = s.toString().toIntOrNull() ?: 0
                listener.onEditTextTyped(holder.newThreshold, currentItem)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        holder.binding.itemThresholdQuantity.addTextChangedListener(textWatcher)
        holder.binding.itemThresholdQuantity.tag = textWatcher

        // Set the initial text based on whether this item has been changed or not
        if (holder.isTextChanged) {
            holder.binding.itemThresholdQuantity.setText(holder.newThreshold.toString())
        } else {
            holder.binding.itemThresholdQuantity.setText(currentItem.threshold?.toString() ?: "")
        }
    }

    fun updateData(newItemList: ArrayList<InventoryModel>) {
        itemList.clear()
        itemList.addAll(newItemList)
        notifyDataSetChanged()
    }
}