package com.ruko.system.utils

import com.ruko.system.domain.model.UserQueue

/** All queue durations and timestamps use milliseconds. */
object QueueTiming {
    fun position(entries: List<UserQueue>, userId: String): Int =
        entries.indexOfFirst { it.userId == userId } + 1

    fun waitMillis(entries: List<UserQueue>, userId: String, averageMillis: Long, now: Long): Long {
        val index = entries.indexOfFirst { it.userId == userId }
        if (index <= 0) return 0L
        val average = averageMillis.coerceAtLeast(1L)
        return entries.take(index).sumOf { entry ->
            entry.startedAt?.let { (average - (now - it).coerceAtLeast(0L)).coerceAtLeast(0L) }
                ?: average
        }
    }

    fun minutesCeiling(millis: Long): Long =
        millis.coerceAtLeast(0L) / 60_000 + if (millis.coerceAtLeast(0L) % 60_000 > 0) 1 else 0
}
