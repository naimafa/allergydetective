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

    /** "Meine Zutaten" (user 25.11.2026): the user's own ingredients with how often they are used. */
    suspend fun customIngredients(): List<CustomIngredient> = emptyList()

    suspend fun update(id: Long, name: String, categoryId: Long, propertyIds: Set<Long>) = Unit

    /** Only an unused ingredient can be deleted; returns whether it was. */
    suspend fun deleteIfUnused(id: Long): Boolean = false

    /** Every meal and dish with [from] gets [to] instead; [from] is deleted. Nothing logged is lost. */
    suspend fun replace(from: Long, to: Long) = Unit

    /** User 25.11.2026: the ingredient was really a dish - every meal gets that dish, then it is deleted. */
    suspend fun replaceWithDish(from: Long, favoriteId: Long, dishName: String) = Unit
}

data class CustomIngredient(
    val id: Long,
    val name: String,
    val categoryId: Long,
    val propertyIds: Set<Long>,
    val mealCount: Int,
    val dishCount: Int,
) {
    val used: Boolean get() = mealCount > 0 || dishCount > 0
    val info: IngredientInfo get() = IngredientInfo(id = id, key = null, customName = name, categoryId = categoryId)
}

/**
 * Custom ingredients get ids from [CUSTOM_INGREDIENT_ID_START] on, so they never collide with
 * seed ingredients shipped later (id = category × 100 + n, see OFFENE_PUNKTE).
 */
const val CUSTOM_INGREDIENT_ID_START = 100_000L

fun nextCustomIngredientId(maxExistingId: Long?): Long =
    maxOf(CUSTOM_INGREDIENT_ID_START, (maxExistingId ?: 0L) + 1)
