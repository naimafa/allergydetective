package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import com.nfar.allergydetective.data.db.entity.DayCheckEntity
import com.nfar.allergydetective.data.db.entity.MealEntity
import com.nfar.allergydetective.data.db.entity.MealIngredientEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntryEntity

@Dao
interface HistoryDao {
    @Query("SELECT * FROM meals WHERE eatenAt >= :fromMillis ORDER BY eatenAt DESC, id DESC")
    suspend fun mealsSince(fromMillis: Long): List<MealEntity>

    @Query(
        """
        SELECT mi.* FROM meal_ingredients mi
        JOIN meals m ON m.id = mi.mealId
        WHERE m.eatenAt >= :fromMillis
        ORDER BY mi.mealId, mi.ingredientId
        """,
    )
    suspend fun mealIngredientsSince(fromMillis: Long): List<MealIngredientEntity>

    @Query("SELECT * FROM symptom_entries WHERE startedAt >= :fromMillis ORDER BY startedAt DESC, id DESC")
    suspend fun symptomEntriesSince(fromMillis: Long): List<SymptomEntryEntity>

    @Query("SELECT * FROM day_checks WHERE epochDay >= :fromDay")
    suspend fun dayChecksSince(fromDay: Long): List<DayCheckEntity>
}
