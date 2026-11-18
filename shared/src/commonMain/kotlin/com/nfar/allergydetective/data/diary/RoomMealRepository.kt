package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.MealDao
import com.nfar.allergydetective.data.analysis.HEAT_LABILE_PROPERTY_KEYS
import com.nfar.allergydetective.data.db.entity.FavoriteEntity
import com.nfar.allergydetective.data.db.entity.FavoriteIngredientEntity
import com.nfar.allergydetective.data.db.entity.MealEntity
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.data.db.entity.MealIngredientEntity
import com.nfar.allergydetective.data.db.model.PortionSize
import com.nfar.allergydetective.data.db.model.PreparationKind
import com.nfar.allergydetective.domain.analysis.model.Preparation
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

    override suspend fun ingredientIdsNeedingPreparation(): Set<Long> =
        dao.ingredientIdsWithProperties(HEAT_LABILE_PROPERTY_KEYS.toList()).toSet()

    override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?): Long =
        dao.insertMealWithItems(
            meal = MealEntity(eatenAt = eatenAt, tzOffsetMin = tzOffsetMin, favoriteId = favoriteId),
            items = items.map { MealIngredientEntity(mealId = 0, ingredientId = it.ingredientId, portion = it.portion.toEntity(), preparation = it.preparation?.toEntity()) },
        )

    override suspend fun favorites(): List<FavoriteInfo> {
        val itemsByFavorite = dao.favoriteIngredients()
            .groupBy({ it.favoriteId }, { MealItem(it.ingredientId, it.defaultPortion.toDomain(), it.preparation?.toDomain()) })
        return dao.favorites().map { FavoriteInfo(it.id, it.name, itemsByFavorite[it.id].orEmpty()) }
    }

    override suspend fun saveFavorite(name: String, items: List<MealItem>): Long =
        dao.insertFavoriteWithItems(
            favorite = FavoriteEntity(name = name),
            items = items.map { FavoriteIngredientEntity(favoriteId = 0, ingredientId = it.ingredientId, defaultPortion = it.portion.toEntity(), preparation = it.preparation?.toEntity()) },
        )

    override suspend fun deleteFavorite(favoriteId: Long) = dao.deleteFavorite(favoriteId)

    override suspend fun deleteMeal(mealId: Long) = dao.deleteMeal(mealId)
}

internal fun PortionSize.toDomain(): Portion = when (this) {
    PortionSize.SMALL -> Portion.SMALL
    PortionSize.MEDIUM -> Portion.MEDIUM
    PortionSize.LARGE -> Portion.LARGE
}

internal fun Portion.toEntity(): PortionSize = when (this) {
    Portion.SMALL -> PortionSize.SMALL
    Portion.MEDIUM -> PortionSize.MEDIUM
    Portion.LARGE -> PortionSize.LARGE
}

internal fun PreparationKind.toDomain(): Preparation = when (this) {
    PreparationKind.RAW -> Preparation.RAW
    PreparationKind.COOKED -> Preparation.COOKED
}

internal fun Preparation.toEntity(): PreparationKind = when (this) {
    Preparation.RAW -> PreparationKind.RAW
    Preparation.COOKED -> PreparationKind.COOKED
}
