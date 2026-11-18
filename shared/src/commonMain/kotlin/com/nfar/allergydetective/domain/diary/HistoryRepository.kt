package com.nfar.allergydetective.domain.diary

import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.Preparation

/** One line of the history (UX 6: tab "Historie"). */
sealed interface HistoryEntry {
    val id: Long
    val time: Long

    data class MealEntry(
        override val id: Long,
        override val time: Long,
        val items: List<Pair<IngredientInfo, Portion>>,
        /** Raw or cooked per ingredient id, only where it was asked. */
        val preparations: Map<Long, Preparation> = emptyMap(),
        /** Name of the dish (favorite) the meal was logged from, if any. */
        val dishName: String? = null,
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
