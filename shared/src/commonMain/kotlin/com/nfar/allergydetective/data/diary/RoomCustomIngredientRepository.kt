package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.IngredientDao
import com.nfar.allergydetective.domain.diary.CategoryInfo
import com.nfar.allergydetective.domain.diary.CustomIngredient
import com.nfar.allergydetective.domain.diary.CustomIngredientRepository
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.PropertyInfo

class RoomCustomIngredientRepository(private val dao: IngredientDao) : CustomIngredientRepository {

    override suspend fun categories(): List<CategoryInfo> = dao.categories().map { CategoryInfo(it.id, it.key) }

    override suspend fun properties(): List<PropertyInfo> = dao.properties().map { PropertyInfo(it.id, it.key) }

    override suspend fun categoryOf(ingredientId: Long): Long? = dao.categoryOf(ingredientId)

    override suspend fun propertiesOf(ingredientId: Long): Set<Long> = dao.propertiesOf(ingredientId).toSet()

    override suspend fun create(name: String, categoryId: Long, baseIngredientId: Long?, propertyIds: Set<Long>): IngredientInfo {
        val id = dao.insertCustomIngredient(name, categoryId, baseIngredientId, propertyIds.sorted())
        return IngredientInfo(id = id, key = null, customName = name)
    }

    override suspend fun customIngredients(): List<CustomIngredient> = dao.customIngredients().map {
        CustomIngredient(
            id = it.id,
            name = it.customName.orEmpty(),
            categoryId = it.categoryId,
            propertyIds = dao.propertiesOf(it.id).toSet(),
            mealCount = dao.mealCountOf(it.id),
            dishCount = dao.dishCountOf(it.id),
        )
    }

    override suspend fun update(id: Long, name: String, categoryId: Long, propertyIds: Set<Long>) =
        dao.updateCustomIngredient(id, name, categoryId, propertyIds.sorted())

    override suspend fun deleteIfUnused(id: Long): Boolean = dao.deleteCustomIfUnused(id)

    override suspend fun replace(from: Long, to: Long) = dao.mergeCustomInto(from, to)
}
