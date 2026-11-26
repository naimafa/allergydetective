package com.nfar.allergydetective.domain.experiment

import com.nfar.allergydetective.domain.analysis.model.DayCheck
import com.nfar.allergydetective.domain.analysis.model.DiaryData
import com.nfar.allergydetective.domain.analysis.model.IngredientRef
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.analysis.model.ReferenceData
import com.nfar.allergydetective.domain.analysis.model.TriggerProperty
import com.nfar.allergydetective.domain.analysis.model.Meal
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.SymptomEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExperimentTest {
    private val dayMs = 86_400_000L
    private val cheese = 302L

    /** Days 0..[last] finished; complaints (intensity 3) on [complaints]; cheese eaten on [cheeseDays]; [sick] days. */
    private fun diary(last: Int, complaints: Set<Int>, cheeseDays: Set<Int> = emptySet(), sick: Set<Int> = emptySet(), intensity: (Int) -> Int = { 3 }) = DiaryData(
        meals = cheeseDays.map { Meal(it.toLong(), it * dayMs + 8 * 3_600_000L, 0, listOf(MealItem(cheese, Portion.MEDIUM))) },
        symptomEntries = complaints.mapIndexed { i, d -> SymptomEntry(id = i.toLong(), symptomId = 1, startedAt = d * dayMs + 7 * 3_600_000L, tzOffsetMin = 0, intensity = intensity(d)) },
        dayChecks = (0..last).map { DayCheck(epochDay = it.toLong(), confirmed = true, sleepHours = null, sleepQuality = null, stress = null, sick = it in sick) },
        dayFactors = emptyList(),
    )

    @Test
    fun fewerComplaintsWithoutTheIngredient() {
        // User 25.11.2026: complaints nearly every morning with cheese, test without it from day 10.
        val test = Experiment(cheese, startDay = 10, days = 10)
        val result = evaluateExperiment(test, diary(19, complaints = (2..9).toSet() + setOf(12, 16), cheeseDays = (0..9).toSet()), today = 20)

        assertEquals(PeriodStats(days = 10, complaintDays = 8, strengthSum = 24), result.before)
        // Days 10 and 11 are the washout; 12..19 count.
        assertEquals(PeriodStats(days = 8, complaintDays = 2, strengthSum = 6), result.during)
        assertEquals(ExperimentVerdict.LESS, result.verdict)
        assertTrue(result.over)
    }

    @Test
    fun similarWhenItMakesNoDifference() {
        val test = Experiment(cheese, startDay = 10, days = 10)
        val result = evaluateExperiment(test, diary(19, complaints = setOf(1, 3, 5, 7, 9, 13, 15, 17, 19)), today = 20)

        assertEquals(ExperimentVerdict.SIMILAR, result.verdict)
    }

    @Test
    fun daysWhenItWasEatenAnywayAndSickDaysDoNotCount() {
        val test = Experiment(cheese, startDay = 10, days = 10)
        val result = evaluateExperiment(test, diary(19, complaints = setOf(14, 15), cheeseDays = setOf(14), sick = setOf(15)), today = 20)

        assertEquals(1, result.eatenDays)
        assertEquals(PeriodStats(days = 6, complaintDays = 0, strengthSum = 0), result.during)
    }

    @Test
    fun tooFewDaysWhileRunning() {
        // Started yesterday: still in the washout.
        val test = Experiment(cheese, startDay = 10, days = 14)
        val result = evaluateExperiment(test, diary(11, complaints = (0..11).toSet()), today = 11)

        assertEquals(ExperimentVerdict.TOO_FEW_DAYS, result.verdict)
        assertEquals(2, result.dayNumber)
        assertFalse(result.over)
    }

    @Test
    fun stoppingEndsTheTest() {
        val test = Experiment(cheese, startDay = 10, days = 14, stoppedDay = 15)
        val result = evaluateExperiment(test, diary(20, complaints = emptySet()), today = 20)

        assertTrue(result.over)
        assertEquals(6, result.dayNumber)
        assertEquals(PeriodStats(days = 4, complaintDays = 0, strengthSum = 0), result.during)
    }

    @Test
    fun aMilderColdEveryDayIsAnImprovement() {
        // User 25.11.2026: "es gab Beschwerden mit dem Schnupfen, nur weniger" - strength 3 before, 1 without cheese.
        val test = Experiment(cheese, startDay = 10, days = 10)
        val result = evaluateExperiment(test, diary(19, complaints = (0..19).toSet(), intensity = { if (it < 10) 3 else 1 }), today = 20)

        assertEquals(PeriodStats(days = 8, complaintDays = 8, strengthSum = 8), result.during)
        assertEquals(3.0, result.before.meanStrength)
        assertEquals(1.0, result.during.meanStrength)
        assertEquals(ExperimentVerdict.LESS, result.verdict)
    }

    private val milkProtein = 117L
    private val glutenId = 102L
    private val yoghurt = 303L
    private val bread = 401L
    private val reference = ReferenceData(
        properties = listOf(TriggerProperty(milkProtein, emptyList(), emptySet()), TriggerProperty(glutenId, emptyList(), emptySet(), gluten = true)),
        ingredients = listOf(IngredientRef(cheese, setOf(milkProtein)), IngredientRef(yoghurt, setOf(milkProtein)), IngredientRef(bread, setOf(glutenId))),
        symptoms = emptyList(),
    )

    @Test
    fun aGroupCountsEveryIngredientOfIt() {
        // User 26.11.2026: leaving out milk protein - yoghurt on day 14 breaks it like cheese would.
        val test = Experiment(0, startDay = 10, days = 10, propertyId = milkProtein)
        val base = diary(19, complaints = emptySet())
        val withYoghurt = base.copy(meals = listOf(Meal(1, 14 * dayMs + 8 * 3_600_000L, 0, listOf(MealItem(yoghurt, Portion.MEDIUM)))))
        val result = evaluateExperiment(test, withYoghurt, today = 20, leftOut = leftOutMatcher(test, reference))

        assertEquals(1, result.eatenDays)
        assertEquals(TestDayStatus.EATEN, result.daily.first { it.number == 5 }.status)
    }

    @Test
    fun glutenFreeBreadDoesNotBreakAGlutenTest() {
        val test = Experiment(0, startDay = 10, days = 10, propertyId = glutenId)
        val matcher = leftOutMatcher(test, reference)

        assertTrue(matcher(MealItem(bread, Portion.MEDIUM)))
        assertFalse(matcher(MealItem(bread, Portion.MEDIUM, Preparation.GLUTEN_FREE)))
        assertFalse(matcher(MealItem(cheese, Portion.MEDIUM)))
    }

    @Test
    fun everyDayOfTheTestIsInTheCourse() {
        val test = Experiment(cheese, startDay = 10, days = 14)
        val result = evaluateExperiment(test, diary(13, complaints = setOf(12), sick = setOf(13), intensity = { 4 }), today = 14)

        assertEquals(listOf(1, 2, 3, 4, 5), result.daily.map { it.number })
        assertEquals(TestDayStatus.WASHOUT, result.daily[0].status)
        assertEquals(TestDay(3, TestDayStatus.COUNTED, 4), result.daily[2])
        assertEquals(TestDay(4, TestDayStatus.NOT_COUNTED, null), result.daily[3])
        // Today is not finished yet.
        assertEquals(TestDay(5, TestDayStatus.NOT_COUNTED, null), result.daily[4])
    }

    @Test
    fun theDaysBeforeAreInTheCourseToo() {
        val test = Experiment(cheese, startDay = 10, days = 5)
        val result = evaluateExperiment(test, diary(12, complaints = setOf(6, 8), sick = setOf(7), intensity = { 2 }), today = 13)

        assertEquals(listOf(-5, -4, -3, -2, -1), result.beforeDaily.map { it.number })
        assertEquals(TestDay(-4, TestDayStatus.COUNTED, 2), result.beforeDaily[1])
        assertEquals(TestDay(-3, TestDayStatus.NOT_COUNTED, null), result.beforeDaily[2])
        assertEquals(TestDay(-1, TestDayStatus.COUNTED, 0), result.beforeDaily[4])
    }

    private fun meal(day: Int, vararg items: MealItem) = Meal(day.toLong() * 100 + items.size, day * dayMs + 12 * 3_600_000L, 0, items.toList())

    @Test
    fun otherFoodEatenMuchLessIsReported() {
        // Bread every day before, hardly during the cheese test: the change may come from the bread.
        val test = Experiment(cheese, startDay = 10, days = 10)
        val base = diary(19, complaints = emptySet())
        val meals = (0..9).map { meal(it, MealItem(bread, Portion.MEDIUM)) } + meal(15, MealItem(bread, Portion.MEDIUM))
        val result = evaluateExperiment(test, base.copy(meals = meals), today = 20, reference = reference, groupIds = listOf(milkProtein, glutenId))

        assertEquals(listOf(DietChange(glutenId, beforeDays = 10, beforeTotal = 10, duringDays = 1, duringTotal = 8)), result.dietChanges)
    }

    @Test
    fun whatTheLeftOutFoodCarriesIsNoChange() {
        // Yoghurt before, none during a milk protein test: that is the test itself, not another change.
        val test = Experiment(0, startDay = 10, days = 10, propertyId = milkProtein)
        val meals = (0..9).map { meal(it, MealItem(yoghurt, Portion.MEDIUM), MealItem(bread, Portion.MEDIUM)) } +
            (12..19).map { meal(it, MealItem(bread, Portion.MEDIUM)) }
        val result = evaluateExperiment(
            test, diary(19, complaints = emptySet()).copy(meals = meals), today = 20,
            leftOut = leftOutMatcher(test, reference), reference = reference, groupIds = listOf(milkProtein, glutenId),
        )

        assertEquals(emptyList(), result.dietChanges)
    }

    @Test
    fun glutenFreeBreadCountsAsLessGluten() {
        val test = Experiment(cheese, startDay = 10, days = 10)
        val meals = (0..9).map { meal(it, MealItem(bread, Portion.MEDIUM)) } +
            (12..19).map { meal(it, MealItem(bread, Portion.MEDIUM, Preparation.GLUTEN_FREE)) }
        val result = evaluateExperiment(test, diary(19, complaints = emptySet()).copy(meals = meals), today = 20, reference = reference, groupIds = listOf(glutenId))

        assertEquals(glutenId, result.dietChanges.single().propertyId)
    }
}
