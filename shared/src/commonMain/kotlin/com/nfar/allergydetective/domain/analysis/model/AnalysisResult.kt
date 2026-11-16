package com.nfar.allergydetective.domain.analysis.model

/** Something that can be ranked: a food candidate or a lifestyle candidate (TRD 2.4, 2.5). */
sealed interface Candidate {
    data class Ingredient(val id: Long) : Candidate
    data class Property(val id: Long) : Candidate
    data object BadSleep : Candidate
    data object HighStress : Candidate
    data class DayFactor(val id: Long) : Candidate
}

/** Ordered from weakest to strongest, so levels can be compared with `<` and `>=`. */
enum class EvidenceLevel { INSUFFICIENT_DATA, NONE, TRACE, SUSPICION, STRONG_SUSPICION }

/**
 * Counts for one candidate (TRD 2.4). Names follow the formula:
 * [n1]/[k1] exposures and hit weight with the candidate (weighted by portion),
 * [n0]/[k0] the same without it. [exposureCount] and [hitCount] are unweighted.
 */
data class ExposureStats(
    val n1: Double,
    val k1: Double,
    val n0: Double,
    val k0: Double,
    val exposureCount: Int,
    val hitCount: Int,
)

data class CandidateResult(
    val candidate: Candidate,
    val stats: ExposureStats,
    val lift: Double,
    val level: EvidenceLevel,
)

/** A meal that contained an ingredient with a known allergy (TRD 2.6). */
data class KnownAllergyExposure(
    val mealId: Long,
    val ingredientId: Long,
    val propertyId: Long,
)

data class AnalysisResult(
    /** All candidates with at least one logged exposure, strongest first. */
    val ranking: List<CandidateResult>,
    /** Ingredients that were tolerated well (TRD 2.9). */
    val safeIngredientIds: List<Long>,
    val knownAllergyExposures: List<KnownAllergyExposure>,
) {
    fun resultFor(candidate: Candidate): CandidateResult? = ranking.firstOrNull { it.candidate == candidate }
}
