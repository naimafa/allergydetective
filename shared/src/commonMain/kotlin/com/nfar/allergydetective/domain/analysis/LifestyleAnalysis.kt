package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.DayCheck
import com.nfar.allergydetective.domain.analysis.model.DiaryData
import com.nfar.allergydetective.domain.analysis.model.ExposureStats

/**
 * Lifestyle candidates (TRD 2.5): exposure is a valid day with the factor, a hit is an episode on that day.
 * Only days before today count, because today is not finished yet.
 */
internal fun lifestyleStats(
    diary: DiaryData,
    episodes: List<Episode>,
    validDays: Set<Long>,
    today: Long,
    config: AnalysisConfig,
): Map<Candidate, ExposureStats> {
    val days = validDays.filter { it < today }
    val weightByDay = episodes
        .groupBy { it.epochDay }
        .mapValues { (_, dayEpisodes) -> dayEpisodes.maxOf { it.maxIntensity }.toDouble() / config.maxIntensity }

    return lifestyleExposureDays(diary, validDays, today, config)
        .mapValues { (_, exposedDays) -> dayStats(days, weightByDay, exposedDays) }
}

/**
 * Valid days before today per lifestyle candidate. A candidate that was only logged
 * on days that do not count yet is included with an empty set.
 */
internal fun lifestyleExposureDays(
    diary: DiaryData,
    validDays: Set<Long>,
    today: Long,
    config: AnalysisConfig,
): Map<Candidate, Set<Long>> {
    val daysByCandidate = mutableMapOf<Candidate, List<Long>>()
    val badSleepDays = diary.dayChecks.filter { isBadSleep(it, config) }.map { it.epochDay }
    if (badSleepDays.isNotEmpty()) daysByCandidate[Candidate.BadSleep] = badSleepDays
    val highStressDays = diary.dayChecks.filter { isHighStress(it, config) }.map { it.epochDay }
    if (highStressDays.isNotEmpty()) daysByCandidate[Candidate.HighStress] = highStressDays
    diary.dayFactors
        .groupBy({ it.factorId }, { it.epochDay })
        .forEach { (factorId, factorDays) -> daysByCandidate[Candidate.DayFactor(factorId)] = factorDays }

    return daysByCandidate.mapValues { (_, days) -> days.filter { it in validDays && it < today }.toSet() }
}

private fun dayStats(days: List<Long>, weightByDay: Map<Long, Double>, exposedDays: Set<Long>): ExposureStats {
    var n1 = 0.0
    var k1 = 0.0
    var n0 = 0.0
    var k0 = 0.0
    var hitCount = 0
    for (day in days) {
        val weight = weightByDay[day] ?: 0.0
        if (day in exposedDays) {
            n1 += 1.0
            k1 += weight
            if (weight > 0.0) hitCount++
        } else {
            n0 += 1.0
            k0 += weight
        }
    }
    return ExposureStats(n1, k1, n0, k0, exposureCount = n1.toInt(), hitCount = hitCount)
}

private fun isBadSleep(check: DayCheck, config: AnalysisConfig): Boolean {
    val t = config.lifestyle
    val poorQuality = check.sleepQuality?.let { it <= t.badSleepMaxQuality } == true
    val tooShort = check.sleepHours?.let { it < t.badSleepMinHours } == true
    return poorQuality || tooShort
}

private fun isHighStress(check: DayCheck, config: AnalysisConfig): Boolean =
    check.stress?.let { it >= config.lifestyle.highStressMin } == true
