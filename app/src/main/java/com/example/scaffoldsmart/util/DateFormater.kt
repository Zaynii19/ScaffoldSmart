package com.example.scaffoldsmart.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateFormater {
    /*fun formatRelativeTimestamp(timestamp: Long?): String {
        if (timestamp == null) return "Unknown"

        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }*/

    fun formatTimestampForMsg(timestamp: Long?): String {
        if (timestamp == null) return "Unknown"

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    fun formatTimestampForLastSeen(timestamp: Long?): String {
        if (timestamp == null) return "Unknown"

        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val today = Calendar.getInstance()
        today.timeInMillis = currentTime

        val yesterday = Calendar.getInstance()
        yesterday.timeInMillis = currentTime
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("MMMM d", Locale.getDefault())

        return when {
            // Today
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
                "last seen today at ${timeFormatter.format(calendar.time)}"
            }
            // Yesterday
            calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> {
                "last seen yesterday at ${timeFormatter.format(calendar.time)}"
            }
            // Within the last 7 days
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
                "last seen $dayOfWeek at ${timeFormatter.format(calendar.time)}"
            }
            // Older than 7 days
            else -> {
                "last seen ${dateFormatter.format(calendar.time)}, ${timeFormatter.format(calendar.time)}"
            }
        }
    }

    fun formatDateHeader(timestamp: Long?): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp!! }
        val now = Calendar.getInstance()

        return when {
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) -> {
                "Today"  // Check if it's today
            }
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1 -> {
                "Yesterday" // Check if it's yesterday
            }
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
                SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time) // Same week day name
            }
            else -> {
                SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(calendar.time) // Format like "March 2, 2025"
            }
        }
    }
}