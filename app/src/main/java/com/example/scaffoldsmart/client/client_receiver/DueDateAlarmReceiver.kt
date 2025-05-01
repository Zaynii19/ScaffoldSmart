package com.example.scaffoldsmart.client.client_receiver

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.client.ClientMainActivity

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

        createNotificationChannel(context)
        showNotification(context, title, message)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "due_date_channel",
                "Due Date Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for due date notifications"
                enableVibration(true)
                enableLights(true)
                lightColor = Color.RED
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        // Intent to open app when notification is clicked
        val contentIntent = Intent(context, ClientMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "due_date_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.app_logo)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(getFullScreenIntent(context), true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(ALARM_NOTIFICATION_ID, notification)
            Log.d("DueDateNoti", "Notification created: $title")
        } catch (e: Exception) {
            Log.e("DueDateNoti", "Error showing notification", e)
        }
    }

    private fun getFullScreenIntent(context: Context): PendingIntent {
        val fullScreenIntent = Intent(context, ClientMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        return PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ALARM_NOTIFICATION_ID = 1001
    }
}