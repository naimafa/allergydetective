package com.nfar.allergydetective.domain.diary

import com.nfar.allergydetective.domain.analysis.model.Portion

/** One line of the history (UX 6: tab "Historie"). */
sealed interface HistoryEntry {
    val id: Long
    val time: Long

    data class MealEntry(
        override val id: Long,
        override val time: Long,
        val items: List<Pair<IngredientInfo, Portion>>,
    ) : HistoryEntry

    data class SymptomEntry(
        override val id: Long,
        override val time: Long,
        val symptomKey: String,
        val intensity: Int,
        val note: String?,
    ) : HistoryEntry
}

interface HistoryRepository {
    /** Meals and symptoms from [fromMillis] on, newest first. */
    suspend fun entriesSince(fromMillis: Long): List<HistoryEntry>

    /** Status of the day checks from [fromDay] on. */
    suspend fun dayStatusSince(fromDay: Long): Map<Long, DayCheckStatus>
}
