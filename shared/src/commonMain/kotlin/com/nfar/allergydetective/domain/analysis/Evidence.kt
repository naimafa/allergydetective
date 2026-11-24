package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.analysis.model.ExposureStats

/** Lift with Laplace smoothing (TRD 2.4). 1.0 means "no difference". */
internal fun computeLift(stats: ExposureStats, config: AnalysisConfig): Double {
    val withRate = (stats.k1 + config.laplaceHits) / (stats.n1 + config.laplaceTotal)
    val withoutRate = (stats.k0 + config.laplaceHits) / (stats.n0 + config.laplaceTotal)
    return withRate / withoutRate
}

/**
 * Evidence level (TRD 2.8).
 * [wasSuspicionBefore] tells whether the candidate already was a suspicion
 * [EvidenceThresholds.strongStableDays] days ago.
 */
internal fun classifyEvidence(
    stats: ExposureStats,
    lift: Double,
    wasSuspicionBefore: Boolean,
    config: AnalysisConfig,
): EvidenceLevel {
    val t = config.evidence
    val isSuspicion = lift.atLeast(t.suspicionMinLift) && stats.k1.atLeast(t.suspicionMinHits)
    val isStrong = lift.atLeast(t.strongMinLift) && stats.k1.atLeast(t.strongMinHits) &&
        stats.n1.atLeast(t.strongMinExposures)
    return when {
        !stats.n1.atLeast(t.minExposures) -> EvidenceLevel.INSUFFICIENT_DATA
        stats.hitCount < t.minRealHits -> EvidenceLevel.NONE
        isSuspicion && isStrong && wasSuspicionBefore -> EvidenceLevel.STRONG_SUSPICION
        isSuspicion -> EvidenceLevel.SUSPICION
        lift.atLeast(t.traceMinLift) -> EvidenceLevel.TRACE
        else -> EvidenceLevel.NONE
    }
}

/** Summed weights like 0.6 + 0.6 + … carry rounding errors, so thresholds get a small tolerance. */
private const val THRESHOLD_EPSILON = 1e-9

private fun Double.atLeast(threshold: Double): Boolean = this >= threshold - THRESHOLD_EPSILON
