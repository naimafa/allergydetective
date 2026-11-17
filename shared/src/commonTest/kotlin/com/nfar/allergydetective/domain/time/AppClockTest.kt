package com.nfar.allergydetective.domain.time

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class AppClockTest {

    private fun clockAt(iso: String, zone: TimeZone = TimeZone.of("Europe/Berlin")) =
        AppClock(clock = object : Clock { override fun now() = Instant.parse(iso) }, zone = { zone })

    @Test
    fun offsetFollowsDaylightSavingTime() {
        val clock = clockAt("2026-11-17T12:00:00Z")

        assertEquals(60, clock.offsetMinAt(Instant.parse("2026-11-17T12:00:00Z").toEpochMilliseconds()))
        assertEquals(120, clock.offsetMinAt(Instant.parse("2026-07-01T12:00:00Z").toEpochMilliseconds()))
    }

    @Test
    fun todayIsTheLocalDayNotTheUtcDay() {
        // 23:30 UTC is already 00:30 the next day in Berlin
        val clock = clockAt("2026-11-17T23:30:00Z")

        assertEquals(clockAt("2026-11-18T12:00:00Z").today(), clock.today())
        assertEquals(Pair(0, 30), clock.localTime(clock.nowMillis()))
    }

    @Test
    fun startOfDayIsLocalMidnight() {
        val clock = clockAt("2026-11-17T12:00:00Z")

        assertEquals(Instant.parse("2026-11-16T23:00:00Z").toEpochMilliseconds(), clock.startOfDayMillis(clock.today()))
    }
}
