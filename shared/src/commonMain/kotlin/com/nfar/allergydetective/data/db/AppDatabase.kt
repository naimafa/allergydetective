package com.nfar.allergydetective.data.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.nfar.allergydetective.data.db.dao.AnalysisDao
import com.nfar.allergydetective.data.db.dao.SeedDao
import com.nfar.allergydetective.data.db.entity.CategoryEntity
import com.nfar.allergydetective.data.db.entity.DayCheckEntity
import com.nfar.allergydetective.data.db.entity.DayFactorEntity
import com.nfar.allergydetective.data.db.entity.FavoriteEntity
import com.nfar.allergydetective.data.db.entity.FavoriteIngredientEntity
import com.nfar.allergydetective.data.db.entity.IngredientEntity
import com.nfar.allergydetective.data.db.entity.IngredientPropertyEntity
import com.nfar.allergydetective.data.db.entity.IngredientSynonymEntity
import com.nfar.allergydetective.data.db.entity.KnownAllergyEntity
import com.nfar.allergydetective.data.db.entity.LifestyleFactorEntity
import com.nfar.allergydetective.data.db.entity.MealEntity
import com.nfar.allergydetective.data.db.entity.MealIngredientEntity
import com.nfar.allergydetective.data.db.entity.PropertyEntity
import com.nfar.allergydetective.data.db.entity.PropertySymptomGroupEntity
import com.nfar.allergydetective.data.db.entity.PropertyWindowEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntryEntity
import com.nfar.allergydetective.data.db.entity.UserProfileEntity

const val DATABASE_NAME = "allergy_detective.db"

@Database(
    entities = [
        // Reference data
        CategoryEntity::class,
        PropertyEntity::class,
        PropertyWindowEntity::class,
        PropertySymptomGroupEntity::class,
        IngredientEntity::class,
        IngredientPropertyEntity::class,
        IngredientSynonymEntity::class,
        SymptomEntity::class,
        LifestyleFactorEntity::class,
        // User
        UserProfileEntity::class,
        KnownAllergyEntity::class,
        // Diary
        FavoriteEntity::class,
        FavoriteIngredientEntity::class,
        MealEntity::class,
        MealIngredientEntity::class,
        SymptomEntryEntity::class,
        DayCheckEntity::class,
        DayFactorEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun seedDao(): SeedDao
    abstract fun analysisDao(): AnalysisDao
}

// Room's KSP processor generates the actual implementations for each platform.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
