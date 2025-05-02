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
import com.example.scaffoldsmart.client.client_receiver.OverDueFeeAlarmReceiver
import java.util.concurrent.TimeUnit

object OverDueFeeAlarm {
    private const val ALARM_REQUEST_CODE_PREFIX = 2000

    fun scheduleOverDueAlarms(context: Context, dueDateMillis: Long) {
        // Cancel any existing alarms for this due date
        cancelAlarmsForOverDue(context, dueDateMillis)

        val currentTime = System.currentTimeMillis()

        // Schedule only future alarms
        val oneDayAfter = dueDateMillis + TimeUnit.DAYS.toMillis(1)
        if (dueDateMillis > currentTime) {
            scheduleSingleAlarm(context, oneDayAfter) // Over Due date reminder
        }
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleSingleAlarm(context: Context, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, OverDueFeeAlarmReceiver::class.java).apply {
            putExtra("DUE_DATE", triggerAtMillis)
        }

        // Unique request code for each alarm
        val requestCode = ALARM_REQUEST_CODE_PREFIX + triggerAtMillis.toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarmsForOverDue(context: Context, dueDateMillis: Long) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        // Cancel alarm for this due date
        val requestCode = ALARM_REQUEST_CODE_PREFIX + dueDateMillis.toInt()
        val intent = Intent(context, DueDateAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}