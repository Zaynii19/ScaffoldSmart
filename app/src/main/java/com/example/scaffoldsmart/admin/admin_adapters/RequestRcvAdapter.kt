package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.example.scaffoldsmart.databinding.ReqRcvItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database

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

