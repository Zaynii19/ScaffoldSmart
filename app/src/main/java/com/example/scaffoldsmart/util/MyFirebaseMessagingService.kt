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
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Create the notification channelF
        createNotificationChannel(applicationContext)

        // Extract notification data and handle both notification and data payloads
        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title ?: "New Rental Request"
            val message = remoteMessage.notification?.body ?: "A new rental request has been submitted."
            showNotification(title, message)
        } else if (remoteMessage.data.isNotEmpty()) {
            // Handle data payload here (rental request details)
            val data = remoteMessage.data
            val clientName = data["clientName"]
            val rentalAddress = data["rentalAddress"]
            val clientEmail = data["clientEmail"]
            val clientPhone = data["clientPhone"]
            val clientCnic = data["clientCnic"]
            val startDuration = data["startDuration"]
            val endDuration = data["endDuration"]
            val pipes = data["pipes"]
            val pipesLength = data["pipesLength"]
            val joints = data["joints"]
            val wench = data["wench"]
            val pumps = data["pumps"]
            val motors = data["motors"]
            val generators = data["generators"]
            val wheel = data["wheel"]

            showNotificationWithData(clientName, rentalAddress, clientEmail, clientPhone, clientCnic,
                startDuration, endDuration, pipes, pipesLength, joints, wench, pumps, motors, generators, wheel)
        }
    }

    private fun showNotification(title: String, message: String) {
        // Intent to launch EnterPinActivity when the notification is clicked
        val intent = Intent(applicationContext, AdminMainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "admin_channel"
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentIntent(pendingIntent) // Perform action when clicked
            .setSmallIcon(R.drawable.alert)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) // Automatically remove notification when clicked
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        try {
            notificationManager.notify(1, notification)
            Log.d("RentalDebug", "onMessageReceived: $title $message")
        } catch (e: SecurityException) {
            Log.e("RentalDebug", "onMessageReceived: ${e.printStackTrace()}")
        }
    }

    private fun showNotificationWithData(
        clientName: String?,
        rentalAddress: String?,
        clientEmail: String?,
        clientPhone: String?,
        clientCnic: String?,
        startDuration: String?,
        endDuration: String?,
        pipes: String?,
        pipesLength: String?,
        joints: String?,
        wench: String?,
        pumps: String?,
        motors: String?,
        generators: String?,
        wheel: String?) {
        // Intent to launch EnterPinActivity when the notification is clicked
        val intent = Intent(applicationContext, AdminMainActivity::class.java).apply {
            putExtra("clientName", clientName)
            putExtra("rentalAddress", rentalAddress)
            putExtra("clientEmail", clientEmail)
            putExtra("clientPhone", clientPhone)
            putExtra("clientCnic", clientCnic)
            putExtra("startDuration", startDuration)
            putExtra("endDuration", endDuration)
            putExtra("pipes", pipes)
            putExtra("pipesLength", pipesLength)
            putExtra("joints", joints)
            putExtra("wench", wench)
            putExtra("pumps", pumps)
            putExtra("motors", motors)
            putExtra("generators", generators)
            putExtra("wheel", wheel)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "admin_channel"
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentIntent(pendingIntent) // Perform action when clicked
            .setSmallIcon(R.drawable.alert)
            .setContentTitle("New Rental Request")
            .setContentText("A new rental request has been submitted.")
            .setAutoCancel(true) // Automatically remove notification when clicked
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        try {
            notificationManager.notify(1, notification)
            Log.d("RentalDebug", "onMessageReceived: $clientName $rentalAddress")
        } catch (e: SecurityException) {
            Log.e("RentalDebug", "onMessageReceived: ${e.printStackTrace()}")
        }
    }

    private fun createNotificationChannel(context: Context) {
        val channelId = "admin_channel"
        val channelName = "Admin Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH // Set importance level
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Channel for admin notifications"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }

        // Register the channel with the system
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}