package com.nfar.allergydetective.domain.experiment

import com.nfar.allergydetective.domain.analysis.AnalysisConfig
import com.nfar.allergydetective.domain.analysis.localEpochDay
import com.nfar.allergydetective.domain.analysis.model.DiaryData

/**
 * Weglass-Test (user 25.11.2026, KONZEPT_MUSTERERKENNUNG D): one ingredient is left out for [days] days from
 * [startDay] (local epoch day). The only way to check what is eaten almost every day. [stoppedDay] is set when the
 * test was ended early.
 */
data class Experiment(
    val ingredientId: Long,
    val startDay: Long,
    val days: Int,
    val stoppedDay: Long? = null,
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

/** Counted days of one side and how many of them had complaints. */
data class PeriodStats(val days: Int, val complaintDays: Int) {
    val rate: Double get() = if (days == 0) 0.0 else complaintDays.toDouble() / days
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
)

/** The first days without the ingredient do not count yet: reactions can come up to 2-3 days late. */
const val WASHOUT_DAYS = 2

/** Counted days needed on each side before the test says anything. */
const val MIN_COUNTED_DAYS = 5

/** Difference in the share of complaint days that counts as "clearly" (25 percentage points). */
const val CLEAR_DIFFERENCE = 0.25

/**
 * Compares the finished days before the test (as many as the test lasts) with the finished days without the
 * ingredient. Sick days and days on which it was eaten anyway are left out; complaints count from the same intensity
 * as the pattern search.
 */
fun evaluateExperiment(
    experiment: Experiment,
    diary: DiaryData,
    today: Long,
    minIntensity: Int = AnalysisConfig().dayLag.minIntensity,
): ExperimentResult {
    val usable = diary.dayChecks.filter { it.confirmed && !it.sick }.map { it.epochDay }.toSet()
    val complaintDays = diary.symptomEntries
        .filter { it.intensity >= minIntensity }
        .map { localEpochDay(it.startedAt, it.tzOffsetMin) }
        .toSet()
    val end = experiment.endDay(today)
    val eatenOn = diary.meals
        .filter { meal -> meal.items.any { it.ingredientId == experiment.ingredientId } }
        .map { localEpochDay(it.eatenAt, it.tzOffsetMin) }
        .filter { it in experiment.startDay..end }
        .toSet()
    val beforeDays = usable.filter { it in (experiment.startDay - experiment.days) until experiment.startDay }
    val duringDays = usable.filter { it in (experiment.startDay + WASHOUT_DAYS)..end && it !in eatenOn }
    val before = PeriodStats(beforeDays.size, beforeDays.count { it in complaintDays })
    val during = PeriodStats(duringDays.size, duringDays.count { it in complaintDays })
    val verdict = when {
        before.days < MIN_COUNTED_DAYS || during.days < MIN_COUNTED_DAYS -> ExperimentVerdict.TOO_FEW_DAYS
        during.rate <= before.rate - CLEAR_DIFFERENCE -> ExperimentVerdict.LESS
        during.rate >= before.rate + CLEAR_DIFFERENCE -> ExperimentVerdict.MORE
        else -> ExperimentVerdict.SIMILAR
    }
    return ExperimentResult(
        before = before,
        during = during,
        eatenDays = eatenOn.size,
        dayNumber = (end - experiment.startDay + 1).toInt().coerceIn(0, experiment.days),
        over = experiment.isOver(today),
        verdict = verdict,
    )
}
