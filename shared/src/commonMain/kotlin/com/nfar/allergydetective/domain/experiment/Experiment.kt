package com.nfar.allergydetective.domain.experiment

import com.nfar.allergydetective.domain.analysis.localEpochDay
import com.nfar.allergydetective.domain.analysis.model.DiaryData
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Preparation
import com.nfar.allergydetective.domain.analysis.model.ReferenceData

/**
 * Weglass-Test (user 25.11.2026, KONZEPT_MUSTERERKENNUNG D): one ingredient is left out for [days] days from
 * [startDay] (local epoch day). The only way to check what is eaten almost every day. [stoppedDay] is set when the
 * test was ended early.
 *
 * User 26.11.2026: or a whole group ([propertyId], e.g. milk protein) - leaving out only milk while still eating cheese
 * and yoghurt says nothing. Then [ingredientId] is 0.
 */
data class Experiment(
    val ingredientId: Long,
    val startDay: Long,
    val days: Int,
    val stoppedDay: Long? = null,
    val propertyId: Long? = null,
) {
    val lastDay: Long get() = startDay + days - 1

    /** The last day that counts: today while running, the planned or the stop day once over. */
    fun endDay(today: Long): Long = minOf(stoppedDay ?: lastDay, lastDay, today)

    fun isOver(today: Long): Boolean = stoppedDay != null || today > lastDay

    companion object {
        val DURATIONS = listOf(7, 14, 21)
        const val DEFAULT_DAYS = 14
    }
}

/**
 * Counted days of one side, how many of them had complaints, and the sum of each day's strongest complaint (0 for a
 * day without). User 25.11.2026: a weaker cold still counts - the mean strength shows "fewer and milder".
 */
data class PeriodStats(val days: Int, val complaintDays: Int, val strengthSum: Int = 0) {
    /** Mean of the strongest complaint per day, 0 to 5. */
    val meanStrength: Double get() = if (days == 0) 0.0 else strengthSum.toDouble() / days
}

enum class ExperimentVerdict {
    /** Fewer than [MIN_COUNTED_DAYS] counted days before or during the test. */
    TOO_FEW_DAYS,

    /** Clearly fewer complaint days without the ingredient. */
    LESS,
    SIMILAR,

    /** Even more complaint days without it. */
    MORE,
}

data class ExperimentResult(
    val before: PeriodStats,
    val during: PeriodStats,
    /** Days of the test on which the ingredient was eaten anyway; they do not count. */
    val eatenDays: Int,
    /** Day of the test today (1 = the start day), at most [Experiment.days]. */
    val dayNumber: Int,
    val over: Boolean,
    val verdict: ExperimentVerdict,
    /** Every day of the test up to today, for the course (user 26.11.2026). */
    val daily: List<TestDay> = emptyList(),
    /** The days before the test it is compared with, oldest first (number -N..-1; user 26.11.2026). */
    val beforeDaily: List<TestDay> = emptyList(),
    /** Other groups eaten clearly more or less often during the test (user 26.11.2026). */
    val dietChanges: List<DietChange> = emptyList(),
)

/** Why a day of the test counts or not. */
enum class TestDayStatus { COUNTED, WASHOUT, EATEN, NOT_COUNTED }

/** [number] 1 = start day; [strength] the strongest complaint (0 = none), null when not entered. */
data class TestDay(val number: Int, val status: TestDayStatus, val strength: Int?)

/** The first days without the ingredient do not count yet: reactions can come up to 2-3 days late. */
const val WASHOUT_DAYS = 2

/** Counted days needed on each side before the test says anything. */
const val MIN_COUNTED_DAYS = 5

/** Difference in the mean daily strength (0 to 5) that counts as "clearly". */
const val CLEAR_DIFFERENCE = 1.0

/**
 * Compares the finished days before the test (as many as the test lasts) with the finished days without the
 * ingredient by the mean strength of the strongest complaint per day. Sick days and days on which it was eaten anyway
 * are left out. Every complaint counts, also a light one (user 25.11.2026).
 */
fun evaluateExperiment(
    experiment: Experiment,
    diary: DiaryData,
    today: Long,
    leftOut: (MealItem) -> Boolean = { it.ingredientId == experiment.ingredientId },
    /** For other changes in what was eaten: the groups to compare and which ingredients belong to them. */
    reference: ReferenceData? = null,
    groupIds: List<Long> = emptyList(),
): ExperimentResult {
    val usable = diary.dayChecks.filter { it.confirmed && !it.sick }.map { it.epochDay }.toSet()
    val strongest: Map<Long, Int> = diary.symptomEntries
        .filter { it.intensity > 0 }
        .groupBy({ localEpochDay(it.startedAt, it.tzOffsetMin) }, { it.intensity })
        .mapValues { (_, intensities) -> intensities.max() }
    val end = experiment.endDay(today)
    val eatenOn = diary.meals
        .filter { meal -> meal.items.any(leftOut) }
        .map { localEpochDay(it.eatenAt, it.tzOffsetMin) }
        .filter { it in experiment.startDay..end }
        .toSet()
    val beforeDays = usable.filter { it in (experiment.startDay - experiment.days) until experiment.startDay }
    val duringDays = usable.filter { it in (experiment.startDay + WASHOUT_DAYS)..end && it !in eatenOn }
    fun stats(days: List<Long>) = PeriodStats(days.size, days.count { it in strongest }, days.sumOf { strongest[it] ?: 0 })
    val before = stats(beforeDays)
    val during = stats(duringDays)
    val verdict = when {
        before.days < MIN_COUNTED_DAYS || during.days < MIN_COUNTED_DAYS -> ExperimentVerdict.TOO_FEW_DAYS
        during.meanStrength <= before.meanStrength - CLEAR_DIFFERENCE -> ExperimentVerdict.LESS
        during.meanStrength >= before.meanStrength + CLEAR_DIFFERENCE -> ExperimentVerdict.MORE
        else -> ExperimentVerdict.SIMILAR
    }
    return ExperimentResult(
        before = before,
        during = during,
        eatenDays = eatenOn.size,
        dayNumber = (end - experiment.startDay + 1).toInt().coerceIn(0, experiment.days),
        over = experiment.isOver(today),
        verdict = verdict,
        dietChanges = if (reference == null) emptyList() else dietChanges(diary, reference, groupIds, leftOut, beforeDays, duringDays),
        beforeDaily = ((experiment.startDay - experiment.days) until experiment.startDay).map { day ->
            val counted = day in usable
            TestDay((day - experiment.startDay).toInt(), if (counted) TestDayStatus.COUNTED else TestDayStatus.NOT_COUNTED, if (counted) strongest[day] ?: 0 else null)
        },
        daily = (experiment.startDay..end).map { day ->
            val status = when {
                day !in usable -> TestDayStatus.NOT_COUNTED
                day in eatenOn -> TestDayStatus.EATEN
                day < experiment.startDay + WASHOUT_DAYS -> TestDayStatus.WASHOUT
                else -> TestDayStatus.COUNTED
            }
            TestDay((day - experiment.startDay + 1).toInt(), status, if (day in usable) strongest[day] ?: 0 else null)
        },
    )
}

/**
 * What the test leaves out: the one ingredient, or every ingredient of the group. A product eaten in its gluten-free
 * version does not count for gluten (user 25.11.2026).
 */
fun leftOutMatcher(experiment: Experiment, reference: ReferenceData): (MealItem) -> Boolean {
    val propertyId = experiment.propertyId ?: return { it.ingredientId == experiment.ingredientId }
    val members = reference.ingredients.filter { propertyId in it.propertyIds }.map { it.id }.toSet()
    val gluten = reference.properties.any { it.id == propertyId && it.gluten }
    return { item -> item.ingredientId in members && !(gluten && item.preparation == Preparation.GLUTEN_FREE) }
}

/** A group eaten on [beforeDays] of [beforeTotal] counted days before and on [duringDays] of [duringTotal] without. */
data class DietChange(val propertyId: Long, val beforeDays: Int, val beforeTotal: Int, val duringDays: Int, val duringTotal: Int) {
    val beforeShare: Double get() = beforeDays.toDouble() / beforeTotal
    val duringShare: Double get() = duringDays.toDouble() / duringTotal
}

/** Difference in the share of days a group was eaten on (0 to 1) that counts as a change of diet. */
const val DIET_CHANGE = 0.4

/**
 * User 26.11.2026 ("andere Veränderungen in der Ernährung"): if something else was also eaten much more or less often
 * while the food was left out, that may be the reason for the change. What the left-out food itself carries does not
 * count (no milk protein = less lactose, of course): only the other ingredients of a meal are looked at.
 */
private fun dietChanges(
    diary: DiaryData,
    reference: ReferenceData,
    groupIds: List<Long>,
    leftOut: (MealItem) -> Boolean,
    beforeDays: List<Long>,
    duringDays: List<Long>,
): List<DietChange> {
    if (beforeDays.size < MIN_COUNTED_DAYS || duringDays.size < MIN_COUNTED_DAYS) return emptyList()
    val propertiesOf = reference.ingredients.associate { it.id to it.propertyIds }
    val glutenIds = reference.properties.filter { it.gluten }.map { it.id }.toSet()
    // Groups eaten per day, without the left-out items; a gluten-free version carries no gluten.
    val groupsByDay: Map<Long, Set<Long>> = diary.meals
        .groupBy { localEpochDay(it.eatenAt, it.tzOffsetMin) }
        .mapValues { (_, meals) ->
            meals.flatMap { it.items }.filterNot(leftOut).flatMap { item ->
                val props = propertiesOf[item.ingredientId].orEmpty()
                if (item.preparation == Preparation.GLUTEN_FREE) props - glutenIds else props
            }.toSet()
        }
    return groupIds.map { id ->
        DietChange(
            propertyId = id,
            beforeDays = beforeDays.count { id in groupsByDay[it].orEmpty() },
            beforeTotal = beforeDays.size,
            duringDays = duringDays.count { id in groupsByDay[it].orEmpty() },
            duringTotal = duringDays.size,
        )
    }.filter { kotlin.math.abs(it.beforeShare - it.duringShare) >= DIET_CHANGE }
}
