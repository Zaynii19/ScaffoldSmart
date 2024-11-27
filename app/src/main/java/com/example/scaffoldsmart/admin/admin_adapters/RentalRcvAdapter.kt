package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.RentalRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.RentalModel

class RentalRcvAdapter (val context: Context, private var rentalList: ArrayList<RentalModel>): RecyclerView.Adapter<RentalRcvAdapter.MyRentalViewHolder>() {
    class MyRentalViewHolder(val binding: RentalRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRentalViewHolder {
        return MyRentalViewHolder(RentalRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return rentalList.size
    }

    override fun onBindViewHolder(holder: MyRentalViewHolder, position: Int) {
        holder.binding.clientName.text = rentalList[position].clientName
        holder.binding.item1.text = rentalList[position].item
        holder.binding.item1Quantity.text = rentalList[position].quantity
        holder.binding.address.text = rentalList[position].address
        holder.binding.cost.text = rentalList[position].cost
        holder.binding.duration.text = rentalList[position].duration

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

        holder.binding.userProfile.setImageResource(
            when (rentalList[position].gender) {
                "male" -> R.drawable.man
                "female" -> R.drawable.woman
                else -> {
                    R.drawable.man
                }
            }
        )
    }
}