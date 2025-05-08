package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ScafoldInfoRcvItemBinding
import com.example.scaffoldsmart.admin.admin_models.ScafoldInfoModel

class ScaffoldRcvAdapter (val context: Context, private var infoList: ArrayList<ScafoldInfoModel>): RecyclerView.Adapter<ScaffoldRcvAdapter.MyInfoViewHolder>() {
    class MyInfoViewHolder(val binding: ScafoldInfoRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyInfoViewHolder {
        return MyInfoViewHolder(ScafoldInfoRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return infoList.size
    }

    override fun onBindViewHolder(holder: MyInfoViewHolder, position: Int) {
        holder.binding.srNo.text = (position + 1).toString()
        holder.binding.clientName.text = infoList[position].clientName
        holder.binding.duration.text = infoList[position].duration
        holder.binding.status.setBackgroundResource(
            when (infoList[position].status) {
                "overdue" -> R.drawable.status_red
                "returned" -> R.drawable.status_green
                "ongoing" -> R.drawable.status_blue
                else -> R.drawable.status_blue
            }
        )
    }

    fun updateList(newItems: ArrayList<ScafoldInfoModel>) {
        infoList = newItems
        notifyDataSetChanged()
    }

}