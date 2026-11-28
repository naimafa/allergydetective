package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.nfar.allergydetective.data.db.entity.FavoriteEntity
import com.nfar.allergydetective.data.db.entity.FavoriteIngredientEntity
import com.nfar.allergydetective.data.db.entity.IngredientEntity
import com.nfar.allergydetective.data.db.entity.IngredientSynonymEntity
import com.nfar.allergydetective.data.db.entity.MealEntity
import com.nfar.allergydetective.data.db.entity.MealItemOccurrenceEntity
import com.nfar.allergydetective.data.db.entity.MealDishEntity
import com.nfar.allergydetective.data.db.entity.MealIngredientEntity
import com.nfar.allergydetective.data.db.model.MealKind
import com.nfar.allergydetective.data.db.model.PortionSize
import com.nfar.allergydetective.data.db.model.PreparationKind

@Dao
interface MealDao {
    @Query("SELECT * FROM ingredients ORDER BY id")
    suspend fun ingredients(): List<IngredientEntity>

    @Query("SELECT * FROM ingredient_synonyms WHERE locale = :locale ORDER BY ingredientId, id")
    suspend fun synonyms(locale: String): List<IngredientSynonymEntity>

    @Query(
        """
        SELECT mi.ingredientId FROM meal_ingredients mi
        JOIN meals m ON m.id = mi.mealId
        GROUP BY mi.ingredientId
        ORDER BY MAX(m.eatenAt) DESC
        LIMIT :limit
        """,
    )
    suspend fun recentIngredientIds(limit: Int): List<Long>

    @Query(
        """
        SELECT DISTINCT ip.ingredientId FROM ingredient_properties ip
        JOIN user_known_allergies k ON k.propertyId = ip.propertyId
        """,
    )
    suspend fun ingredientIdsWithKnownAllergy(): List<Long>

    @Query(
        """
        SELECT DISTINCT ip.ingredientId FROM ingredient_properties ip
        JOIN properties p ON p.id = ip.propertyId
        WHERE p.`key` IN (:keys)
        """,
    )
    suspend fun ingredientIdsWithProperties(keys: List<String>): List<Long>

    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert
    suspend fun insertMealIngredients(items: List<MealIngredientEntity>)

    /** Meal and items in one transaction. The mealId of [items] is replaced by the new id. */
    @Transaction
    suspend fun insertMealWithItems(meal: MealEntity, items: List<MealIngredientEntity>): Long {
        val mealId = insertMeal(meal)
        insertMealIngredients(items.map { it.copy(mealId = mealId) })
        return mealId
    }

    @Query("SELECT * FROM favorites ORDER BY sortOrder, name COLLATE NOCASE")
    suspend fun favorites(): List<FavoriteEntity>

    @Query("SELECT * FROM favorite_ingredients ORDER BY favoriteId, ingredientId")
    suspend fun favoriteIngredients(): List<FavoriteIngredientEntity>

    /** Next position: new dishes go to the end of the user's order. */
    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM favorites")
    suspend fun nextFavoriteSortOrder(): Int

    @Query("UPDATE favorites SET sortOrder = :sortOrder WHERE id = :favoriteId")
    suspend fun setFavoriteSortOrder(favoriteId: Long, sortOrder: Int)

    /** The dishes in the given order, 0, 1, 2 … (dragging in the favourites list). */
    @Transaction
    suspend fun reorderFavorites(favoriteIds: List<Long>) {
        favoriteIds.forEachIndexed { index, id -> setFavoriteSortOrder(id, index) }
    }

    @Insert
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Insert
    suspend fun insertFavoriteIngredients(items: List<FavoriteIngredientEntity>)

    @Transaction
    suspend fun insertFavoriteWithItems(favorite: FavoriteEntity, items: List<FavoriteIngredientEntity>): Long {
        val favoriteId = insertFavorite(favorite.copy(sortOrder = nextFavoriteSortOrder()))
        insertFavoriteIngredients(items.map { it.copy(favoriteId = favoriteId) })
        return favoriteId
    }

    @Query("UPDATE favorites SET name = :name WHERE id = :favoriteId")
    suspend fun renameFavorite(favoriteId: Long, name: String)

    /** Editing a dish: new name and items, same id, so logged meals keep their origin. */
    @Transaction
    suspend fun updateFavoriteWithItems(favoriteId: Long, name: String, items: List<FavoriteIngredientEntity>) {
        renameFavorite(favoriteId, name)
        deleteFavoriteIngredients(favoriteId)
        insertFavoriteIngredients(items.map { it.copy(favoriteId = favoriteId) })
    }

    @Query("DELETE FROM favorite_ingredients WHERE favoriteId = :favoriteId")
    suspend fun deleteFavoriteIngredients(favoriteId: Long)

    @Query("UPDATE meals SET favoriteId = NULL WHERE favoriteId = :favoriteId")
    suspend fun detachMealsFromFavorite(favoriteId: Long)

    @Query("DELETE FROM favorites WHERE id = :favoriteId")
    suspend fun deleteFavoriteRow(favoriteId: Long)

    /** Meals keep their items (snapshot), only the reference to the favorite is removed. */
    @Transaction
    suspend fun deleteFavorite(favoriteId: Long) {
        deleteFavoriteIngredients(favoriteId)
        detachMealsFromFavorite(favoriteId)
        detachMealDishesFromFavorite(favoriteId)
        deleteFavoriteRow(favoriteId)
    }

    @Query("DELETE FROM meal_ingredients WHERE mealId = :mealId")
    suspend fun deleteMealIngredients(mealId: Long)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealRow(mealId: Long)

    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun meal(mealId: Long): MealEntity?

    @Query("SELECT * FROM meal_ingredients WHERE mealId = :mealId")
    suspend fun mealIngredients(mealId: Long): List<MealIngredientEntity>

    /** The first meal of [kind] eaten from [from] (inclusive) until [until] (exclusive), i.e. on that day (user 24.11.2026: a changed time must not lead to a second breakfast). */
    @Query("SELECT id FROM meals WHERE eatenAt >= :from AND eatenAt < :until AND kind = :kind ORDER BY id LIMIT 1")
    suspend fun mealIdOnDay(from: Long, until: Long, kind: MealKind): Long?

    @Query("UPDATE meals SET eatenAt = :eatenAt, tzOffsetMin = :tzOffsetMin, favoriteId = :favoriteId, kind = :kind WHERE id = :mealId")
    suspend fun updateMealRow(mealId: Long, eatenAt: Long, tzOffsetMin: Int, favoriteId: Long?, kind: MealKind?)

    /** Editing a logged meal: new time, kind and items, same id. */
    @Transaction
    suspend fun updateMealWithItems(mealId: Long, eatenAt: Long, tzOffsetMin: Int, favoriteId: Long?, kind: MealKind?, items: List<MealIngredientEntity>) {
        updateMealRow(mealId, eatenAt, tzOffsetMin, favoriteId, kind)
        deleteMealIngredients(mealId)
        insertMealIngredients(items.map { it.copy(mealId = mealId) })
    }

    // --- Snapshot of what was logged (user 24.11.2026) ---

    @Insert
    suspend fun insertMealDish(dish: MealDishEntity): Long

    @Insert
    suspend fun insertOccurrences(items: List<MealItemOccurrenceEntity>)

    @Query("DELETE FROM meal_item_occurrences WHERE mealId = :mealId")
    suspend fun deleteOccurrences(mealId: Long)

    @Query("DELETE FROM meal_dishes WHERE mealId = :mealId")
    suspend fun deleteMealDishes(mealId: Long)

    @Query("SELECT * FROM meal_dishes WHERE mealId = :mealId ORDER BY position, id")
    suspend fun mealDishes(mealId: Long): List<MealDishEntity>

    @Query("SELECT * FROM meal_item_occurrences WHERE mealId = :mealId ORDER BY id")
    suspend fun occurrences(mealId: Long): List<MealItemOccurrenceEntity>

    @Query("UPDATE meal_dishes SET favoriteId = NULL WHERE favoriteId = :favoriteId")
    suspend fun detachMealDishesFromFavorite(favoriteId: Long)

    /** Replaces the snapshot of a meal: its dishes with their ingredients, and the single ingredients. */
    @Transaction
    suspend fun replaceSnapshot(mealId: Long, dishes: List<Pair<MealDishEntity, List<MealItemOccurrenceEntity>>>, singles: List<MealItemOccurrenceEntity>) {
        deleteOccurrences(mealId)
        deleteMealDishes(mealId)
        dishes.forEach { (dish, items) ->
            val dishId = insertMealDish(dish.copy(id = 0, mealId = mealId))
            insertOccurrences(items.map { it.copy(id = 0, mealId = mealId, mealDishId = dishId) })
        }
        insertOccurrences(singles.map { it.copy(id = 0, mealId = mealId, mealDishId = null) })
    }

    /** A new meal with its merged items and its snapshot, in one transaction. */
    @Transaction
    suspend fun insertMealWithSnapshot(
        meal: MealEntity,
        items: List<MealIngredientEntity>,
        dishes: List<Pair<MealDishEntity, List<MealItemOccurrenceEntity>>>,
        singles: List<MealItemOccurrenceEntity>,
    ): Long {
        val id = insertMealWithItems(meal, items)
        replaceSnapshot(id, dishes, singles)
        return id
    }

    /** Editing a logged meal with its snapshot, in one transaction. */
    @Transaction
    suspend fun updateMealWithSnapshot(
        mealId: Long, eatenAt: Long, tzOffsetMin: Int, favoriteId: Long?, kind: MealKind?,
        items: List<MealIngredientEntity>,
        dishes: List<Pair<MealDishEntity, List<MealItemOccurrenceEntity>>>,
        singles: List<MealItemOccurrenceEntity>,
    ) {
        updateMealWithItems(mealId, eatenAt, tzOffsetMin, favoriteId, kind, items)
        replaceSnapshot(mealId, dishes, singles)
    }

    // --- Changing a dish also in earlier meals (user 28.11.2026) ---

    /** Meals that were logged with this dish (snapshot rows), newest first. */
    @Query("SELECT DISTINCT d.mealId FROM meal_dishes d JOIN meals m ON m.id = d.mealId WHERE d.favoriteId = :favoriteId ORDER BY m.eatenAt DESC")
    suspend fun mealIdsWithDish(favoriteId: Long): List<Long>

    /**
     * Brings a change of a dish into the meals logged with it: [added] ingredients join the dish (with the dish's portion
     * of that meal, one step smaller for [minorIds] next to a main part), [removed] ones leave it. What the user took out
     * or changed in a single meal stays. The merged list for the analysis is rebuilt from all occurrences.
     */
    @Transaction
    suspend fun applyDishChange(favoriteId: Long, added: List<Pair<Long, PreparationKind?>>, removed: Set<Long>, minorIds: Set<Long>) {
        for (mealId in mealIdsWithDish(favoriteId)) {
            val dishIds = mealDishes(mealId).filter { it.favoriteId == favoriteId }.associateBy { it.id }
            val rows = occurrences(mealId).filterNot { it.mealDishId in dishIds && it.ingredientId in removed }.toMutableList()
            for (dish in dishIds.values) {
                val inDish = rows.filter { it.mealDishId == dish.id }.map { it.ingredientId }.toSet()
                val newOnes = added.filter { (id, _) -> id !in inDish }
                val hasMainPart = (inDish + newOnes.map { it.first }).any { it !in minorIds }
                newOnes.forEach { (id, preparation) ->
                    val portion = if (hasMainPart && id in minorIds) dish.portion.smaller() else dish.portion
                    rows += MealItemOccurrenceEntity(mealId = mealId, mealDishId = dish.id, ingredientId = id, portion = portion, preparation = preparation)
                }
            }
            deleteOccurrences(mealId)
            insertOccurrences(rows.map { it.copy(id = 0) })
            deleteMealIngredients(mealId)
            insertMealIngredients(
                rows.groupBy { it.ingredientId }.map { (id, same) ->
                    MealIngredientEntity(mealId = mealId, ingredientId = id, portion = same.maxOf { it.portion }, preparation = same.firstNotNullOfOrNull { it.preparation })
                },
            )
        }
    }

    /** Items first, so this does not depend on the foreign key cascade being enabled. */
    @Transaction
    suspend fun deleteMeal(mealId: Long) {
        deleteOccurrences(mealId)
        deleteMealDishes(mealId)
        deleteMealIngredients(mealId)
        deleteMealRow(mealId)
    }
}

/** One step smaller, small stays small (like Portion.smaller in the meal form). */
private fun PortionSize.smaller(): PortionSize = if (this == PortionSize.LARGE) PortionSize.MEDIUM else PortionSize.SMALL
