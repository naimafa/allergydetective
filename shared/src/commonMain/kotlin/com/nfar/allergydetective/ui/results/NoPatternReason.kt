package com.nfar.allergydetective.ui.results

import com.nfar.allergydetective.domain.analysis.AnalysisConfig
import com.nfar.allergydetective.domain.analysis.localEpochDay
import com.nfar.allergydetective.domain.analysis.model.DiaryData

/**
 * Why the results show no pattern yet (user 25.11.2026: "say why, without getting technical").
 * Checked in this order; the first that applies is shown.
 */
enum class NoPatternReason {
    /** Fewer finished days than the first evaluation needs. */
    TOO_FEW_DAYS,

    /** No complaints on the finished days: nothing to look for. */
    NO_COMPLAINTS,

    /** Complaints on only one day: a pattern needs something that happens more than once. */
    FEW_COMPLAINT_DAYS,

    /** Complaints on (almost) every finished day: too few good days to compare with (user data 25.11.2026: 2 of 10). */
    NO_GOOD_DAYS,

    /** Enough days of both kinds, but no food stands out. */
    NOTHING_STANDS_OUT,
}

/** Complaint days needed before a pattern is possible (as `minRealHits` in the analysis). */
internal const val MIN_COMPLAINT_DAYS = 2

/** Days without complaints needed to compare with; fewer, and every food looks the same. */
internal const val MIN_GOOD_DAYS = 3

/** The reason with the counts behind it, for the report ("complaints on 8 of 10 days"). */
data class NoPatternFacts(val reason: NoPatternReason, val finishedDays: Int, val complaintDays: Int)

internal fun noPatternReason(
    diary: DiaryData,
    firstEvaluationDays: Int,
    minIntensity: Int = AnalysisConfig().dayLag.minIntensity,
): NoPatternReason = noPatternFacts(diary, firstEvaluationDays, minIntensity).reason

internal fun noPatternFacts(
    diary: DiaryData,
    firstEvaluationDays: Int,
    minIntensity: Int = AnalysisConfig().dayLag.minIntensity,
): NoPatternFacts {
    val finished = diary.dayChecks.filter { it.confirmed }.map { it.epochDay }.toSet()
    val complaintDays = diary.symptomEntries
        // The same bar as the day analysis: very light complaints do not make a complaint day.
        .filter { it.intensity >= minIntensity }
        .map { localEpochDay(it.startedAt, it.tzOffsetMin) }
        .filter { it in finished }
        .toSet()
    val reason = when {
        finished.size < firstEvaluationDays -> NoPatternReason.TOO_FEW_DAYS
        complaintDays.isEmpty() -> NoPatternReason.NO_COMPLAINTS
        complaintDays.size < MIN_COMPLAINT_DAYS -> NoPatternReason.FEW_COMPLAINT_DAYS
        finished.size - complaintDays.size < MIN_GOOD_DAYS -> NoPatternReason.NO_GOOD_DAYS
        else -> NoPatternReason.NOTHING_STANDS_OUT
    }
    return NoPatternFacts(reason, finished.size, complaintDays.size)
}
