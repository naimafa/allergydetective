package com.nfar.allergydetective.domain.diary

/** Confirmation state of a day (TRD 1.3). Only NO_SYMPTOMS and COMPLETE days count for the analysis. */
enum class DayCheckStatus { OPEN, NO_SYMPTOMS, COMPLETE }

/** Evening check of one day (UX 3.4). Ratings are 1–5, null if not answered. */
data class DayCheckData(
    val epochDay: Long,
    val wellbeing: Int? = null,
    /** Sleep of the night before this day. */
    val sleepHours: Float? = null,
    val sleepQuality: Int? = null,
    val stress: Int? = null,
    val status: DayCheckStatus = DayCheckStatus.OPEN,
    val factorIds: Set<Long> = emptySet(),
) {
    val isConfirmed: Boolean get() = status != DayCheckStatus.OPEN
}

data class FactorInfo(val id: Long, val key: String)

interface DayCheckRepository {
    /** Stored check of the day, or null if the day was never checked. */
    suspend fun load(epochDay: Long): DayCheckData?

    suspend fun lifestyleFactors(): List<FactorInfo>

    /** Number of symptom entries that started in [fromMillis, toMillis). */
    suspend fun symptomCount(fromMillis: Long, toMillis: Long): Int

    /** Saves the check and replaces the day factors, in one transaction. */
    suspend fun save(check: DayCheckData)

    /** Days of [days] that are confirmed. */
    suspend fun confirmedDays(days: List<Long>): Set<Long>

    /** Number of confirmed days, shown as progress on the home screen (UX 2). */
    suspend fun confirmedDayCount(): Int
}
