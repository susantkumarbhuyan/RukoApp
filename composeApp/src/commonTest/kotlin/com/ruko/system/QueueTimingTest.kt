package com.ruko.system

import com.ruko.system.domain.model.UserQueue
import com.ruko.system.utils.QueueTiming
import kotlin.test.Test
import kotlin.test.assertEquals

class QueueTimingTest {
    private val entries = listOf(
        UserQueue(id = "a", userId = "a", joinedAt = 100, startedAt = 1000),
        UserQueue(id = "b", userId = "b", joinedAt = 200),
        UserQueue(id = "c", userId = "c", joinedAt = 300)
    )

    @Test fun findsActualPositionAndAbsence() {
        assertEquals(2, QueueTiming.position(entries, "b"))
        assertEquals(0, QueueTiming.position(entries, "missing"))
    }

    @Test fun frontCustomerHasNoWaitingTime() {
        assertEquals(0L, QueueTiming.waitMillis(entries, "a", 600_000, 301_000))
    }

    @Test fun deductsElapsedServiceAndExcludesOwnService() {
        assertEquals(900_000L, QueueTiming.waitMillis(entries, "c", 600_000, 301_000))
    }

    @Test fun overrunDoesNotCreateNegativeWaitingTime() {
        assertEquals(600_000L, QueueTiming.waitMillis(entries, "c", 600_000, 901_000))
    }

    @Test fun roundsMinutesUpAndClampsNegativeDurations() {
        assertEquals(0L, QueueTiming.minutesCeiling(-1))
        assertEquals(1L, QueueTiming.minutesCeiling(1))
        assertEquals(1L, QueueTiming.minutesCeiling(60_000))
        assertEquals(2L, QueueTiming.minutesCeiling(60_001))
    }
}
