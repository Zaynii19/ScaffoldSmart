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

    fun scheduleOverDueAlarms(context: Context, dueDateMillis: Long?) {
        // Cancel any existing alarms for this due date
        cancelAlarmsForOverDue(context, dueDateMillis)

        val currentTime = System.currentTimeMillis()

        // Schedule only future alarms
        val oneDayAfter = dueDateMillis?.plus(TimeUnit.DAYS.toMillis(1))
        dueDateMillis?.let {
            if (it > currentTime) {
                scheduleSingleAlarm(context, oneDayAfter) // Over Due date reminder
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleSingleAlarm(context: Context, triggerAtMillis: Long?) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, OverDueFeeAlarmReceiver::class.java).apply {
            putExtra("DUE_DATE", triggerAtMillis)
        }

        // Unique request code for each alarm
        val requestCode = triggerAtMillis?.let { ALARM_REQUEST_CODE_PREFIX + it.toInt() }

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

    fun cancelAlarmsForOverDue(context: Context, dueDateMillis: Long?) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        // Cancel alarm for this due date
        val requestCode = dueDateMillis?.let { ALARM_REQUEST_CODE_PREFIX + it.toInt() }
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