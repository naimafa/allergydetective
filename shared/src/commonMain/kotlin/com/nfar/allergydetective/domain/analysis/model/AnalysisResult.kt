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
 * [n0]/[k0] the same without it. The counts are unweighted, for the reason shown to the
 * user (UX 5: "4 of 5 times, otherwise 1 of 9").
 */
data class ExposureStats(
    val n1: Double,
    val k1: Double,
    val n0: Double,
    val k0: Double,
    val exposureCount: Int,
    val hitCount: Int,
    val unexposedCount: Int = 0,
    val unexposedHitCount: Int = 0,
)

data class CandidateResult(
    val candidate: Candidate,
    val stats: ExposureStats,
    val lift: Double,
    val level: EvidenceLevel,
    /** Food: typical delay in days (0 = same day), null when there is none (user 24.11.2026). */
    val lagDays: Int? = null,
    /** Food eaten almost every day: no clean days to compare with. */
    val tooOften: Boolean = false,
    /** Food: found as a fast reaction within 2 hours after the meal (user 24.11.2026). */
    val fast: Boolean = false,
)

/** A meal that contained an ingredient with a known allergy (TRD 2.6). */
data class KnownAllergyExposure(
    val mealId: Long,
    val ingredientId: Long,
    val propertyId: Long,
)

/**
 * Candidates that occurred together so often that the diary cannot tell them apart yet (TRD 2.7).
 * [members] are ordered like the ranking, so the first one is the strongest.
 */
data class CandidateGroup(val members: List<Candidate>)

data class AnalysisResult(
    /** All candidates with at least one logged exposure, strongest first. */
    val ranking: List<CandidateResult>,
    /** Indistinguishable candidates. Every member is also part of [ranking]. */
    val groups: List<CandidateGroup>,
    /** Ingredients that were tolerated well (TRD 2.9). */
    val safeIngredientIds: List<Long>,
    val knownAllergyExposures: List<KnownAllergyExposure>,
) {
    fun resultFor(candidate: Candidate): CandidateResult? = ranking.firstOrNull { it.candidate == candidate }
}
