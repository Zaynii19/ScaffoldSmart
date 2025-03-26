package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.SendReminderRcvItemBinding
import com.example.scaffoldsmart.util.DateFormater
import com.example.scaffoldsmart.util.OnesignalService

class ReminderRcvAdapter(
    val context: Context,
    private var rentalList: ArrayList<RentalModel>,
    private val oneSignal: OnesignalService,
    private val listener: OnItemActionListener
): RecyclerView.Adapter<ReminderRcvAdapter.MyReminderViewHolder>() {

    class MyReminderViewHolder(val binding: SendReminderRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    interface OnItemActionListener {
        fun onDialogDismissed()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReminderViewHolder {
        return MyReminderViewHolder(SendReminderRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return rentalList.size
    }

    override fun onBindViewHolder(holder: MyReminderViewHolder, position: Int) {

        val currentRent = rentalList[position]
        holder.binding.clientName.text = currentRent.clientName
        holder.binding.dueDate.text = buildString {
            append("Due Date: ")
            append(currentRent.endDuration)
        }

        holder.binding.root.setOnClickListener {
            val dueDate = DateFormater.formatDateString(currentRent.endDuration)
            val title = "Scaffold Reminder"
            val msg = "Your scaffold rental is about to expire. Due date is $dueDate"
            val externalId = listOf(currentRent.clientEmail)
            oneSignal.sendNotiByOneSignalToExternalId(title, msg, externalId)
            Toast.makeText(context, "Rental Reminder Send to ${currentRent.clientName}", Toast.LENGTH_SHORT).show()
            // Notify the fragment about the dismissal
            listener.onDialogDismissed()
        }
    }
}