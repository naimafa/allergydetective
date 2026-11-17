package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.MealDao
import com.nfar.allergydetective.data.db.entity.MealEntity
import com.nfar.allergydetective.data.db.entity.MealIngredientEntity
import com.nfar.allergydetective.data.db.model.PortionSize
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.MealRepository

class RoomMealRepository(private val dao: MealDao) : MealRepository {

    override suspend fun ingredients(): List<IngredientInfo> =
        dao.ingredients().map { IngredientInfo(it.id, it.key, it.customName) }

    override suspend fun synonyms(language: String): Map<Long, List<String>> =
        dao.synonyms(language).groupBy({ it.ingredientId }, { it.synonym })

    override suspend fun recentIngredientIds(limit: Int): List<Long> = dao.recentIngredientIds(limit)

    override suspend fun ingredientIdsWithKnownAllergy(): Set<Long> = dao.ingredientIdsWithKnownAllergy().toSet()

    override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>): Long =
        dao.insertMealWithItems(
            meal = MealEntity(eatenAt = eatenAt, tzOffsetMin = tzOffsetMin, favoriteId = null),
            items = items.map { MealIngredientEntity(mealId = 0, ingredientId = it.ingredientId, portion = it.portion.toEntity()) },
        )

    override suspend fun deleteMeal(mealId: Long) = dao.deleteMeal(mealId)
}

internal fun Portion.toEntity(): PortionSize = when (this) {
    Portion.SMALL -> PortionSize.SMALL
    Portion.MEDIUM -> PortionSize.MEDIUM
    Portion.LARGE -> PortionSize.LARGE
}
