package com.rajpawardotin.dekhreekh.utils

import java.util.Locale

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }
}

/**
 * Formats pace from total seconds per kilometer.
 * e.g., 330 seconds -> "5:30"
 */
fun formatPace(paceSecondsPerKm: Long): String {
    if (paceSecondsPerKm <= 0 || paceSecondsPerKm > 3600) return "--:--"
    val minutes = paceSecondsPerKm / 60
    val seconds = paceSecondsPerKm % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
