package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
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
    val grouping: GroupingThresholds = GroupingThresholds(),
    val sickness: SicknessWeights = SicknessWeights(),
    val dayLag: DayLagThresholds = DayLagThresholds(),
) {
    fun portionFactor(portion: Portion): Double = portionFactors.getValue(portion)
}

/** Evidence levels (TRD 2.8). n1 is the portion-weighted exposure count. */
data class EvidenceThresholds(
    val minExposures: Double = 3.0,
    /**
     * User 24.11.2026: with smoothing, "0 of 5 times, otherwise 0 of 19" became a trace. Every level now needs at least
     * this many meals with the candidate that were really followed by a complaint. A safe ingredient (no hit) is never a trace.
     */
    val minRealHits: Int = 2,
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

/** Indistinguishable candidates (TRD 2.7). */
data class GroupingThresholds(
    /** Both candidates must share at least this part of their exposures with each other. */
    val minSharedRatio: Double = 0.8,
    /** A group is only reported when at least one member reaches this level. */
    val minLevel: EvidenceLevel = EvidenceLevel.TRACE,
)

/**
 * Sick days (self-test 19.11.). Meals and symptoms of a sick day count with [dayWeight]; a symptom
 * with an unknown relation to the illness additionally with [unknownRelationWeight]; symptoms that
 * belong to the illness do not count. Lifestyle candidates leave sick days out. **Preliminary.**
 */
data class SicknessWeights(
    val dayWeight: Double = 0.5,
    val unknownRelationWeight: Double = 0.5,
)

/** Day analysis with a delay (KONZEPT_MUSTERERKENNUNG.md, decided with the user 24.11.2026). */
data class DayLagThresholds(
    /** A day counts as a day with complaints from this intensity on. */
    val minIntensity: Int = 2,
    /** Delays tested: same day up to this many days later. */
    val maxLagDays: Int = 3,
    /** Fewer days without it than this: "eaten almost daily", cannot be compared. */
    val minCleanDays: Int = 3,
    /** "Eaten almost daily" is only said after this many finished days ... */
    val minDaysForDaily: Int = 7,
    /** ... and for food on at least this share of them. */
    val dailyShare: Double = 0.8,
    /** Testing several delays finds chance results more easily, so a trace needs a clearer difference. */
    val traceMinLift: Double = 2.0,
)

/** Lifestyle candidates (TRD 2.5). */
data class LifestyleThresholds(
    val badSleepMaxQuality: Int = 2,
    val badSleepMinHours: Float = 6f,
    val highStressMin: Int = 4,
)
