package com.example.scaffoldsmart.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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

    fun formatRentDuration(calendar: Calendar?): String {
        // Create a SimpleDateFormat instance with the desired format
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        // Format the date and return the string
        return dateFormat.format(calendar!!.time)
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
        if (timestamp == null) return ""

        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val now = Calendar.getInstance()

        return when {
            isSameDay(calendar, now) -> "Today"
            isYesterday(calendar, now) -> "Yesterday"
            isWithinLast7Days(calendar, now) -> SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
            else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(calendar.time)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = cal2.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(cal1, yesterday)
    }

    private fun isWithinLast7Days(cal1: Calendar, cal2: Calendar): Boolean {
        val sevenDaysAgo = Calendar.getInstance().apply {
            timeInMillis = cal2.timeInMillis
            add(Calendar.DAY_OF_YEAR, -7)
        }
        return cal1.after(sevenDaysAgo) && !isSameDay(cal1, cal2) && !isYesterday(cal1, cal2)
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

    fun formatDateString(dateString: String?): String {
        // Create a SimpleDateFormat instance to parse the input date string
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        // Create a SimpleDateFormat instance for the desired output format
        val outputFormat = SimpleDateFormat("d MMMM, yyyy", Locale.getDefault())

        // Parse the date string
        val date = inputFormat.parse(dateString!!)

        // Format the date into the desired output format
        val formattedDate = outputFormat.format(date!!)

        return formattedDate
    }

    fun calculateDurationInMonths(startDateString: String?, endDateString: String?): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val startDate = inputFormat.parse(startDateString!!)
        val endDate = inputFormat.parse(endDateString!!)

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

    fun calculateDurationInDays(startDateString: String?, endDateString: String?): Int {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val startDate: Date? = inputFormat.parse(startDateString!!)
        val endDate: Date? = inputFormat.parse(endDateString!!)

        if (startDate == null || endDate == null) {
            // Consider throwing an exception or returning a specific error value
            return -1 // Example error value
        }

        // Calculate the difference in milliseconds
        val timeDifference = endDate.time - startDate.time

        // Convert milliseconds to days
        return TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS).toInt()
    }

    fun compareDateWithCurrentDate(endDuration: String?): Boolean {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        // Convert end duration strings to Date type
        val endDate = inputFormat.parse(endDuration!!)

        // Get current date and format it
        val currentDate = LocalDate.now() // Get current date
        val currentDateFormatted = currentDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val currentDateParsed = inputFormat.parse(currentDateFormatted)

        return currentDateParsed!!.after(endDate)
    }

    fun convertDateStringToDateMillis(dateString: String?): Long? {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return try {
            val date: Date? = inputFormat.parse(dateString!!)
            date!!.time
        } catch (e: Exception) {
            // Handle parsing exception (e.g., invalid date format)
            e.printStackTrace()
            null
        }
    }

    fun combineAlarmDateTime(dueDate: String?): Long {
        val dueDateTime = "10:00 AM" //Trigger alarm on 10 AM of due date
        try {
            val calendar = Calendar.getInstance().apply {
                clear()

                // Parse date (format: dd-MM-yyyy)
                val dateParts = dueDate!!.split("-")
                require(dateParts.size == 3) { "Invalid date format" }
                set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1) // Month is 0-based
                set(Calendar.YEAR, dateParts[2].toInt())

                // Parse hardcoded time (format: h:mm a)
                val timeParts = dueDateTime.split(":")
                require(timeParts.size == 2) { "Invalid time format" }

                val hour = timeParts[0].trim().toInt()
                val minuteAndPeriod = timeParts[1].split(" ")
                require(minuteAndPeriod.isNotEmpty()) { "Invalid time format" }

                val minute = minuteAndPeriod[0].trim().toInt()

                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        } catch (e: Exception) {
            return 0L
        }
    }
}