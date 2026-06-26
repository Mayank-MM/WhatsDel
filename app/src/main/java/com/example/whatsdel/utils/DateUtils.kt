package com.example.whatsdel.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun formatTimestamp(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Returns a human-readable relative time string (e.g., "2 hours ago", "Yesterday").
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes min ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hr ago"
            }
            diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days days ago"
            }
            else -> formatDate(timestamp)
        }
    }
}
