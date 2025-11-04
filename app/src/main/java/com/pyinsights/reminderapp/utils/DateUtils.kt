package com.pyinsights.reminderapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getFormattedDate(calendar: Calendar): String {
        val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)

        return "$dayOfWeek, $dayOfMonth${getDayOfMonthSuffix(dayOfMonth)} $month"
    }

    fun getFormattedDateTime(timeInMillis: Long): String {
        val date = Date(timeInMillis)
        val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        val dayOfMonth = SimpleDateFormat("d", Locale.getDefault()).format(date).toInt()
        val month = SimpleDateFormat("MMM", Locale.getDefault()).format(date)
        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)

        return "$dayOfWeek, $dayOfMonth${getDayOfMonthSuffix(dayOfMonth)} $month at $time"
    }


    private fun getDayOfMonthSuffix(n: Int): String {
        if (n in 11..13) {
            return "th"
        }
        return when (n % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}
