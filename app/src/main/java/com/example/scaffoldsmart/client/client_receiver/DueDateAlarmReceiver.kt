package com.example.scaffoldsmart.client

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.scaffoldsmart.R

class DueDateAlarmReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        val dueDate = intent?.getLongExtra("DUE_DATE", 0L) ?: return
        val notificationType = intent.getIntExtra("NOTIFICATION_TYPE", 0)

        val title = when (notificationType) {
            1 -> "Due Date Approaching (5 Days Left)"
            2 -> "Due Date Approaching (3 Days Left)"
            else -> "Due Date Today!"
        }

        val message = when (notificationType) {
            1 -> "Your rental is due in 5 days. Please prepare for return."
            2 -> "Your rental is due in 3 days. Don't forget!"
            else -> "Today is the due date for your rental. Please return the items."
        }

        showNotification(context, title, message)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "due_date_channel"
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Create notification channel (required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Due Date Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open app when notification is clicked
        val appIntent = Intent(context, ClientMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}