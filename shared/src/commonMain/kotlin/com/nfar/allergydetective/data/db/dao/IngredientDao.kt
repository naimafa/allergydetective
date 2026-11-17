package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.nfar.allergydetective.data.db.entity.CategoryEntity
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
}
