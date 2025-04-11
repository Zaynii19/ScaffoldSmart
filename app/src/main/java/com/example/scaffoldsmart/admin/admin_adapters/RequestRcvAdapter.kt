package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.ReqRcvItemBinding

class RequestRcvAdapter(
    val context: Context,
    private var reqList: ArrayList<RentalModel>,
    private val listener: OnItemActionListener
): RecyclerView.Adapter<RequestRcvAdapter.MyRequestViewHolder>() {

    class MyRequestViewHolder(val binding: ReqRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    interface OnItemActionListener {
        fun onReqUpdateListener(currentReq: RentalModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRequestViewHolder {
        return MyRequestViewHolder(ReqRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return reqList.size
    }

    override fun onBindViewHolder(holder: MyRequestViewHolder, position: Int) {

        val currentReq = reqList[position]
        holder.binding.clientName.text = currentReq.clientName

        holder.binding.root.setOnClickListener {
            listener.onReqUpdateListener(currentReq)
        }
    }

    fun updateList(newReqList: ArrayList<RentalModel>) {
        reqList = newReqList
        notifyDataSetChanged()
    }
}

