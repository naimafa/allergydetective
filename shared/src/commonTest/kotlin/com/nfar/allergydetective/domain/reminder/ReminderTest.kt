package com.nfar.allergydetective.domain.reminder

import kotlin.test.Test
import kotlin.test.assertEquals

class ReminderTest {

    @Test
    fun reminderLaterTodayWaitsUntilThen() {
        // 18:30:00 → 20:00
        assertEquals(90 * 60_000L, delayUntilNext(18 * 60 + 30, 0, 20, 0))
    }

    @Test
    fun passedReminderMovesToTomorrow() {
        // 21:00 → 20:00 next day
        assertEquals(23 * 60 * 60_000L, delayUntilNext(21 * 60, 0, 20, 0))
    }

    @Test
    fun exactlyNowMeansTomorrow() {
        assertEquals(24 * 60 * 60_000L, delayUntilNext(20 * 60, 0, 20, 0))
    }

    @Test
    fun secondsAreSubtracted() {
        assertEquals(60_000L - 15_000L, delayUntilNext(19 * 60 + 59, 15, 20, 0))
    }

    @Test
    fun applyFollowsTheSetting() {
        val calls = mutableListOf<String>()
        val scheduler = object : ReminderScheduler {
            override fun schedule(hour: Int, minute: Int) {
                calls += "schedule $hour:$minute"
            }
            override fun cancel() {
                calls += "cancel"
            }
        }

        scheduler.apply(ReminderSettings(enabled = true, hour = 21, minute = 30))
        scheduler.apply(ReminderSettings(enabled = false))

        assertEquals(listOf("schedule 21:30", "cancel"), calls)
    }
}
