package com.nfar.allergydetective.domain.diary

import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.time.MealSlot

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
        /** Stored kind (feedback 20.11.); null only for meals from before version 8. */
        val slot: MealSlot? = null,
    ) : HistoryEntry

    data class SymptomEntry(
        override val id: Long,
        override val time: Long,
        val symptomKey: String,
        val intensity: Int,
        val note: String?,
        /** On a sick day: part of the illness or not (self-test 19.11.). */
        val illnessRelation: IllnessRelation? = null,
    ) : HistoryEntry
}

interface HistoryRepository {
    /** Meals and symptoms from [fromMillis] on, newest first. */
    suspend fun entriesSince(fromMillis: Long): List<HistoryEntry>
}
