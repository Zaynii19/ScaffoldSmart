package com.example.scaffoldsmart.admin.admin_adapters

import android.annotation.SuppressLint
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
    class MyRentalViewHolder(val binding: RentalRcvItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(rental: RentalModel) {
            // Bind your chat item to UI elements here.
            binding.rentalItems.text = rental.clientName
        }
    }

    interface OnItemActionListener {
        fun onDownloadButtonClick(rental: RentalModel)
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
        if (currentItem.pipes.toString().isNotEmpty()) itemsList.add("Pipes")
        if (currentItem.joints.toString().isNotEmpty()) itemsList.add("Joints")
        if (currentItem.wench.toString().isNotEmpty()) itemsList.add("Wench")
        if (currentItem.pumps.toString().isNotEmpty()) itemsList.add("Pumps")
        if (currentItem.generators.toString().isNotEmpty()) itemsList.add("Generators")
        if (currentItem.wheel.toString().isNotEmpty()) itemsList.add("Wheel")
        if (currentItem.motors.toString().isNotEmpty()) itemsList.add("Motors")

        if (itemsList.isNotEmpty()) {
            holder.binding.rentalItems.text = itemsList.joinToString(", ")
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
        setViewVisibilityAndText(binder.pipes, currentReq.pipes, binder.entry8)
        setViewVisibilityAndText(binder.joints, currentReq.joints, binder.entry10)
        setViewVisibilityAndText(binder.wench, currentReq.wench, binder.entry11)
        setViewVisibilityAndText(binder.slugPumps, currentReq.pumps, binder.entry12)
        setViewVisibilityAndText(binder.motors, currentReq.motors, binder.entry13)
        setViewVisibilityAndText(binder.generators, currentReq.generators, binder.entry14)
        setViewVisibilityAndText(binder.wheel, currentReq.wheel, binder.entry15)

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

    private fun setViewVisibilityAndText(view: TextView, text: Int, entry: ConstraintLayout) {
        if (text.toString().isNotEmpty()) {
            view.text = "$text"
        } else {
            entry.visibility = View.GONE
        }
    }

    fun updateList(newItems: ArrayList<RentalModel>) {
        rentalList = newItems
        notifyDataSetChanged()
    }
}

