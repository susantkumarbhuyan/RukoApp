package com.ruko.system.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object TimeUtils {

    /**
     * Convert minutes to milliseconds
     * @param minutes Number of minutes
     * @return Equivalent milliseconds
     */
    fun minutesToMillis(minutes: Long): Long {
        return minutes * 60 * 1000
    }

    /**
     * Convert seconds to milliseconds
     */
    fun secondsToMillis(seconds: Long): Long {
        return seconds * 1000
    }

    /**
     * Convert milliseconds to minutes
     */
    fun millisToMinutes(millis: Long): Long {
        return millis / (60 * 1000)
    }

    fun formatMillisToTimeString(millis: Long): String {
        val totalMinutes = millis / (60 * 1000)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "$minutes min"
        }
    }

    @OptIn(ExperimentalTime::class)
    fun currentTimeInMilli() = Clock.System.now().toEpochMilliseconds()
}