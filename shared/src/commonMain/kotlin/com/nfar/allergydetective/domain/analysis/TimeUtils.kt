package com.nfar.allergydetective.domain.analysis

internal const val MILLIS_PER_MINUTE = 60_000L
internal const val MILLIS_PER_DAY = 86_400_000L

/** Local epoch day of a UTC timestamp, using the offset that was active when it was logged. */
internal fun localEpochDay(utcMillis: Long, tzOffsetMin: Int): Long =
    (utcMillis + tzOffsetMin * MILLIS_PER_MINUTE).floorDiv(MILLIS_PER_DAY)
