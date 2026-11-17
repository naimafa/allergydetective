package com.nfar.allergydetective.domain.diary

import com.nfar.allergydetective.domain.analysis.model.MealItem

/** An ingredient as the food log shows it. Predefined ones have a [key], custom ones a [customName]. */
data class IngredientInfo(
    val id: Long,
    val key: String?,
    val customName: String?,
)

/** Write and read access for logging meals (UX 3.1). */
interface MealRepository {
    suspend fun ingredients(): List<IngredientInfo>

    /** Search synonyms per ingredient for one language, e.g. "de". */
    suspend fun synonyms(language: String): Map<Long, List<String>>

    /** Ingredients of the latest meals, most recent first, without duplicates. */
    suspend fun recentIngredientIds(limit: Int): List<Long>

    /** Ingredients that contain a property stored as known allergy (TRD 2.6). */
    suspend fun ingredientIdsWithKnownAllergy(): Set<Long>

    /** Saves the meal with its items in one transaction and returns its id. */
    suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>): Long

    /** Undo of [saveMeal], removes the meal and its items. */
    suspend fun deleteMeal(mealId: Long)
}

/** Current app language for the synonym search, e.g. "de". */
fun interface LanguageProvider {
    fun language(): String
}
