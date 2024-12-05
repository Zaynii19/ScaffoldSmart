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
        holder.binding.itemName.text = infoList[position].itemName
        holder.binding.itemQuantity.text = infoList[position].quantity
        holder.binding.duration.text = infoList[position].duration

    }

}