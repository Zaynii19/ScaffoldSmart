package com.example.scaffoldsmart.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object DateFormater {
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

    fun formatRentDuration(calendar: Calendar): String {
        // Create a SimpleDateFormat instance with the desired format
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        // Format the date and return the string
        return dateFormat.format(calendar.time)
    }

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

    fun formatCurrentDate(): String {
        // Get the current date
        val currentDate = Calendar.getInstance()

        // Create a SimpleDateFormat instance for the desired output format
        val outputFormat = SimpleDateFormat("d'th' MMMM, yyyy", Locale.getDefault())

        // Get the day of the month
        val day = SimpleDateFormat("d", Locale.getDefault()).format(currentDate.time)

        // Determine the suffix for the day
        val suffix = when {
            day.endsWith("1") && day != "11" -> "st"
            day.endsWith("2") && day != "12" -> "nd"
            day.endsWith("3") && day != "13" -> "rd"
            else -> "th"
        }

        // Format the current date into the desired output format and replace "th"
        val formattedDate = outputFormat.format(currentDate.time).replace("th", suffix)

        return formattedDate
    }

    fun formatDateString(dateString: String): String {
        // Create a SimpleDateFormat instance to parse the input date string
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        // Create a SimpleDateFormat instance for the desired output format
        val outputFormat = SimpleDateFormat("d MMMM, yyyy", Locale.getDefault())

        // Parse the date string
        val date = inputFormat.parse(dateString)

        // Format the date into the desired output format
        val formattedDate = outputFormat.format(date!!)

        return formattedDate
    }

    fun calculateDurationInMonths(startDateString: String, endDateString: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val startDate = inputFormat.parse(startDateString)
        val endDate = inputFormat.parse(endDateString)

        // Create Calendar instances for both dates
        val startCalendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()

        startCalendar.time = startDate!!
        endCalendar.time = endDate!!

        // Calculate the difference in months
        val yearsDifference = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
        val monthsDifference = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)

        val totalMonthDifference = yearsDifference * 12 + monthsDifference

        return totalMonthDifference.toString() + " month" + (if (totalMonthDifference != 1) "s" else "")
    }

    fun compareDateWithCurrentDate(endDuration: String): Boolean {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        // Convert end duration strings to Date type
        val endDate = inputFormat.parse(endDuration)

        // Get current date and format it
        val currentDate = LocalDate.now() // Get current date
        val currentDateFormatted = currentDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val currentDateParsed = inputFormat.parse(currentDateFormatted)

        return currentDateParsed!!.after(endDate)
    }
}