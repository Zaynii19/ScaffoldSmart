package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalReqModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalReqViewModel
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.example.scaffoldsmart.databinding.ReqRcvItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class RequestRcvAdapter(
    val context: Context,
    private var reqList: ArrayList<RentalReqModel>,
    private val viewModel: RentalReqViewModel,
    private val listener: OnItemActionListener
): RecyclerView.Adapter<RequestRcvAdapter.MyRequestViewHolder>() {

    class MyRequestViewHolder(val binding: ReqRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    interface OnItemActionListener {
        fun onDialogDismissed()
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
            reqDetailsDialog(currentReq)
        }
    }

    private fun reqDetailsDialog(currentReq: RentalReqModel) {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.rentals_details_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = RentalsDetailsDialogBinding.bind(customDialog)

        binder.clientName.text = currentReq.clientName
        binder.address.text = currentReq.rentalAddress
        binder.phoneNum.text = currentReq.clientPhone
        binder.email.text = currentReq.clientEmail
        binder.cnic.text = currentReq.clientCnic
        binder.rentalDurationFrom.text = currentReq.startDuration
        binder.rentalDurationTo.text = currentReq.endDuration
        setViewVisibilityAndText(binder.pipes, currentReq.pipes, binder.entry8)
        setViewVisibilityAndText(binder.pipesLength, currentReq.pipesLength, binder.entry9)
        setViewVisibilityAndText(binder.joints, currentReq.joints, binder.entry10)
        setViewVisibilityAndText(binder.wench, currentReq.wench, binder.entry11)
        setViewVisibilityAndText(binder.slugPumps, currentReq.pumps, binder.entry12)
        setViewVisibilityAndText(binder.motors, currentReq.motors, binder.entry13)
        setViewVisibilityAndText(binder.generators, currentReq.generators, binder.entry14)
        setViewVisibilityAndText(binder.wheel, currentReq.wheel, binder.entry15)

        builder.setView(customDialog)
            .setTitle("Rental Request Details")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.curved_msg_view_client))
            .setPositiveButton("Approve") { dialog, _ ->
                approveRentalReq(currentReq)
                dialog.dismiss()
            }.setNegativeButton("Reject"){ dialog, _ ->
                delRentalReq(currentReq)
                dialog.dismiss()
            }.setOnDismissListener {
                // Notify the fragment about the dismissal
                listener.onDialogDismissed()
            }.create().apply {
                show()
                // Set title text color
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
                // Set button color
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
    }

    private fun setViewVisibilityAndText(view: TextView, text: String, entry: ConstraintLayout) {
        if (text.isNotEmpty()) {
            view.text = text
        } else {
            entry.visibility = View.GONE
        }
    }

    private fun approveRentalReq(currentReq: RentalReqModel) {
        // Reference to the specific req in Firebase
        val databaseRef = Firebase.database.reference.child("Rentals")
            .child(currentReq.rentalId) // Reference to the specific req using reqId

        val newStatus = "approved"
        // Create a map of the fields you want to update
        val updates = hashMapOf<String, Any>(
            "status" to newStatus
        )

        // Update the item with the new values
        databaseRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Request Approved", Toast.LENGTH_SHORT).show()
                reqList.clear()
                viewModel.retrieveRentalReq() // Refresh to reflect changes
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to approve request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun delRentalReq(currentReq: RentalReqModel) {
        // Reference to the specific req in Firebase
        val databaseRef = Firebase.database.reference.child("Rentals")
            .child(currentReq.rentalId) // Reference to the specific req using reqId
        databaseRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Request rejected from ${currentReq.clientName}", Toast.LENGTH_SHORT).show()
                // Remove the item from the list
                reqList.remove(currentReq)
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to reject request", Toast.LENGTH_SHORT).show()
            }
    }
}

