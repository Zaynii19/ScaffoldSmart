package com.example.scaffoldsmart.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.scaffoldsmart.client.client_receiver.DueDateAlarmReceiver
import java.util.concurrent.TimeUnit

object DueDateAlarm {
    private const val ALARM_REQUEST_CODE_PREFIX = 1000

    fun scheduleDueDateAlarms(context: Context, dueDateMillis: Long?) {
        // Cancel any existing alarms for this due date
        cancelAlarmsForDueDate(context, dueDateMillis)

        val currentTime = System.currentTimeMillis()

        // Schedule only future alarms
        val fiveDaysBefore = dueDateMillis?.minus(TimeUnit.DAYS.toMillis(5))
        val threeDaysBefore = dueDateMillis?.minus(TimeUnit.DAYS.toMillis(3))

        if (fiveDaysBefore != null) {
            if (fiveDaysBefore > currentTime) {
                scheduleSingleAlarm(context, fiveDaysBefore, 1) // Early reminder (5 days before)
            }
        }

        if (threeDaysBefore != null) {
            if (threeDaysBefore > currentTime) {
                scheduleSingleAlarm(context, threeDaysBefore, 2) // Close reminder (3 days before)
            }
        }

        // Always schedule the due date alarm (if not in the past)
        if (dueDateMillis != null) {
            if (dueDateMillis > currentTime) {
                scheduleSingleAlarm(context, dueDateMillis, 3) // Due date reminder
            }
        }

        if (fiveDaysBefore!= null && threeDaysBefore != null && fiveDaysBefore <= currentTime && threeDaysBefore <= currentTime) {
            Toast.makeText(context, "Due date is too close, only scheduling on-time reminder", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleSingleAlarm(context: Context, triggerAtMillis: Long?, notificationType: Int) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DueDateAlarmReceiver::class.java).apply {
            putExtra("DUE_DATE", triggerAtMillis)
            putExtra("NOTIFICATION_TYPE", notificationType)
        }

        // Unique request code for each alarm
        val requestCode = triggerAtMillis?.let { ALARM_REQUEST_CODE_PREFIX + notificationType + it.toInt() }

        val pendingIntent = requestCode?.let {
            PendingIntent.getBroadcast(
                context,
                it,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            triggerAtMillis?.let {
                pendingIntent?.let { operation ->
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        it,
                        operation
                    )
                }
            }
        } else {
            triggerAtMillis?.let {
                pendingIntent?.let { operation ->
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        it,
                        operation
                    )
                }
            }
        }
    }

    fun cancelAlarmsForDueDate(context: Context, dueDateMillis: Long?) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        // Cancel all 3 possible alarms for this due date
        for (notificationType in 1..3) {
            val requestCode = dueDateMillis?.let { ALARM_REQUEST_CODE_PREFIX + notificationType + it.toInt() }
            val intent = Intent(context, DueDateAlarmReceiver::class.java)
            val pendingIntent = requestCode?.let {
                PendingIntent.getBroadcast(
                    context,
                    it,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
                )
            }

            pendingIntent?.let {
                alarmManager.cancel(it)
            }
        }
    }
}