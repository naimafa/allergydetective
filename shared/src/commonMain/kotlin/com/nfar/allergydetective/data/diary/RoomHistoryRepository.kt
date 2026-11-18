package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.HistoryDao
import com.nfar.allergydetective.data.db.dao.MealDao
import com.nfar.allergydetective.data.db.dao.SymptomDao
import com.nfar.allergydetective.data.db.model.DayStatus
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.HistoryRepository
import com.nfar.allergydetective.domain.diary.IngredientInfo

class RoomHistoryRepository(
    private val dao: HistoryDao,
    private val mealDao: MealDao,
    private val symptomDao: SymptomDao,
) : HistoryRepository {

    override suspend fun entriesSince(fromMillis: Long): List<HistoryEntry> {
        val ingredients = mealDao.ingredients().associate { it.id to IngredientInfo(it.id, it.key, it.customName) }
        val itemsByMeal = dao.mealIngredientsSince(fromMillis).groupBy { it.mealId }
        val dishNames = mealDao.favorites().associate { it.id to it.name }
        val meals = dao.mealsSince(fromMillis).map { meal ->
            HistoryEntry.MealEntry(
                id = meal.id,
                time = meal.eatenAt,
                items = itemsByMeal[meal.id].orEmpty().mapNotNull { item ->
                    ingredients[item.ingredientId]?.let { it to item.portion.toDomain() }
                },
                preparations = itemsByMeal[meal.id].orEmpty().mapNotNull { item ->
                    item.preparation?.let { item.ingredientId to it.toDomain() }
                }.toMap(),
                dishName = meal.favoriteId?.let(dishNames::get),
            )
        }
        val symptomKeys = symptomDao.symptoms().associate { it.id to it.key }
        val symptoms = dao.symptomEntriesSince(fromMillis).map { entry ->
            HistoryEntry.SymptomEntry(
                id = entry.id,
                time = entry.startedAt,
                symptomKey = symptomKeys[entry.symptomId].orEmpty(),
                intensity = entry.intensity,
                note = entry.note,
            )
        }
        return (meals + symptoms).sortedByDescending { it.time }
    }

    override suspend fun dayStatusSince(fromDay: Long): Map<Long, DayCheckStatus> =
        dao.dayChecksSince(fromDay).associate {
            it.epochDay to when (it.status) {
                DayStatus.OPEN -> DayCheckStatus.OPEN
                DayStatus.NO_SYMPTOMS -> DayCheckStatus.NO_SYMPTOMS
                DayStatus.COMPLETE -> DayCheckStatus.COMPLETE
            }
        }
}
