package com.example.scaffoldsmart.admin.admin_service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LowInventoryAlertService : Service() {

    private lateinit var database: FirebaseDatabase
    private var itemList = ArrayList<InventoryModel>()

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        retrieveInventoryData() // Fetch Inventory data
        Log.d("LowInventoryService", "Service created")
    }

    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Service will be restarted if terminated
    }

    private fun retrieveInventoryData() {
        database.reference.child("Inventory")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    itemList.clear() // Clear old data
                    for (child in snapshot.children) {
                        val inventoryItem = child.getValue(InventoryModel::class.java)
                        if (inventoryItem != null) {
                            itemList.add(inventoryItem)
                        }
                    }
                    // Check thresholds AFTER all items are loaded
                    checkInventoryThresholds(itemList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LowInventoryService", "Failed to retrieve inventory", error.toException())
                }
            })
    }

    private fun checkInventoryThresholds(items: List<InventoryModel>) {
        items.forEach { item ->
            if (item.quantity < item.threshold) {
                notifyLowInventory(item.itemName, item.quantity, item.threshold)
            }
        }
    }

    private fun notifyLowInventory(itemName: String, qty: Int, threshold: Int) {
        val title = "Low Inventory Alert"
        val message = "$itemName is running low!. Current Quantity: $qty, Threshold Value: $threshold)"
        val notificationId = itemName.hashCode() // Unique ID based on item name
        handleNotification(title, message, notificationId)
    }

    // Method to handle notification creation
    private fun handleNotification(title: String, message: String, notificationId: Int) {
        createNotificationChannel()
        showNotificationWithData(title, message, notificationId)
    }

    // Create notification channel
    private fun createNotificationChannel() {
        val channelId = "low_inventory_channel"
        val channelName = "low_inventory_channel"
        val importance = NotificationManager.IMPORTANCE_HIGH // Set importance level
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Channel for alert notifications"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }

        // Register the channel with the system
        val notificationManager = this@LowInventoryAlertService.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    // Create and display notification
    private fun showNotificationWithData(
        title: String,
        message: String,
        notificationId: Int
    ) {
        // Intent to launch EnterPinActivity when the notification is clicked
        val intent = Intent(this@LowInventoryAlertService, AdminMainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(this@LowInventoryAlertService, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "low_inventory_channel"
        val notification = NotificationCompat.Builder(this@LowInventoryAlertService, channelId)
            .setContentIntent(pendingIntent) // Perform action when clicked
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) // Automatically remove notification when clicked
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(this@LowInventoryAlertService)
        try {
            notificationManager.notify(notificationId, notification)
            Log.d("LowInventoryService", "Notification created")
        } catch (e: SecurityException) {
            Log.e("LowInventoryService", "Error to create notification: ${e.printStackTrace()}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
