package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ClientRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.ClientModel

class ClientRcvAdapter (val context: Context, private var clientList: ArrayList<ClientModel>): RecyclerView.Adapter<ClientRcvAdapter.MyClientViewHolder>() {
    class MyClientViewHolder(val binding: ClientRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyClientViewHolder {
        return MyClientViewHolder(ClientRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return clientList.size
    }

    override fun onBindViewHolder(holder: MyClientViewHolder, position: Int) {
        holder.binding.clientName.text = clientList[position].clientName

        val gender = clientList[position].gender
        val status = clientList[position].status

        holder.binding.userProfile.setImageResource(
            when (gender) {
                "Male" -> R.drawable.man
                "Female" -> R.drawable.woman
                else -> {
                    R.drawable.man
                }
            }
        )

        holder.binding.status.setBackgroundResource(
            when (status) {
                "Ongoing" -> R.drawable.status_blue
                "Returned" -> R.drawable.status_green
                "Overdue" -> R.drawable.status_red
                else -> {
                    R.drawable.status_blue
                }
            }
        )



    }
}