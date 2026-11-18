package com.nfar.allergydetective.domain.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Current time and the device time zone, injected so tests can fix both.
 * Timestamps are UTC epoch millis, days are local epoch days (CODING_RICHTLINIEN 3).
 */
@OptIn(ExperimentalTime::class)
class AppClock(
    private val clock: Clock = Clock.System,
    private val zone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    fun nowMillis(): Long = clock.now().toEpochMilliseconds()

    /** Offset of the device time zone at [millis], stored next to every timestamp. */
    fun offsetMinAt(millis: Long): Int = zone().offsetAt(Instant.fromEpochMilliseconds(millis)).totalSeconds / 60

    fun epochDayOf(millis: Long): Long =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone()).date.toEpochDays().toLong()

    fun today(): Long = epochDayOf(nowMillis())

    fun startOfDayMillis(epochDay: Long): Long =
        LocalDate.fromEpochDays(epochDay).atStartOfDayIn(zone()).toEpochMilliseconds()

    fun localDate(epochDay: Long): LocalDate = LocalDate.fromEpochDays(epochDay)

    /** UTC millis of a local wall-clock time on [epochDay]. */
    fun millisAt(epochDay: Long, hour: Int, minute: Int): Long =
        LocalDateTime(LocalDate.fromEpochDays(epochDay), LocalTime(hour, minute)).toInstant(zone()).toEpochMilliseconds()

    /** Local hour and minute of [millis], for display. */
    fun localTime(millis: Long): Pair<Int, Int> {
        val time = Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone()).time
        return time.hour to time.minute
    }
}
