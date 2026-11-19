package com.nfar.allergydetective.data.analysis

import com.nfar.allergydetective.data.db.model.DayStatus
import com.nfar.allergydetective.data.db.model.IllnessRelationKind
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import com.nfar.allergydetective.data.db.model.PortionSize
import com.nfar.allergydetective.data.diary.toDomain
import com.nfar.allergydetective.domain.analysis.localEpochDay
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
        diary = toDiaryData(today = localEpochDay(now, nowTzOffsetMin)),
        now = now,
        nowTzOffsetMin = nowTzOffsetMin,
    )

private fun AnalysisRows.toReferenceData(): ReferenceData {
    val windowsByProperty = propertyWindows.groupBy({ it.propertyId }, { TimeWindow(it.startMin, it.endMin) })
    val groupsByProperty = propertySymptomGroups.groupBy({ it.propertyId }, { it.symptomGroupKey })
    val propertiesByIngredient = ingredientProperties.groupBy({ it.ingredientId }, { it.propertyId })
    return ReferenceData(
        properties = properties.map {
            TriggerProperty(
                id = it.id,
                windows = windowsByProperty[it.id].orEmpty(),
                symptomGroups = groupsByProperty[it.id].orEmpty().toSet(),
                heatLabile = it.key in HEAT_LABILE_PROPERTY_KEYS,
            )
        },
        ingredients = ingredients.map { IngredientRef(it.id, propertiesByIngredient[it.id].orEmpty().toSet()) },
        symptoms = symptoms.map { SymptomRef(it.id, it.groupKey) },
        knownAllergyPropertyIds = knownAllergies.map { it.propertyId }.toSet(),
    )
}

private fun AnalysisRows.toDiaryData(today: Long): DiaryData {
    val itemsByMeal = mealIngredients.groupBy({ it.mealId }, { MealItem(it.ingredientId, it.portion.toPortion(), it.preparation?.toDomain()) })
    // Self-test 18.11.: symptoms of an acute illness (e.g. sinusitis) would be blamed on food.
    // Since 19.11. a sick day counts with a lower weight and the user decides per symptom whether
    // it belongs to the illness (engine: AnalysisConfig.sickness). The factor itself is no candidate.
    val sickDays = dayFactors.filter { it.factorId == SICK_FACTOR_ID }.map { it.epochDay }.toSet() +
        sickPeriods.flatMap { (it.startDay..(it.endDay ?: today)).toList() }
    return DiaryData(
        meals = meals.map { Meal(it.id, it.eatenAt, it.tzOffsetMin, itemsByMeal[it.id].orEmpty()) },
        symptomEntries = symptomEntries.map {
            SymptomEntry(it.id, it.symptomId, it.startedAt, it.tzOffsetMin, it.intensity, it.illnessRelation?.toDomain())
        },
        dayChecks = dayChecks.map {
            DayCheck(it.epochDay, it.status.isConfirmed(), it.sleepHours, it.sleepQuality, it.stress, sick = it.epochDay in sickDays)
        },
        dayFactors = dayFactors.filter { it.factorId != SICK_FACTOR_ID }.map { DayFactor(it.epochDay, it.factorId) },
    )
}

/** Triggers destroyed by cooking. Ingredients with one of them ask "raw or cooked?". */
val HEAT_LABILE_PROPERTY_KEYS = setOf("birch_pollen_cross")

internal fun IllnessRelationKind.toDomain(): IllnessRelation = when (this) {
    IllnessRelationKind.RELATED -> IllnessRelation.RELATED
    IllnessRelationKind.UNRELATED -> IllnessRelation.UNRELATED
    IllnessRelationKind.UNKNOWN -> IllnessRelation.UNKNOWN
}

internal fun IllnessRelation.toEntity(): IllnessRelationKind = when (this) {
    IllnessRelation.RELATED -> IllnessRelationKind.RELATED
    IllnessRelation.UNRELATED -> IllnessRelationKind.UNRELATED
    IllnessRelation.UNKNOWN -> IllnessRelationKind.UNKNOWN
}

/** Lifestyle factor "sick" (seed id 4): the day counts with a lower weight (AnalysisConfig.sickness). */
const val SICK_FACTOR_ID = 4L

/** Only these days go into the analysis (TRD 2.2). */
private fun DayStatus.isConfirmed(): Boolean = this == DayStatus.NO_SYMPTOMS || this == DayStatus.COMPLETE

private fun PortionSize.toPortion(): Portion = when (this) {
    PortionSize.SMALL -> Portion.SMALL
    PortionSize.MEDIUM -> Portion.MEDIUM
    PortionSize.LARGE -> Portion.LARGE
}
