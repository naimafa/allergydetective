package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.MealDao
import com.nfar.allergydetective.data.analysis.HEAT_LABILE_PROPERTY_KEYS
import com.nfar.allergydetective.data.db.entity.FavoriteEntity
import com.nfar.allergydetective.data.db.entity.FavoriteIngredientEntity
import com.nfar.allergydetective.data.db.entity.MealEntity
import com.nfar.allergydetective.domain.diary.FavoriteInfo
import com.nfar.allergydetective.data.db.entity.MealIngredientEntity
import com.nfar.allergydetective.data.db.model.MealKind
import com.nfar.allergydetective.data.db.model.PortionSize
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.data.db.model.PreparationKind
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.LoggedMeal
import com.nfar.allergydetective.domain.diary.MealRepository
import com.nfar.allergydetective.domain.diary.MealSnapshot
import com.nfar.allergydetective.domain.diary.SnapshotDish
import com.nfar.allergydetective.data.db.entity.MealDishEntity
import com.nfar.allergydetective.data.db.entity.MealItemOccurrenceEntity

class RoomMealRepository(private val dao: MealDao) : MealRepository {

    override suspend fun ingredients(): List<IngredientInfo> =
        dao.ingredients().map { IngredientInfo(it.id, it.key, it.customName, it.categoryId) }

    override suspend fun synonyms(language: String): Map<Long, List<String>> =
        dao.synonyms(language).groupBy({ it.ingredientId }, { it.synonym })

    override suspend fun recentIngredientIds(limit: Int): List<Long> = dao.recentIngredientIds(limit)

    override suspend fun ingredientIdsWithKnownAllergy(): Set<Long> = dao.ingredientIdsWithKnownAllergy().toSet()

    override suspend fun ingredientIdsNeedingPreparation(): Set<Long> =
        dao.ingredientIdsWithProperties(HEAT_LABILE_PROPERTY_KEYS.toList()).toSet()

    override suspend fun saveMeal(eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?, slot: MealSlot?, snapshot: MealSnapshot?): Long {
        val meal = MealEntity(eatenAt = eatenAt, tzOffsetMin = tzOffsetMin, favoriteId = favoriteId, kind = slot?.toEntity())
        val rows = items.map { MealIngredientEntity(mealId = 0, ingredientId = it.ingredientId, portion = it.portion.toEntity(), preparation = it.preparation?.toEntity()) }
        // Without a snapshot (older callers) every item counts as picked on its own.
        val shot = snapshot ?: MealSnapshot(emptyList(), items)
        return dao.insertMealWithSnapshot(meal, rows, shot.dishRows(), shot.singleRows())
    }

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

    override suspend fun updateFavorite(favoriteId: Long, name: String, items: List<MealItem>) =
        dao.updateFavoriteWithItems(
            favoriteId = favoriteId,
            name = name,
            items = items.map { FavoriteIngredientEntity(favoriteId = favoriteId, ingredientId = it.ingredientId, defaultPortion = it.portion.toEntity(), preparation = it.preparation?.toEntity()) },
        )

    override suspend fun deleteFavorite(favoriteId: Long) = dao.deleteFavorite(favoriteId)

    override suspend fun mealIdsWithDish(favoriteId: Long): List<Long> = dao.mealIdsWithDish(favoriteId)

    override suspend fun applyDishChange(favoriteId: Long, added: List<MealItem>, removedIngredientIds: Set<Long>, minorIngredientIds: Set<Long>) =
        dao.applyDishChange(favoriteId, added.map { it.ingredientId to it.preparation?.toEntity() }, removedIngredientIds, minorIngredientIds)

    override suspend fun reorderFavorites(favoriteIds: List<Long>) = dao.reorderFavorites(favoriteIds)

    override suspend fun deleteMeal(mealId: Long) = dao.deleteMeal(mealId)

    override suspend fun meal(mealId: Long): LoggedMeal? {
        val meal = dao.meal(mealId) ?: return null
        val items = dao.mealIngredients(mealId).map { MealItem(it.ingredientId, it.portion.toDomain(), it.preparation?.toDomain()) }
        val occurrences = dao.occurrences(mealId)
        // Meals from before version 13 have no snapshot.
        val snapshot = if (occurrences.isEmpty()) null else MealSnapshot(
            dishes = dao.mealDishes(mealId).map { dish ->
                SnapshotDish(dish.favoriteId, dish.name, dish.portion.toDomain(), occurrences.filter { it.mealDishId == dish.id }.map { it.toItem() })
            },
            singles = occurrences.filter { it.mealDishId == null }.map { it.toItem() },
        )
        return LoggedMeal(meal.id, meal.eatenAt, items, meal.favoriteId, meal.tzOffsetMin, meal.kind?.toDomain(), snapshot)
    }

    override suspend fun updateMeal(mealId: Long, eatenAt: Long, tzOffsetMin: Int, items: List<MealItem>, favoriteId: Long?, slot: MealSlot?, snapshot: MealSnapshot?) {
        val shot = snapshot ?: MealSnapshot(emptyList(), items)
        dao.updateMealWithSnapshot(
            mealId = mealId,
            eatenAt = eatenAt,
            tzOffsetMin = tzOffsetMin,
            favoriteId = favoriteId,
            kind = slot?.toEntity(),
            items = items.map { MealIngredientEntity(mealId = mealId, ingredientId = it.ingredientId, portion = it.portion.toEntity(), preparation = it.preparation?.toEntity()) },
            dishes = shot.dishRows(),
            singles = shot.singleRows(),
        )
    }

    private fun MealSnapshot.dishRows(): List<Pair<MealDishEntity, List<MealItemOccurrenceEntity>>> =
        dishes.mapIndexed { index, dish ->
            MealDishEntity(mealId = 0, favoriteId = dish.favoriteId, name = dish.name, portion = dish.portion.toEntity(), position = index) to dish.items.map { it.toOccurrence() }
        }

    private fun MealSnapshot.singleRows(): List<MealItemOccurrenceEntity> = singles.map { it.toOccurrence() }

    private fun MealItem.toOccurrence() =
        MealItemOccurrenceEntity(mealId = 0, mealDishId = null, ingredientId = ingredientId, portion = portion.toEntity(), preparation = preparation?.toEntity())

    private fun MealItemOccurrenceEntity.toItem() = MealItem(ingredientId, portion.toDomain(), preparation?.toDomain())

    override suspend fun mealIdOnDay(from: Long, until: Long, slot: MealSlot): Long? = dao.mealIdOnDay(from, until, slot.toEntity())
}

internal fun MealKind.toDomain(): MealSlot = when (this) {
    MealKind.BREAKFAST -> MealSlot.BREAKFAST
    MealKind.LUNCH -> MealSlot.LUNCH
    MealKind.DINNER -> MealSlot.DINNER
    MealKind.SNACK -> MealSlot.SNACK
    MealKind.DRINK -> MealSlot.DRINK
}

internal fun MealSlot.toEntity(): MealKind = when (this) {
    MealSlot.BREAKFAST -> MealKind.BREAKFAST
    MealSlot.LUNCH -> MealKind.LUNCH
    MealSlot.DINNER -> MealKind.DINNER
    MealSlot.SNACK -> MealKind.SNACK
    MealSlot.DRINK -> MealKind.DRINK
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
    PreparationKind.GLUTEN_FREE -> Preparation.GLUTEN_FREE
}

internal fun Preparation.toEntity(): PreparationKind = when (this) {
    Preparation.RAW -> PreparationKind.RAW
    Preparation.COOKED -> PreparationKind.COOKED
    Preparation.GLUTEN_FREE -> PreparationKind.GLUTEN_FREE
}
