package com.example.scaffoldsmart.client.client_service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DueReminderService : Service() {

    private lateinit var database: FirebaseDatabase
    private var rentalList = ArrayList<RentalModel>()
    private var clientUid: String? = null
    private lateinit var chatPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        chatPreferences = getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        clientUid = chatPreferences.getString("SenderUid", null)
        retrieveRentalData() // Fetch Inventory data
        Log.d("DueReminderService", "Service created")
    }

    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Service will be restarted if terminated
    }

    private fun retrieveRentalData() {
        database.reference.child("Rentals")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    rentalList.clear() // Clear old data
                    for (child in snapshot.children) {
                        val rental = child.getValue(RentalModel::class.java)
                        if (
                            rental != null &&
                            rental.status.isNotEmpty() &&
                            rental.clientID == clientUid &&
                            rental.rentStatus == "ongoing"
                            ) {
                            rental.let {
                                rentalList.add(it)
                            }
                        }
                    }
                    Log.d("DueReminderService", "Rental Data: ${rentalList.size} ")
                    // Check thresholds AFTER all items are loaded
                    checkRemainingDays(rentalList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DueReminderService", "Failed to retrieve rentals", error.toException())
                }
            })
    }

    private fun checkRemainingDays(rentals: List<RentalModel>) {
        rentals.forEach { rental ->

        }
    }

    private fun notifyLowInventory(dueDate: String) {
        val title = "Due Date Reminder"
        val message = "Your scaffold rental is about to expire. Due date is $dueDate"
        val notificationId = dueDate.hashCode() // Unique ID based on item name
        handleNotification(title, message, notificationId)
    }

    // Method to handle notification creation
    private fun handleNotification(title: String, message: String, notificationId: Int) {
        createNotificationChannel()
        showNotificationWithData(title, message, notificationId)
    }

    // Create notification channel
    private fun createNotificationChannel() {
        val channelId = "reminder_channel"
        val channelName = "Reminder Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH // Set importance level
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Channel for reminder notifications"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }

        // Register the channel with the system
        val notificationManager = this@DueReminderService.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    // Create and display notification
    private fun showNotificationWithData(
        title: String,
        message: String,
        notificationId: Int
    ) {
        // Intent to launch EnterPinActivity when the notification is clicked
        val intent = Intent(this@DueReminderService, AdminMainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(this@DueReminderService, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "reminder_channel"
        val notification = NotificationCompat.Builder(this@DueReminderService, channelId)
            .setContentIntent(pendingIntent) // Perform action when clicked
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) // Automatically remove notification when clicked
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(this@DueReminderService)
        try {
            notificationManager.notify(notificationId, notification)
            Log.d("DueReminderService", "Notification created")
        } catch (e: SecurityException) {
            Log.e("DueReminderService", "Error to create notification: ${e.printStackTrace()}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
