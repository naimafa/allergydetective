package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.nfar.allergydetective.data.db.entity.CategoryEntity
import com.nfar.allergydetective.data.db.entity.IngredientEntity
import com.nfar.allergydetective.data.db.entity.IngredientPropertyEntity
import com.nfar.allergydetective.data.db.entity.PropertyEntity
import com.nfar.allergydetective.domain.diary.nextCustomIngredientId

@Dao
interface IngredientDao {
    @Query("SELECT * FROM categories ORDER BY id")
    suspend fun categories(): List<CategoryEntity>

    @Query("SELECT * FROM properties ORDER BY id")
    suspend fun properties(): List<PropertyEntity>

    @Query("SELECT categoryId FROM ingredients WHERE id = :ingredientId")
    suspend fun categoryOf(ingredientId: Long): Long?

    @Query("SELECT propertyId FROM ingredient_properties WHERE ingredientId = :ingredientId ORDER BY propertyId")
    suspend fun propertiesOf(ingredientId: Long): List<Long>

    @Query("SELECT MAX(id) FROM ingredients")
    suspend fun maxIngredientId(): Long?

    @Insert
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Insert
    suspend fun insertIngredientProperties(items: List<IngredientPropertyEntity>)

    /** Picks the next custom id and inserts the ingredient with its properties in one transaction. */
    @Transaction
    suspend fun insertCustomIngredient(
        name: String,
        categoryId: Long,
        baseIngredientId: Long?,
        propertyIds: List<Long>,
    ): Long {
        val id = nextCustomIngredientId(maxIngredientId())
        insertIngredient(
            IngredientEntity(
                id = id,
                key = null,
                customName = name,
                categoryId = categoryId,
                isCustom = true,
                baseIngredientId = baseIngredientId,
            ),
        )
        insertIngredientProperties(propertyIds.map { IngredientPropertyEntity(id, it) })
        return id
    }

    @Query("SELECT * FROM ingredients ORDER BY id")
    suspend fun allIngredients(): List<IngredientEntity>

    @Query(
        """
        DELETE FROM meal_ingredients WHERE ingredientId = :from
        AND mealId IN (SELECT mealId FROM meal_ingredients WHERE ingredientId = :to)
        """,
    )
    suspend fun dropMealItemsAlreadyContaining(from: Long, to: Long)

    @Query("UPDATE meal_ingredients SET ingredientId = :to WHERE ingredientId = :from")
    suspend fun moveMealItems(from: Long, to: Long)

    /** Logged occurrences may hold the same ingredient twice, so they are simply moved (user 24.11.2026). */
    @Query("UPDATE meal_item_occurrences SET ingredientId = :to WHERE ingredientId = :from")
    suspend fun moveOccurrences(from: Long, to: Long)

    @Query(
        """
        DELETE FROM favorite_ingredients WHERE ingredientId = :from
        AND favoriteId IN (SELECT favoriteId FROM favorite_ingredients WHERE ingredientId = :to)
        """,
    )
    suspend fun dropFavoriteItemsAlreadyContaining(from: Long, to: Long)

    @Query("UPDATE favorite_ingredients SET ingredientId = :to WHERE ingredientId = :from")
    suspend fun moveFavoriteItems(from: Long, to: Long)

    @Query("UPDATE ingredients SET baseIngredientId = :to WHERE baseIngredientId = :from")
    suspend fun moveBaseIngredient(from: Long, to: Long)

    @Query("DELETE FROM ingredients WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomIngredient(id: Long)

    /**
     * Moves every use of the custom ingredient [from] to [to] and deletes [from].
     * A meal or dish that already contains [to] keeps only that entry (primary key).
     */
    @Transaction
    suspend fun mergeCustomInto(from: Long, to: Long) {
        dropMealItemsAlreadyContaining(from, to)
        moveMealItems(from, to)
        moveOccurrences(from, to)
        dropFavoriteItemsAlreadyContaining(from, to)
        moveFavoriteItems(from, to)
        moveBaseIngredient(from, to)
        deleteCustomIngredient(from)
    }
}
