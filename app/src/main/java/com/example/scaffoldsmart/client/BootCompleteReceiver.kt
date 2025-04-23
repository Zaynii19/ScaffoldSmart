package com.example.scaffoldsmart.client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.util.AlarmUtils.scheduleDueDateAlarms
import com.example.scaffoldsmart.util.DateFormater
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all alarms after device reboot
            rescheduleAllAlarms(context)
        }
    }

    private fun rescheduleAllAlarms(context: Context) {
        // Fetch all due dates from Firebase and re-schedule alarms
        FirebaseDatabase.getInstance().reference.child("Rentals")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (rentalSnapshot in snapshot.children) {
                        val rental = rentalSnapshot.getValue(RentalModel::class.java)
                        rental?.let {
                            if (it.clientID == getCurrentUserId(context) && it.rentStatus == "ongoing") {
                                val dueDateMiles = DateFormater.convertDateStringToDateMillis(it.endDuration)
                                if (dueDateMiles != null) {
                                    scheduleDueDateAlarms(context, dueDateMiles)
                                } else {
                                    Log.d("BootCompleteReceiver", "Invalid date format")
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BootCompleteReceiver", "Failed to fetch rentals", error.toException())
                }
            })
    }

    private fun getCurrentUserId(context: Context): String {
        // Return current user ID from SharedPreferences
        val prefs = context.getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        return prefs.getString("SenderUid", "") ?: ""
    }
}