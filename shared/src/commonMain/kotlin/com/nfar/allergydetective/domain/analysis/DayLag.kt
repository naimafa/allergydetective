package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.DiaryData
import com.nfar.allergydetective.domain.analysis.model.ExposureStats
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation

/**
 * Day analysis with a delay (concept KONZEPT_MUSTERERKENNUNG.md, user 24.11.2026).
 *
 * The unit is a day, not a meal. For every food candidate and every delay of 0..[DayLagThresholds.maxLagDays] days:
 * - **with:** days on which it was eaten → was the day [delay] later a day with complaints?
 * - **without:** days after a day without it → complaints?
 * The delay with the clearest difference is the candidate's typical delay. All complaints count together.
 */
internal data class DayLagResult(
    val stats: ExposureStats,
    /** Typical delay in days (0 = same day), null while there is no day with complaints after it. */
    val lagDays: Int?,
    /** Eaten so often that there are too few clean days to compare with ("fast täglich"). */
    val tooOften: Boolean,
    /** Found as a fast reaction: complaints within [DayLagThresholds.fastWindowMin] minutes after the meal. */
    val fast: Boolean = false,
)

/** Weight of the complaints of a day: strongest intensity / max of complaints from [DayLagThresholds.minIntensity] on, less on sick days. */
internal fun complaintDays(
    diary: DiaryData,
    validDays: Set<Long>,
    sickDays: Set<Long>,
    config: AnalysisConfig,
): Map<Long, Double> {
    val result = mutableMapOf<Long, Double>()
    // User 24.11.2026: a complaint lasting several days is one reaction - only the day it began counts.
    val logged = diary.symptomEntries.map { it.symptomId to localEpochDay(it.startedAt, it.tzOffsetMin) }.toSet()
    for (entry in diary.symptomEntries) {
        if (entry.intensity < config.dayLag.minIntensity) continue
        val day = localEpochDay(entry.startedAt, entry.tzOffsetMin)
        if ((entry.symptomId to day - 1) in logged) continue
        if (day !in validDays) continue
        val weight = if (day !in sickDays) {
            1.0
        } else {
            when (entry.illnessRelation) {
                IllnessRelation.RELATED -> 0.0
                IllnessRelation.UNRELATED -> 1.0
                IllnessRelation.UNKNOWN, null -> config.sickness.unknownRelationWeight
            }
        }
        // Like the meal analysis: the strongest complaint of the day, intensity / max (TRD 2.3).
        val value = weight * entry.intensity / config.maxIntensity
        if (value > 0.0) result[day] = maxOf(result[day] ?: 0.0, value)
    }
    return result
}

/** Largest portion factor of the candidate per local day. */
internal fun exposureDays(diary: DiaryData, candidate: Candidate, catalog: FoodCatalog, config: AnalysisConfig): Map<Long, Double> {
    val result = mutableMapOf<Long, Double>()
    for (meal in diary.meals) {
        val portion = catalog.exposurePortion(meal, candidate, config) ?: continue
        val day = localEpochDay(meal.eatenAt, meal.tzOffsetMin)
        result[day] = maxOf(result[day] ?: 0.0, portion)
    }
    return result
}

internal fun dayLagStats(
    exposure: Map<Long, Double>,
    complaints: Map<Long, Double>,
    validDays: Set<Long>,
    sickDays: Set<Long>,
    config: AnalysisConfig,
): DayLagResult {
    val t = config.dayLag
    val dayWeight = { day: Long -> if (day in sickDays) config.sickness.dayWeight else 1.0 }

    // For every delay: target days after a day with the candidate against target days after a day without it.
    val perLag = (0..t.maxLagDays).map { lag ->
        var n1 = 0.0
        var k1 = 0.0
        var count = 0
        var hits = 0
        var n0 = 0.0
        var k0 = 0.0
        var without = 0
        var withoutHits = 0
        for (target in validDays) {
            val source = target - lag
            if (source !in validDays) continue
            val c = complaints[target] ?: 0.0
            val portion = exposure[source]
            if (portion != null) {
                val w = portion * dayWeight(source)
                n1 += w
                k1 += c * w
                count++
                if (c > 0.0) hits++
            } else {
                val w = dayWeight(source)
                n0 += w
                k0 += c * w
                without++
                if (c > 0.0) withoutHits++
            }
        }
        ExposureStats(n1, k1, n0, k0, count, hits, without, withoutHits)
    }

    // The clearest delay among those with enough real complaints; otherwise the one with the most complaints.
    val withHits = perLag.withIndex().filter { it.value.hitCount >= config.evidence.minRealHits }
    val best = withHits.maxByOrNull { computeLift(it.value, config) }
        ?: perLag.withIndex().maxBy { it.value.hitCount }
    return DayLagResult(
        stats = best.value,
        lagDays = best.index.takeIf { best.value.hitCount > 0 },
        // User 24.11.2026: only after enough finished days, and only for food on most of them.
        tooOften = validDays.size >= t.minDaysForDaily && exposure.keys.count { it in validDays } >= validDays.size * t.dailyShare,
    )
}
