package com.example.scaffoldsmart.client.client_adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.ClientRentalRcvItemBinding
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ClientRentalRcvAdapter (val context: Context, private var rentalList: ArrayList<RentalModel>): RecyclerView.Adapter<ClientRentalRcvAdapter.MyRentalViewHolder>() {
    class MyRentalViewHolder(val binding: ClientRentalRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRentalViewHolder {
        return MyRentalViewHolder(ClientRentalRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return rentalList.size
    }

    override fun onBindViewHolder(holder: MyRentalViewHolder, position: Int) {
        val currentItem = rentalList[position]

        val itemsList = mutableListOf<String>()
        if (currentItem.pipes != 0) itemsList.add("Scaffolding Pipes")
        if (currentItem.joints != 0) itemsList.add("Joints")
        if (currentItem.wench != 0) itemsList.add("Wench")
        if (currentItem.pumps != 0) itemsList.add("Pumps")
        if (currentItem.generators != 0) itemsList.add("Generators")
        if (currentItem.wheel != 0) itemsList.add("Wheel")
        if (currentItem.motors != 0) itemsList.add("Motors")

        if (itemsList.isNotEmpty()) {
            holder.binding.rentalItems.text = itemsList.joinToString(", ")
            holder.binding.rentalItems.isSelected = true
        } else {
            holder.binding.rentalItems.text = ""
        }

        holder.binding.status.setBackgroundResource(
            when (currentItem.rentStatus) {
                "ongoing" -> R.drawable.status_blue
                "returned" -> R.drawable.status_green
                "overdue" -> R.drawable.status_red
                else -> {
                    R.drawable.status_blue
                }
            }
        )

        holder.binding.rent.text = buildString {
            append("Total Rent: ")
            append(currentItem.rent)
            append(" .Rs")
        }

        holder.binding.root.setOnClickListener {
            showRentDetailsDialog(currentItem)
        }
    }

    private fun showRentDetailsDialog(currentReq: RentalModel) {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.rentals_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = RentalsDetailsDialogBinding.bind(customDialog)

        binder.textView.visibility = View.GONE
        binder.entry1.visibility = View.GONE
        binder.entry2.visibility = View.GONE
        binder.entry3.visibility = View.GONE
        binder.entry4.visibility = View.GONE
        binder.entry5.visibility = View.GONE
        binder.line.visibility = View.GONE
        binder.textView6.visibility = View.GONE
        binder.rentalDurationFrom.text = currentReq.startDuration
        binder.rentalDurationTo.text = currentReq.endDuration
        binder.rent.text = "${currentReq.rent}"
        binder.rentalAddress.text = currentReq.rentalAddress
        currentReq.pipes?.let { setViewVisibilityAndText(binder.pipes, it, binder.entry8) }
        currentReq.pipesLength?.let { setViewVisibilityAndText(binder.pipesLength, it, binder.entry9) }
        currentReq.joints?.let { setViewVisibilityAndText(binder.joints, it, binder.entry10) }
        currentReq.wench?.let { setViewVisibilityAndText(binder.wench, it, binder.entry11) }
        currentReq.pumps?.let { setViewVisibilityAndText(binder.slugPumps, it, binder.entry12) }
        currentReq.motors?.let { setViewVisibilityAndText(binder.motors, it, binder.entry13) }
        currentReq.generators?.let { setViewVisibilityAndText(binder.generators, it, binder.entry14) }
        currentReq.wheel?.let { setViewVisibilityAndText(binder.wheel, it, binder.entry15) }

        builder.setView(customDialog)
            .setTitle("Rental Details")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.msg_view_received))
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }.create().apply {
                show()
                // Set title text color
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
                // Set button color
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            }
    }

    private fun setViewVisibilityAndText(view: TextView, quantity: Int, entry: ConstraintLayout) {
        if (quantity !=0 ) {
            view.text = "$quantity"
        } else {
            entry.visibility = View.GONE
        }
    }

    fun updateList(newItems: ArrayList<RentalModel>) {
        rentalList = newItems
        notifyDataSetChanged()
    }
}

