package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import com.nfar.allergydetective.data.analysis.AnalysisRows
import com.nfar.allergydetective.data.db.entity.DayCheckEntity
import com.nfar.allergydetective.data.db.entity.DayFactorEntity
import com.nfar.allergydetective.data.db.entity.SickPeriodEntity
import com.nfar.allergydetective.data.db.entity.IngredientEntity
import com.nfar.allergydetective.data.db.entity.IngredientPropertyEntity
import com.nfar.allergydetective.data.db.entity.KnownAllergyEntity
import com.nfar.allergydetective.data.db.entity.MealEntity
import com.nfar.allergydetective.data.db.entity.MealIngredientEntity
import com.nfar.allergydetective.data.db.entity.PropertyEntity
import com.nfar.allergydetective.data.db.entity.PropertySymptomGroupEntity
import com.nfar.allergydetective.data.db.entity.PropertyWindowEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntryEntity

/** Read access for the analysis (TRD 2). Fixed ordering keeps the result reproducible. */
@Dao
interface AnalysisDao {
    @Query("SELECT * FROM properties ORDER BY id")
    suspend fun properties(): List<PropertyEntity>

    @Query("SELECT * FROM property_windows ORDER BY propertyId, startMin")
    suspend fun propertyWindows(): List<PropertyWindowEntity>

    @Query("SELECT * FROM property_symptom_groups ORDER BY propertyId, symptomGroupKey")
    suspend fun propertySymptomGroups(): List<PropertySymptomGroupEntity>

    @Query("SELECT * FROM ingredients ORDER BY id")
    suspend fun ingredients(): List<IngredientEntity>

    @Query("SELECT * FROM ingredient_properties ORDER BY ingredientId, propertyId")
    suspend fun ingredientProperties(): List<IngredientPropertyEntity>

    @Query("SELECT * FROM symptoms ORDER BY id")
    suspend fun symptoms(): List<SymptomEntity>

    @Query("SELECT * FROM user_known_allergies ORDER BY propertyId")
    suspend fun knownAllergies(): List<KnownAllergyEntity>

    @Query("SELECT * FROM meals ORDER BY eatenAt, id")
    suspend fun meals(): List<MealEntity>

    @Query("SELECT * FROM meal_ingredients ORDER BY mealId, ingredientId")
    suspend fun mealIngredients(): List<MealIngredientEntity>

    @Query("SELECT * FROM symptom_entries ORDER BY startedAt, id")
    suspend fun symptomEntries(): List<SymptomEntryEntity>

    @Query("SELECT * FROM day_checks ORDER BY epochDay")
    suspend fun dayChecks(): List<DayCheckEntity>

    @Query("SELECT * FROM day_factors ORDER BY epochDay, factorId")
    suspend fun dayFactors(): List<DayFactorEntity>

    @Query("SELECT * FROM sick_periods ORDER BY startDay")
    suspend fun sickPeriods(): List<SickPeriodEntity>

    /** One transaction, so a meal saved during the read cannot appear without its ingredients. */
    @Transaction
    suspend fun loadAll(): AnalysisRows = AnalysisRows(
        properties = properties(),
        propertyWindows = propertyWindows(),
        propertySymptomGroups = propertySymptomGroups(),
        ingredients = ingredients(),
        ingredientProperties = ingredientProperties(),
        symptoms = symptoms(),
        knownAllergies = knownAllergies(),
        meals = meals(),
        mealIngredients = mealIngredients(),
        symptomEntries = symptomEntries(),
        dayChecks = dayChecks(),
        dayFactors = dayFactors(),
        sickPeriods = sickPeriods(),
    )
}
