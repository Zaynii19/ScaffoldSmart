package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.admin.admin_models.RentalItem
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.InvoiceRcvItemBinding
import com.example.scaffoldsmart.util.DateFormater

class InvoiceDetailRcvAdapter(
    val context: Context,
    private val itemList: ArrayList<RentalItem>,
    private val currentRent: RentalModel,
) : RecyclerView.Adapter<InvoiceDetailRcvAdapter.MyItemViewHolder>() {

    class MyItemViewHolder(val binding: InvoiceRcvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(InvoiceRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.binding.itemName.text = currentItem.itemName
        holder.binding.itemQty.text = "${currentItem.itemQuantity}"
        holder.binding.itemUnitPrice.text = "${currentItem.itemPrice}"

        val diffInDays = DateFormater.calculateDurationInDays(currentRent.startDuration, currentRent.endDuration)
        holder.binding.rentDays.text = "$diffInDays"

        val isPipe = currentItem.itemName?.lowercase()?.contains("pipe") == true
        val itemRent = if (isPipe) {
            currentItem.itemQuantity?.let { q ->
                currentItem.itemPrice?.let { p ->
                    currentItem.pipeLength?.let { l ->
                        q * p * l * diffInDays
                    }
                }
            }
        } else {
            currentItem.itemQuantity?.let { q ->
                currentItem.itemPrice?.let { p ->
                    q * p * diffInDays
                }
            }
        }
        holder.binding.itemRent.text = "$itemRent"
    }
}
