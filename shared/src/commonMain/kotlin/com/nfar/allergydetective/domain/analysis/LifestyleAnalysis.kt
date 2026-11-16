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
    val checksByDay = diary.dayChecks.associateBy { it.epochDay }
    val factorsByDay = diary.dayFactors.groupBy({ it.epochDay }, { it.factorId })

    val exposures = mutableMapOf<Candidate, (Long) -> Boolean>()
    if (diary.dayChecks.any { isBadSleep(it, config) }) {
        exposures[Candidate.BadSleep] = { day -> checksByDay[day]?.let { isBadSleep(it, config) } == true }
    }
    if (diary.dayChecks.any { isHighStress(it, config) }) {
        exposures[Candidate.HighStress] = { day -> checksByDay[day]?.let { isHighStress(it, config) } == true }
    }
    diary.dayFactors.map { it.factorId }.distinct().forEach { factorId ->
        exposures[Candidate.DayFactor(factorId)] = { day -> factorId in factorsByDay[day].orEmpty() }
    }

    return exposures.mapValues { (_, isExposed) -> dayStats(days, weightByDay, isExposed) }
}

private fun dayStats(days: List<Long>, weightByDay: Map<Long, Double>, isExposed: (Long) -> Boolean): ExposureStats {
    var n1 = 0.0
    var k1 = 0.0
    var n0 = 0.0
    var k0 = 0.0
    var hitCount = 0
    for (day in days) {
        val weight = weightByDay[day] ?: 0.0
        if (isExposed(day)) {
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
