package com.nfar.allergydetective.domain.diary

import com.nfar.allergydetective.domain.analysis.model.MealItem

/** An ingredient as the food log shows it. Predefined ones have a [key], custom ones a [customName]. */
data class IngredientInfo(
    val id: Long,
    val key: String?,
    val customName: String?,
)

/** A saved meal template, e.g. "Müsli" (UX 3.1). */
data class FavoriteInfo(
    val id: Long,
    val name: String,
    val items: List<MealItem>,
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

    /** Ingredients with a heat-labile trigger: logging them asks "raw or cooked?" (self-test 18.11.). */
    suspend fun ingredientIdsNeedingPreparation(): Set<Long> = emptySet()

    /**
     * Saves the meal with its items in one transaction and returns its id.
     * [favoriteId] only records the origin, the items are always a snapshot (TRD 3.2).
     */
    suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long? = null): Long

    /** Favorites with their default portions, sorted by name. */
    suspend fun favorites(): List<FavoriteInfo>

    suspend fun saveFavorite(name: String, items: List<MealItem>): Long

    suspend fun deleteFavorite(favoriteId: Long)

    /**
     * Changes name and ingredients of a dish. The database keeps the id, so logged meals keep
     * their origin; this default (for simple implementations) replaces the dish instead.
     */
    suspend fun updateFavorite(favoriteId: Long, name: String, items: List<MealItem>) {
        deleteFavorite(favoriteId)
        saveFavorite(name, items)
    }

    /** Undo of [saveMeal], removes the meal and its items. */
    suspend fun deleteMeal(mealId: Long)
}

/** Current app language for the synonym search, e.g. "de". */
fun interface LanguageProvider {
    fun language(): String
}
