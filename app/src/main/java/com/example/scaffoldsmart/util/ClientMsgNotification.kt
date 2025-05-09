package com.example.scaffoldsmart.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.client.ClientChatActivity

class ClientMsgNotification {

    // Method to handle notification creation
    fun handleNotification(
        context: Context,
        message: String?,
        friendName: String?,
        receiverUid: String?
    ) {
        val title = "Message from Admin"
        createNotificationChannel(context)
        val notificationId = message.hashCode() // Unique ID based on item name
        message?.let { showNotificationWithData(context, title, it, friendName, receiverUid, notificationId) }
    }

    // Create notification channel
    private fun createNotificationChannel(context: Context) {
        val channelId = "message_channel"
        val channelName = "Message Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH // Set importance level
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Channel for message notifications"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }

        // Register the channel with the system
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    // Create and display notification
    private fun showNotificationWithData(
        context: Context,
        title: String,
        message: String,
        friendName: String?,
        receiverUid: String?,
        notificationId: Int
    ) {
        // Intent to launch EnterPinActivity when the notification is clicked
        val intent = Intent(context, ClientChatActivity::class.java).apply {
            putExtra("USERNAME", friendName)
            putExtra("UID", receiverUid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "message_channel"
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentIntent(pendingIntent) // Perform action when clicked
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) // Automatically remove notification when clicked
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationId, notification)
            Log.d("NotiDebug", "Notification created with Title: $title ")
        } catch (e: SecurityException) {
            Log.e("NotiDebug", "Error to create notification: ${e.printStackTrace()}")
        }
    }
}
