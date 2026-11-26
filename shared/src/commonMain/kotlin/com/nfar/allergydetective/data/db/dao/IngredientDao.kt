package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.nfar.allergydetective.data.db.entity.CategoryEntity
import com.nfar.allergydetective.data.db.entity.FavoriteIngredientEntity
import com.nfar.allergydetective.data.db.entity.MealDishEntity
import com.nfar.allergydetective.data.db.entity.MealItemOccurrenceEntity
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

    /** "Meine Zutaten" (user 25.11.2026). */
    @Query("SELECT * FROM ingredients WHERE isCustom = 1 ORDER BY customName COLLATE NOCASE")
    suspend fun customIngredients(): List<IngredientEntity>

    @Query(
        """
        SELECT COUNT(*) FROM (SELECT mealId FROM meal_ingredients WHERE ingredientId = :id
        UNION SELECT mealId FROM meal_item_occurrences WHERE ingredientId = :id)
        """,
    )
    suspend fun mealCountOf(id: Long): Int

    @Query("SELECT COUNT(DISTINCT favoriteId) FROM favorite_ingredients WHERE ingredientId = :id")
    suspend fun dishCountOf(id: Long): Int

    @Query("UPDATE ingredients SET customName = :name, categoryId = :categoryId WHERE id = :id AND isCustom = 1")
    suspend fun renameCustom(id: Long, name: String, categoryId: Long)

    @Query("DELETE FROM ingredient_properties WHERE ingredientId = :id")
    suspend fun deleteProperties(id: Long)

    /** Name, kind (and with it the picture) and contents of a custom ingredient; past entries follow. */
    @Transaction
    suspend fun updateCustomIngredient(id: Long, name: String, categoryId: Long, propertyIds: List<Long>) {
        renameCustom(id, name, categoryId)
        deleteProperties(id)
        insertIngredientProperties(propertyIds.map { IngredientPropertyEntity(id, it) })
    }

    /** Occurrences of an ingredient in logged meals, for replacing it by a dish. */
    @Query("SELECT * FROM meal_item_occurrences WHERE ingredientId = :id ORDER BY mealId, id")
    suspend fun occurrencesOf(id: Long): List<MealItemOccurrenceEntity>

    @Query("SELECT * FROM favorite_ingredients WHERE favoriteId = :favoriteId")
    suspend fun dishItems(favoriteId: Long): List<FavoriteIngredientEntity>

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM meal_dishes WHERE mealId = :mealId")
    suspend fun nextDishPosition(mealId: Long): Int

    @Insert
    suspend fun insertMealDish(dish: MealDishEntity): Long

    @Insert
    suspend fun insertOccurrences(items: List<MealItemOccurrenceEntity>)

    @Query("DELETE FROM meal_item_occurrences WHERE id = :occurrenceId")
    suspend fun deleteOccurrence(occurrenceId: Long)

    /** The merged list for the analysis: the dish's ingredients with the portion the replaced one had. */
    @Query(
        """
        INSERT OR IGNORE INTO meal_ingredients (mealId, ingredientId, portion, preparation)
        SELECT mi.mealId, fi.ingredientId, mi.portion, fi.preparation FROM meal_ingredients mi
        JOIN favorite_ingredients fi ON fi.favoriteId = :favoriteId AND fi.ingredientId != :from
        WHERE mi.ingredientId = :from
        """,
    )
    suspend fun addDishToMealIngredients(from: Long, favoriteId: Long)

    @Query("DELETE FROM meal_ingredients WHERE ingredientId = :id")
    suspend fun deleteMealIngredients(id: Long)

    /** Other dishes that contain the ingredient get the dish's ingredients instead. */
    @Query(
        """
        INSERT OR IGNORE INTO favorite_ingredients (favoriteId, ingredientId, defaultPortion, preparation)
        SELECT f.favoriteId, fi.ingredientId, f.defaultPortion, fi.preparation FROM favorite_ingredients f
        JOIN favorite_ingredients fi ON fi.favoriteId = :favoriteId AND fi.ingredientId != :from
        WHERE f.ingredientId = :from AND f.favoriteId != :favoriteId
        """,
    )
    suspend fun addDishToOtherDishes(from: Long, favoriteId: Long)

    @Query("DELETE FROM favorite_ingredients WHERE ingredientId = :id")
    suspend fun deleteFavoriteItems(id: Long)

    @Query("UPDATE ingredients SET baseIngredientId = NULL WHERE baseIngredientId = :id")
    suspend fun clearBaseIngredient(id: Long)

    /**
     * User 25.11.2026: a custom ingredient that is really a dish ("Feigen-Datteln-Marmelade") is replaced by that dish.
     * In every logged meal it becomes the dish with its ingredients (a dish of its own, or flattened into the dish it
     * was part of), keeping the portion it had; other dishes get the dish's ingredients. Then it is deleted.
     */
    @Transaction
    suspend fun replaceCustomWithDish(from: Long, favoriteId: Long, dishName: String) {
        val items = dishItems(favoriteId).filter { it.ingredientId != from }
        occurrencesOf(from).forEach { occurrence ->
            val dishId = occurrence.mealDishId ?: insertMealDish(
                MealDishEntity(mealId = occurrence.mealId, favoriteId = favoriteId, name = dishName, portion = occurrence.portion, position = nextDishPosition(occurrence.mealId)),
            )
            insertOccurrences(items.map { MealItemOccurrenceEntity(mealId = occurrence.mealId, mealDishId = dishId, ingredientId = it.ingredientId, portion = occurrence.portion, preparation = it.preparation) })
            deleteOccurrence(occurrence.id)
        }
        addDishToMealIngredients(from, favoriteId)
        deleteMealIngredients(from)
        addDishToOtherDishes(from, favoriteId)
        deleteFavoriteItems(from)
        clearBaseIngredient(from)
        deleteCustomIngredient(from)
    }

    /** Deletes the custom ingredient only when no meal or dish uses it; returns whether it was deleted. */
    @Transaction
    suspend fun deleteCustomIfUnused(id: Long): Boolean {
        if (mealCountOf(id) > 0 || dishCountOf(id) > 0) return false
        deleteCustomIngredient(id)
        return true
    }

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
