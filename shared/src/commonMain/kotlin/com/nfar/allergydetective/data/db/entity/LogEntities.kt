package com.nfar.allergydetective.data.db.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.nfar.allergydetective.data.db.model.DayStatus
import com.nfar.allergydetective.data.db.model.PortionSize

/*
 * Diary data entered by the user (TRD 1.3).
 * Timestamps are UTC epoch millis; the offset is kept for display and day assignment.
 * Days are stored as epoch days (days since 1970-01-01).
 */

@Entity(
    tableName = "favorites",
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "favorite_ingredients",
    primaryKeys = ["favoriteId", "ingredientId"],
    foreignKeys = [
        ForeignKey(FavoriteEntity::class, ["id"], ["favoriteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(IngredientEntity::class, ["id"], ["ingredientId"]),
    ],
    indices = [Index("ingredientId")],
)
data class FavoriteIngredientEntity(
    val favoriteId: Long,
    val ingredientId: Long,
    val defaultPortion: PortionSize,
)

/** [favoriteId] only records the origin; ingredients are copied as a snapshot. */
@Entity(
    tableName = "meals",
    foreignKeys = [
        ForeignKey(FavoriteEntity::class, ["id"], ["favoriteId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("eatenAt"), Index("favoriteId")],
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eatenAt: Long,
    val tzOffsetMin: Int,
    val favoriteId: Long?,
)

@Entity(
    tableName = "meal_ingredients",
    primaryKeys = ["mealId", "ingredientId"],
    foreignKeys = [
        ForeignKey(MealEntity::class, ["id"], ["mealId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(IngredientEntity::class, ["id"], ["ingredientId"]),
    ],
    indices = [Index("ingredientId")],
)
data class MealIngredientEntity(
    val mealId: Long,
    val ingredientId: Long,
    val portion: PortionSize,
)

/** [intensity] 1–5. [note] is not used by the analysis, only in the PDF export. */
@Entity(
    tableName = "symptom_entries",
    foreignKeys = [
        ForeignKey(SymptomEntity::class, ["id"], ["symptomId"]),
    ],
    indices = [Index("startedAt"), Index("symptomId")],
)
data class SymptomEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symptomId: Long,
    val startedAt: Long,
    val tzOffsetMin: Int,
    val intensity: Int,
    val note: String?,
)

/** Evening check per day (TRD 3.3). Ratings are 1–5, null if not answered. */
@Entity(tableName = "day_checks")
data class DayCheckEntity(
    @PrimaryKey val epochDay: Long,
    val wellbeing: Int?,
    val sleepHours: Float?,
    val sleepQuality: Int?,
    val stress: Int?,
    val status: DayStatus = DayStatus.OPEN,
)

@Entity(
    tableName = "day_factors",
    primaryKeys = ["epochDay", "factorId"],
    foreignKeys = [
        ForeignKey(LifestyleFactorEntity::class, ["id"], ["factorId"]),
    ],
    indices = [Index("factorId")],
)
data class DayFactorEntity(
    val epochDay: Long,
    val factorId: Long,
)
