package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.Portion

/**
 * All thresholds of the analysis in one place (CODING_RICHTLINIEN 2).
 * The defaults are the start values from TRD 2 and will be calibrated in the self-test.
 */
data class AnalysisConfig(
    /** Entries of the same symptom group within this time from the episode start form one episode (TRD 2.2). */
    val episodeMergeWindowMin: Int = 180,
    /** Hit weight = intensity / maxIntensity (TRD 2.3). */
    val maxIntensity: Int = 5,
    /** Portion factors (TRD 1.4). */
    val portionFactors: Map<Portion, Double> = mapOf(
        Portion.SMALL to 0.5,
        Portion.MEDIUM to 1.0,
        Portion.LARGE to 2.0,
    ),
    /** Laplace smoothing of the lift: (k + hits) / (n + total) (TRD 2.4). */
    val laplaceHits: Double = 1.0,
    val laplaceTotal: Double = 2.0,
    val evidence: EvidenceThresholds = EvidenceThresholds(),
    val safeList: SafeListThresholds = SafeListThresholds(),
    val lifestyle: LifestyleThresholds = LifestyleThresholds(),
) {
    fun portionFactor(portion: Portion): Double = portionFactors.getValue(portion)
}

/** Evidence levels (TRD 2.8). n1 is the portion-weighted exposure count. */
data class EvidenceThresholds(
    val minExposures: Double = 3.0,
    val traceMinLift: Double = 1.5,
    val suspicionMinLift: Double = 2.0,
    val suspicionMinHits: Double = 3.0,
    val strongMinLift: Double = 2.5,
    val strongMinHits: Double = 4.0,
    val strongMinExposures: Double = 6.0,
    /** A suspicion must already have existed this many days ago to become strong. */
    val strongStableDays: Int = 14,
)

/** Safe list (TRD 2.9): unweighted exposures and no plausible hit. */
data class SafeListThresholds(
    val minExposures: Int = 3,
)

/** Lifestyle candidates (TRD 2.5). */
data class LifestyleThresholds(
    val badSleepMaxQuality: Int = 2,
    val badSleepMinHours: Float = 6f,
    val highStressMin: Int = 4,
)
