package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.nfar.allergydetective.data.db.entity.CategoryEntity
import com.nfar.allergydetective.data.db.entity.IngredientEntity
import com.nfar.allergydetective.data.db.entity.IngredientPropertyEntity
import com.nfar.allergydetective.data.db.entity.IngredientSynonymEntity
import com.nfar.allergydetective.data.db.entity.LifestyleFactorEntity
import com.nfar.allergydetective.data.db.entity.PropertyEntity
import com.nfar.allergydetective.data.db.entity.PropertySymptomGroupEntity
import com.nfar.allergydetective.data.db.entity.PropertyWindowEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntity
import com.nfar.allergydetective.data.seed.SeedEntities

/** Write access for the reference tables, used only by the seeder. */
@Dao
interface SeedDao {
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int

    @Insert suspend fun insertCategories(items: List<CategoryEntity>)
    @Insert suspend fun insertProperties(items: List<PropertyEntity>)
    @Insert suspend fun insertPropertyWindows(items: List<PropertyWindowEntity>)
    @Insert suspend fun insertPropertySymptomGroups(items: List<PropertySymptomGroupEntity>)
    @Insert suspend fun insertIngredients(items: List<IngredientEntity>)
    @Insert suspend fun insertIngredientProperties(items: List<IngredientPropertyEntity>)
    @Insert suspend fun insertIngredientSynonyms(items: List<IngredientSynonymEntity>)
    @Insert suspend fun insertSymptoms(items: List<SymptomEntity>)
    @Insert suspend fun insertLifestyleFactors(items: List<LifestyleFactorEntity>)

    /** Parents before children because of the foreign keys. All or nothing. */
    @Transaction
    suspend fun insertAll(seed: SeedEntities) {
        insertCategories(seed.categories)
        insertProperties(seed.properties)
        insertPropertyWindows(seed.propertyWindows)
        insertPropertySymptomGroups(seed.propertySymptomGroups)
        insertIngredients(seed.ingredients)
        insertIngredientProperties(seed.ingredientProperties)
        insertIngredientSynonyms(seed.ingredientSynonyms)
        insertSymptoms(seed.symptoms)
        insertLifestyleFactors(seed.lifestyleFactors)
    }
}
