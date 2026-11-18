package com.nfar.allergydetective.data.analysis

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

/** Everything the analysis needs, read from the database in one transaction. */
data class AnalysisRows(
    val properties: List<PropertyEntity>,
    val propertyWindows: List<PropertyWindowEntity>,
    val propertySymptomGroups: List<PropertySymptomGroupEntity>,
    val ingredients: List<IngredientEntity>,
    val ingredientProperties: List<IngredientPropertyEntity>,
    val symptoms: List<SymptomEntity>,
    val knownAllergies: List<KnownAllergyEntity>,
    val meals: List<MealEntity>,
    val mealIngredients: List<MealIngredientEntity>,
    val symptomEntries: List<SymptomEntryEntity>,
    val dayChecks: List<DayCheckEntity>,
    val dayFactors: List<DayFactorEntity>,
    val sickPeriods: List<SickPeriodEntity> = emptyList(),
)
