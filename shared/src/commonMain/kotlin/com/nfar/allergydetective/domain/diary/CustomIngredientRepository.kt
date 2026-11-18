package com.nfar.allergydetective.domain.diary

data class CategoryInfo(val id: Long, val key: String)

data class PropertyInfo(val id: Long, val key: String)

/** Custom ingredients (TRD 1.2): name + category + "similar to …", triggers taken from the base. */
interface CustomIngredientRepository {
    suspend fun categories(): List<CategoryInfo>

    suspend fun properties(): List<PropertyInfo>

    suspend fun categoryOf(ingredientId: Long): Long?

    suspend fun propertiesOf(ingredientId: Long): Set<Long>

    suspend fun create(name: String, categoryId: Long, baseIngredientId: Long?, propertyIds: Set<Long>): IngredientInfo
}

/**
 * Custom ingredients get ids from [CUSTOM_INGREDIENT_ID_START] on, so they never collide with
 * seed ingredients shipped later (id = category × 100 + n, see OFFENE_PUNKTE).
 */
const val CUSTOM_INGREDIENT_ID_START = 100_000L

fun nextCustomIngredientId(maxExistingId: Long?): Long =
    maxOf(CUSTOM_INGREDIENT_ID_START, (maxExistingId ?: 0L) + 1)
