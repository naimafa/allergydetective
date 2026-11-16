package com.nfar.allergydetective.data.analysis

import com.nfar.allergydetective.data.db.model.DayStatus
import com.nfar.allergydetective.data.db.model.PortionSize
import com.nfar.allergydetective.domain.analysis.model.AnalysisInput
import com.nfar.allergydetective.domain.analysis.model.DayCheck
import com.nfar.allergydetective.domain.analysis.model.DayFactor
import com.nfar.allergydetective.domain.analysis.model.DiaryData
import com.nfar.allergydetective.domain.analysis.model.IngredientRef
import com.nfar.allergydetective.domain.analysis.model.Meal
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.ReferenceData
import com.nfar.allergydetective.domain.analysis.model.SymptomEntry
import com.nfar.allergydetective.domain.analysis.model.SymptomRef
import com.nfar.allergydetective.domain.analysis.model.TimeWindow
import com.nfar.allergydetective.domain.analysis.model.TriggerProperty

/** Maps database rows to the plain input of the analysis engine. Pure, no database access. */
fun AnalysisRows.toAnalysisInput(now: Long, nowTzOffsetMin: Int): AnalysisInput =
    AnalysisInput(
        reference = toReferenceData(),
        diary = toDiaryData(),
        now = now,
        nowTzOffsetMin = nowTzOffsetMin,
    )

private fun AnalysisRows.toReferenceData(): ReferenceData {
    val windowsByProperty = propertyWindows.groupBy({ it.propertyId }, { TimeWindow(it.startMin, it.endMin) })
    val groupsByProperty = propertySymptomGroups.groupBy({ it.propertyId }, { it.symptomGroupKey })
    val propertiesByIngredient = ingredientProperties.groupBy({ it.ingredientId }, { it.propertyId })
    return ReferenceData(
        properties = properties.map {
            TriggerProperty(it.id, windowsByProperty[it.id].orEmpty(), groupsByProperty[it.id].orEmpty().toSet())
        },
        ingredients = ingredients.map { IngredientRef(it.id, propertiesByIngredient[it.id].orEmpty().toSet()) },
        symptoms = symptoms.map { SymptomRef(it.id, it.groupKey) },
        knownAllergyPropertyIds = knownAllergies.map { it.propertyId }.toSet(),
    )
}

private fun AnalysisRows.toDiaryData(): DiaryData {
    val itemsByMeal = mealIngredients.groupBy({ it.mealId }, { MealItem(it.ingredientId, it.portion.toPortion()) })
    return DiaryData(
        meals = meals.map { Meal(it.id, it.eatenAt, it.tzOffsetMin, itemsByMeal[it.id].orEmpty()) },
        symptomEntries = symptomEntries.map { SymptomEntry(it.id, it.symptomId, it.startedAt, it.tzOffsetMin, it.intensity) },
        dayChecks = dayChecks.map {
            DayCheck(it.epochDay, it.status.isConfirmed(), it.sleepHours, it.sleepQuality, it.stress)
        },
        dayFactors = dayFactors.map { DayFactor(it.epochDay, it.factorId) },
    )
}

/** Only these days go into the analysis (TRD 2.2). */
private fun DayStatus.isConfirmed(): Boolean = this == DayStatus.NO_SYMPTOMS || this == DayStatus.COMPLETE

private fun PortionSize.toPortion(): Portion = when (this) {
    PortionSize.SMALL -> Portion.SMALL
    PortionSize.MEDIUM -> Portion.MEDIUM
    PortionSize.LARGE -> Portion.LARGE
}
