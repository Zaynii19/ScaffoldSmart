package com.example.scaffoldsmart.client.client_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ScafoldInfoRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.ScafoldInfoModel
import com.example.scaffoldsmart.client.client_models.ClientScafoldInfoModel
import com.example.scaffoldsmart.databinding.ClientScafoldInfoRcvItemBinding

class ClientScafoldRcvAdapter (val context: Context, private var infoList: ArrayList<ClientScafoldInfoModel>): RecyclerView.Adapter<ClientScafoldRcvAdapter.MyInfoViewHolder>() {
    class MyInfoViewHolder(val binding: ClientScafoldInfoRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyInfoViewHolder {
        return MyInfoViewHolder(ClientScafoldInfoRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return infoList.size
    }

    override fun onBindViewHolder(holder: MyInfoViewHolder, position: Int) {
        holder.binding.srNo.text = (position + 1).toString()
        holder.binding.rent.text = infoList[position].rent
        holder.binding.duration.text = infoList[position].duration
        holder.binding.status.setBackgroundResource(
            when (infoList[position].rentStatus) {
                "overdue" -> R.drawable.status_red
                "returned" -> R.drawable.status_green
                "ongoing" -> R.drawable.status_blue
                else -> R.drawable.status_blue
            }
        )

    }

    fun updateList(newItems: ArrayList<ClientScafoldInfoModel>) {
        infoList = newItems
        notifyDataSetChanged()
    }

}