package com.nfar.allergydetective.data.analysis

import com.nfar.allergydetective.data.db.entity.DayCheckEntity
import com.nfar.allergydetective.data.db.entity.DayFactorEntity
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
import com.nfar.allergydetective.data.db.model.DayStatus
import com.nfar.allergydetective.data.db.model.PortionSize
import com.nfar.allergydetective.domain.analysis.AnalysisEngine
import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.DayCheck
import com.nfar.allergydetective.domain.analysis.model.DayFactor
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.analysis.model.IngredientRef
import com.nfar.allergydetective.domain.analysis.model.Meal
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.SymptomEntry
import com.nfar.allergydetective.domain.analysis.model.SymptomRef
import com.nfar.allergydetective.domain.analysis.model.TimeWindow
import com.nfar.allergydetective.domain.analysis.model.TriggerProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalysisInputMapperTest {

    private companion object {
        const val LACTOSE = 103L
        const val HISTAMINE = 101L
        const val MILK = 1L
        const val RICE = 3L
        const val BLOATING = 1002L
        const val TZ = 60
        const val HOUR = 3_600_000L
        const val DAY = 24 * HOUR
        const val START_DAY = 20_000L
    }

    private val emptyRows = AnalysisRows(
        properties = emptyList(),
        propertyWindows = emptyList(),
        propertySymptomGroups = emptyList(),
        ingredients = emptyList(),
        ingredientProperties = emptyList(),
        symptoms = emptyList(),
        knownAllergies = emptyList(),
        meals = emptyList(),
        mealIngredients = emptyList(),
        symptomEntries = emptyList(),
        dayChecks = emptyList(),
        dayFactors = emptyList(),
    )

    private fun ingredient(id: Long) =
        IngredientEntity(id = id, key = "ingredient_$id", customName = null, categoryId = 1, isCustom = false, baseIngredientId = null)

    private val referenceRows = emptyRows.copy(
        properties = listOf(
            PropertyEntity(LACTOSE, "lactose", "intolerance"),
            PropertyEntity(HISTAMINE, "histamine", "intolerance"),
        ),
        propertyWindows = listOf(
            PropertyWindowEntity(id = 1, propertyId = LACTOSE, startMin = 15, endMin = 360),
            PropertyWindowEntity(id = 2, propertyId = HISTAMINE, startMin = 0, endMin = 120),
            PropertyWindowEntity(id = 3, propertyId = HISTAMINE, startMin = 240, endMin = 1440),
        ),
        propertySymptomGroups = listOf(
            PropertySymptomGroupEntity(LACTOSE, "digestive"),
            PropertySymptomGroupEntity(HISTAMINE, "skin"),
            PropertySymptomGroupEntity(HISTAMINE, "head"),
        ),
        ingredients = listOf(ingredient(MILK), ingredient(RICE)),
        ingredientProperties = listOf(IngredientPropertyEntity(MILK, LACTOSE)),
        symptoms = listOf(SymptomEntity(BLOATING, "bloating", "digestive", isRedFlag = false)),
    )

    @Test
    fun propertiesGetTheirWindowsAndSymptomGroups() {
        val reference = referenceRows.toAnalysisInput(now = 0, nowTzOffsetMin = TZ).reference

        assertEquals(
            listOf(
                TriggerProperty(LACTOSE, listOf(TimeWindow(15, 360)), setOf("digestive")),
                TriggerProperty(HISTAMINE, listOf(TimeWindow(0, 120), TimeWindow(240, 1440)), setOf("skin", "head")),
            ),
            reference.properties,
        )
    }

    @Test
    fun ingredientsGetTheirPropertiesAndMayHaveNone() {
        val reference = referenceRows.toAnalysisInput(now = 0, nowTzOffsetMin = TZ).reference

        assertEquals(listOf(IngredientRef(MILK, setOf(LACTOSE)), IngredientRef(RICE, emptySet())), reference.ingredients)
        assertEquals(listOf(SymptomRef(BLOATING, "digestive")), reference.symptoms)
    }

    @Test
    fun knownAllergiesAndNowArePassedOn() {
        val input = referenceRows
            .copy(knownAllergies = listOf(KnownAllergyEntity(LACTOSE)))
            .toAnalysisInput(now = 1_234L, nowTzOffsetMin = 120)

        assertEquals(setOf(LACTOSE), input.reference.knownAllergyPropertyIds)
        assertEquals(1_234L, input.now)
        assertEquals(120, input.nowTzOffsetMin)
    }

    @Test
    fun mealsGetTheirIngredientsWithPortions() {
        val rows = referenceRows.copy(
            meals = listOf(
                MealEntity(id = 10, eatenAt = 5_000, tzOffsetMin = TZ, favoriteId = null),
                MealEntity(id = 11, eatenAt = 9_000, tzOffsetMin = 120, favoriteId = 7),
            ),
            mealIngredients = listOf(
                MealIngredientEntity(mealId = 10, ingredientId = MILK, portion = PortionSize.LARGE),
                MealIngredientEntity(mealId = 10, ingredientId = RICE, portion = PortionSize.SMALL),
                MealIngredientEntity(mealId = 11, ingredientId = RICE, portion = PortionSize.MEDIUM),
            ),
        )

        val meals = rows.toAnalysisInput(now = 0, nowTzOffsetMin = TZ).diary.meals

        assertEquals(
            listOf(
                Meal(10, 5_000, TZ, listOf(MealItem(MILK, Portion.LARGE), MealItem(RICE, Portion.SMALL))),
                Meal(11, 9_000, 120, listOf(MealItem(RICE, Portion.MEDIUM))),
            ),
            meals,
        )
    }

    @Test
    fun onlyNoSymptomsAndCompleteDaysAreConfirmed() {
        val rows = referenceRows.copy(
            dayChecks = listOf(
                DayCheckEntity(epochDay = 1, wellbeing = 3, sleepHours = 7.5f, sleepQuality = 4, stress = 2, status = DayStatus.OPEN),
                DayCheckEntity(epochDay = 2, wellbeing = null, sleepHours = null, sleepQuality = null, stress = null, status = DayStatus.NO_SYMPTOMS),
                DayCheckEntity(epochDay = 3, wellbeing = 2, sleepHours = 5f, sleepQuality = 1, stress = 5, status = DayStatus.COMPLETE),
            ),
            dayFactors = listOf(DayFactorEntity(epochDay = 3, factorId = 501)),
        )

        val diary = rows.toAnalysisInput(now = 0, nowTzOffsetMin = TZ).diary

        assertEquals(
            listOf(
                DayCheck(1, confirmed = false, sleepHours = 7.5f, sleepQuality = 4, stress = 2),
                DayCheck(2, confirmed = true, sleepHours = null, sleepQuality = null, stress = null),
                DayCheck(3, confirmed = true, sleepHours = 5f, sleepQuality = 1, stress = 5),
            ),
            diary.dayChecks,
        )
        assertEquals(listOf(DayFactor(3, 501)), diary.dayFactors)
    }

    @Test
    fun sickDaysAreLeftOutAndSickIsNoCandidate() {
        val rows = referenceRows.copy(
            dayChecks = listOf(
                DayCheckEntity(epochDay = 2, wellbeing = 1, sleepHours = null, sleepQuality = null, stress = null, status = DayStatus.COMPLETE),
                DayCheckEntity(epochDay = 3, wellbeing = 4, sleepHours = null, sleepQuality = null, stress = null, status = DayStatus.COMPLETE),
            ),
            dayFactors = listOf(DayFactorEntity(epochDay = 2, factorId = SICK_FACTOR_ID), DayFactorEntity(epochDay = 2, factorId = 501)),
        )

        val diary = rows.toAnalysisInput(now = 0, nowTzOffsetMin = TZ).diary

        assertEquals(listOf(false, true), diary.dayChecks.map { it.confirmed })
        assertEquals(listOf(DayFactor(2, 501)), diary.dayFactors)
    }

    @Test
    fun symptomEntriesAreMappedWithoutTheNote() {
        val rows = referenceRows.copy(
            symptomEntries = listOf(
                SymptomEntryEntity(id = 20, symptomId = BLOATING, startedAt = 7_000, tzOffsetMin = TZ, intensity = 4, note = "after lunch"),
            ),
        )

        val entries = rows.toAnalysisInput(now = 0, nowTzOffsetMin = TZ).diary.symptomEntries

        assertEquals(listOf(SymptomEntry(20, BLOATING, 7_000, TZ, 4)), entries)
    }

    /** Database rows of three weeks with milk every third day → the engine finds lactose. */
    @Test
    fun mappedRowsLetTheEngineDetectLactose() {
        fun utc(day: Int, hour: Int, minute: Int = 0) = (START_DAY + day) * DAY + hour * HOUR + minute * 60_000L - TZ * 60_000L
        val meals = mutableListOf<MealEntity>()
        val items = mutableListOf<MealIngredientEntity>()
        val entries = mutableListOf<SymptomEntryEntity>()
        for (day in 0 until 21) {
            val riceMeal = 100L + day
            meals += MealEntity(riceMeal, utc(day, 12), TZ, favoriteId = null)
            items += MealIngredientEntity(riceMeal, RICE, PortionSize.MEDIUM)
            if (day % 3 == 0) {
                val milkMeal = 200L + day
                meals += MealEntity(milkMeal, utc(day, 15), TZ, favoriteId = null)
                items += MealIngredientEntity(milkMeal, MILK, PortionSize.MEDIUM)
                entries += SymptomEntryEntity(300L + day, BLOATING, utc(day, 16, 30), TZ, intensity = 4, note = null)
            }
        }
        val rows = referenceRows.copy(
            meals = meals,
            mealIngredients = items,
            symptomEntries = entries,
            dayChecks = (0 until 21).map { DayCheckEntity(START_DAY + it, null, null, null, null, DayStatus.COMPLETE) },
        )

        val result = AnalysisEngine().analyze(rows.toAnalysisInput(now = utc(24, 12), nowTzOffsetMin = TZ))

        assertTrue(result.resultFor(Candidate.Property(LACTOSE))!!.level >= EvidenceLevel.SUSPICION)
    }
}
