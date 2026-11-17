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
import com.nfar.allergydetective.data.db.entity.MealIngredientEntity

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

    @Query("SELECT * FROM favorites ORDER BY name COLLATE NOCASE")
    suspend fun favorites(): List<FavoriteEntity>

    @Query("SELECT * FROM favorite_ingredients ORDER BY favoriteId, ingredientId")
    suspend fun favoriteIngredients(): List<FavoriteIngredientEntity>

    @Insert
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Insert
    suspend fun insertFavoriteIngredients(items: List<FavoriteIngredientEntity>)

    @Transaction
    suspend fun insertFavoriteWithItems(favorite: FavoriteEntity, items: List<FavoriteIngredientEntity>): Long {
        val favoriteId = insertFavorite(favorite)
        insertFavoriteIngredients(items.map { it.copy(favoriteId = favoriteId) })
        return favoriteId
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
        deleteFavoriteRow(favoriteId)
    }

    @Query("DELETE FROM meal_ingredients WHERE mealId = :mealId")
    suspend fun deleteMealIngredients(mealId: Long)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealRow(mealId: Long)

    /** Items first, so this does not depend on the foreign key cascade being enabled. */
    @Transaction
    suspend fun deleteMeal(mealId: Long) {
        deleteMealIngredients(mealId)
        deleteMealRow(mealId)
    }
}
