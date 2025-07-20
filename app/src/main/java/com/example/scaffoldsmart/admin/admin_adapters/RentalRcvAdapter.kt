package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalItem
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.client.client_adapters.RentalDetailRcvAdapter
import com.example.scaffoldsmart.databinding.RentalRcvItemBinding
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RentalRcvAdapter(
    val context: Context,
    private var rentalList: ArrayList<RentalModel>,
    private val listener: OnItemActionListener
): RecyclerView.Adapter<RentalRcvAdapter.MyRentalViewHolder>() {
    class MyRentalViewHolder(val binding: RentalRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    private lateinit var dialogRcvAdapter: RentalDetailRcvAdapter

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

        currentItem.items?.let { items ->
            // Join all item names with comma separation
            val itemNames = items.mapNotNull { it.itemName } // Extract names and filter nulls
            holder.binding.rentalItems.text = itemNames.joinToString(", ")
            holder.binding.rentalItems.isSelected = true // For marquee effect
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

        setDialogRcv(binder, currentReq.items)

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

    private fun setDialogRcv(binder: RentalsDetailsDialogBinding, items: ArrayList<RentalItem>?) {
        binder.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        // Provide empty list if items is null
        dialogRcvAdapter = RentalDetailRcvAdapter(context, items ?: ArrayList())
        binder.rcv.adapter = dialogRcvAdapter
        binder.rcv.setHasFixedSize(true)
    }

    fun updateList(newItems: ArrayList<RentalModel>) {
        rentalList = newItems
        notifyDataSetChanged()
    }
}

