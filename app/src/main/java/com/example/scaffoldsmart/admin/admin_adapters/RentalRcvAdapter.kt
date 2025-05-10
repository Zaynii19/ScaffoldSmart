package com.example.scaffoldsmart.admin.admin_adapters

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
import com.example.scaffoldsmart.databinding.RentalRcvItemBinding
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RentalRcvAdapter(
    val context: Context,
    private var rentalList: ArrayList<RentalModel>,
    private val listener: OnItemActionListener
): RecyclerView.Adapter<RentalRcvAdapter.MyRentalViewHolder>() {
    class MyRentalViewHolder(val binding: RentalRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    interface OnItemActionListener {
        fun onDownloadButtonClick(rental: RentalModel)
        fun onDoneRentalButtonClick(rental: RentalModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRentalViewHolder {
        return MyRentalViewHolder(RentalRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return rentalList.size
    }

    override fun onBindViewHolder(holder: MyRentalViewHolder, position: Int) {
        val currentItem = rentalList[position]
        holder.binding.clientName.text = currentItem.clientName

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

        holder.binding.downloadContract.setOnClickListener {
            listener.onDownloadButtonClick(currentItem)
        }

        holder.binding.doneRental.setOnClickListener {
            listener.onDoneRentalButtonClick(currentItem)
        }

        if (currentItem.rentStatus == "returned") {
            holder.binding.doneRental.visibility = View.GONE
        }

        holder.binding.root.setOnClickListener {
            showRentDetailsDialog(currentItem)
        }
    }

    private fun showRentDetailsDialog(currentReq: RentalModel) {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.rentals_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = RentalsDetailsDialogBinding.bind(customDialog)

        binder.clientName.text = currentReq.clientName
        binder.address.text = currentReq.clientAddress
        binder.phoneNum.text = currentReq.clientPhone
        binder.email.text = currentReq.clientEmail
        binder.cnic.text = currentReq.clientCnic
        binder.rent.text = buildString {
            append(currentReq.rent)
            append(" .Rs")
        }
        binder.rentalAddress.text = currentReq.rentalAddress
        binder.rentalDurationFrom.text = currentReq.startDuration
        binder.rentalDurationTo.text = currentReq.endDuration

        // Regular quantities (just numbers)
        currentReq.pipes?.let { setViewVisibilityAndText(binder.pipes, it, binder.entry8) }
        currentReq.joints?.let { setViewVisibilityAndText(binder.joints, it, binder.entry10) }
        currentReq.wench?.let { setViewVisibilityAndText(binder.wench, it, binder.entry11) }
        currentReq.pumps?.let { setViewVisibilityAndText(binder.slugPumps, it, binder.entry12) }
        currentReq.motors?.let { setViewVisibilityAndText(binder.motors, it, binder.entry13) }
        currentReq.generators?.let { setViewVisibilityAndText(binder.generators, it, binder.entry14) }
        currentReq.wheel?.let { setViewVisibilityAndText(binder.wheel, it, binder.entry15) }

        // Special case for pipe length (with "feet" unit)
        if (currentReq.pipesLength != 0) {
            binder.pipesLength.text = buildString {
                append(currentReq.pipesLength)
                append(" feet")
            }
            binder.entry9.visibility = View.VISIBLE
        } else {
            binder.entry9.visibility = View.GONE
        }

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
        if (quantity != 0) {
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

