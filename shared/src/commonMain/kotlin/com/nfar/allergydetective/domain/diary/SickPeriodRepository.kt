package com.nfar.allergydetective.domain.diary

/** Illness from [startDay] to [endDay] (local epoch days, inclusive); [endDay] null = still ill. */
data class SickPeriod(val id: Long, val startDay: Long, val endDay: Long?) {
    fun contains(day: Long, today: Long): Boolean = day in startDay..(endDay ?: today)
}

/** Periods of acute illness, left out of the analysis (self-test 18.11.). */
interface SickPeriodRepository {
    /** Newest first. */
    suspend fun periods(): List<SickPeriod>

    suspend fun add(startDay: Long, endDay: Long?): Long

    suspend fun setEnd(id: Long, endDay: Long?)

    /** Changes start and end, e.g. from the history (self-test 19.11.). */
    suspend fun update(id: Long, startDay: Long, endDay: Long?) {
        delete(id)
        add(startDay, endDay)
    }

    suspend fun delete(id: Long)
}
