package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.ChatModel
import com.example.scaffoldsmart.databinding.RentalRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.RentalModel

class RentalRcvAdapter (val context: Context, private var rentalList: ArrayList<RentalModel>): RecyclerView.Adapter<RentalRcvAdapter.MyRentalViewHolder>() {
    class MyRentalViewHolder(val binding: RentalRcvItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(rental: RentalModel) {
            // Bind your chat item to UI elements here.
            binding.rentalItems.text = rental.item
        }
    }

    private var originalRentalList = ArrayList(rentalList) // Store the original list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRentalViewHolder {
        return MyRentalViewHolder(RentalRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return rentalList.size
    }

    override fun onBindViewHolder(holder: MyRentalViewHolder, position: Int) {
        holder.binding.clientName.text = rentalList[position].clientName

        holder.binding.rentalItems.text = rentalList[position].item

        holder.binding.status.setBackgroundResource(
            when (rentalList[position].status) {
                "ongoing" -> R.drawable.status_blue
                "returned" -> R.drawable.status_green
                "overdue" -> R.drawable.status_red
                else -> {
                    R.drawable.status_blue
                }
            }
        )
    }

    // Method to filter rent list based on the search query
    fun filter(query: String) {
        rentalList.clear()
        if (query.isEmpty()) {
            rentalList.addAll(originalRentalList) // Reset to original if query is empty
        } else {
            val filteredList = originalRentalList.filter {
                it.item.contains(query, ignoreCase = true) // Case insensitive matching
            }
            rentalList.addAll(filteredList) // Add the filtered results
        }
        notifyDataSetChanged() // Notify the adapter
    }
}

