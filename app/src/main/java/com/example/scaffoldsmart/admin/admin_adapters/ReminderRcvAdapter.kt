package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.SendReminderRcvItemBinding
import com.example.scaffoldsmart.util.CheckNetConnectvity
import com.example.scaffoldsmart.util.DateFormater
import com.example.scaffoldsmart.util.OnesignalService

class ReminderRcvAdapter(
    val context: Context,
    private var rentalList: ArrayList<RentalModel>,
    private val oneSignal: OnesignalService
): RecyclerView.Adapter<ReminderRcvAdapter.MyReminderViewHolder>() {

    class MyReminderViewHolder(val binding: SendReminderRcvItemBinding): RecyclerView.ViewHolder(binding.root)

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

        holder.binding.sendBtn.setOnClickListener {
            if (CheckNetConnectvity.hasInternetConnection(context)) {
                val dueDate = DateFormater.formatDateString(currentRent.endDuration)
                val title = "Scaffold Rental Reminder"
                val msg = "Your scaffold rental is about to expire. Due date is $dueDate"
                val externalId = listOf(currentRent.clientEmail)
                oneSignal.sendNotiByOneSignalToExternalId(title, msg, externalId)
                Toast.makeText(context, "Rental Reminder Send to ${currentRent.clientName}", Toast.LENGTH_SHORT).show()
                // Disable the button if clientObj is null
                holder.binding.sendBtn.isEnabled = false
                holder.binding.sendBtn.backgroundTintList = ContextCompat.getColorStateList(context, R.color.dark_gray)
            } else {
                Toast.makeText(context, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show()
            }
        }
    }
}