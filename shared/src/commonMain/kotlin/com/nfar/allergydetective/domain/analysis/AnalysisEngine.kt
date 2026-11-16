package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.AnalysisInput
import com.nfar.allergydetective.domain.analysis.model.AnalysisResult
import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.CandidateResult
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.analysis.model.ExposureStats
import com.nfar.allergydetective.domain.analysis.model.KnownAllergyExposure

/**
 * The "detective logic" (TRD 2) as a pure function: valid days → episodes → hits per window
 * → lift → evidence level → safe list. No database access, no side effects.
 */
class AnalysisEngine(private val config: AnalysisConfig = AnalysisConfig()) {

    fun analyze(input: AnalysisInput): AnalysisResult {
        // Strong suspicion needs a suspicion that already existed some days ago (TRD 2.8).
        // We get it by running the same analysis with an earlier "now".
        val stableSince = input.now - config.evidence.strongStableDays * MILLIS_PER_DAY
        val earlierSuspicions = evaluate(input.copy(now = stableSince), emptySet())
            .filter { it.level >= EvidenceLevel.SUSPICION }
            .map { it.candidate }
            .toSet()

        val foodStats = foodStatsByCandidate(input)
        val ranking = evaluate(input, earlierSuspicions, foodStats)
        return AnalysisResult(
            ranking = ranking,
            groups = indistinguishableGroups(ranking, exposures(input), config.grouping),
            safeIngredientIds = safeIngredients(foodStats),
            knownAllergyExposures = knownAllergyExposures(input),
        )
    }

    private fun evaluate(
        input: AnalysisInput,
        earlierSuspicions: Set<Candidate>,
        foodStats: Map<Candidate, ExposureStats> = foodStatsByCandidate(input),
    ): List<CandidateResult> {
        val all = foodStats + lifestyleStats(
            diary = input.diary,
            episodes = episodes(input),
            validDays = validDays(input),
            today = localEpochDay(input.now, input.nowTzOffsetMin),
            config = config,
        )
        return all
            .map { (candidate, stats) ->
                val lift = computeLift(stats, config)
                CandidateResult(candidate, stats, lift, classifyEvidence(stats, lift, candidate in earlierSuspicions, config))
            }
            .sortedWith(compareByDescending<CandidateResult> { it.level }.thenByDescending { it.lift })
    }

    /** Stats for every food candidate that occurs in at least one meal. */
    private fun foodStatsByCandidate(input: AnalysisInput): Map<Candidate, ExposureStats> {
        val catalog = FoodCatalog(input.reference)
        val meals = input.diary.meals
        val episodes = episodes(input)
        val validDays = validDays(input)
        return catalog.profiles()
            .filter { profile -> meals.any { catalog.exposurePortion(it, profile.candidate, config) != null } }
            .associate { profile ->
                profile.candidate to foodStats(profile, meals, episodes, validDays, input.now, catalog, config)
            }
    }

    /** Exposures of all candidates for the grouping (TRD 2.7): meal ids for food, days for lifestyle. */
    private fun exposures(input: AnalysisInput): Map<Candidate, Set<Long>> {
        val validDays = validDays(input)
        val today = localEpochDay(input.now, input.nowTzOffsetMin)
        return foodExposureMeals(input.diary.meals, validDays, input.now, FoodCatalog(input.reference)) +
            lifestyleExposureDays(input.diary, validDays, today, config)
    }

    /** Only days confirmed as NO_SYMPTOMS or COMPLETE (TRD 2.2). */
    private fun validDays(input: AnalysisInput): Set<Long> =
        input.diary.dayChecks.filter { it.confirmed }.map { it.epochDay }.toSet()

    /** Episodes that started before "now" on valid days. */
    private fun episodes(input: AnalysisInput): List<Episode> {
        val groups = input.reference.symptoms.associate { it.id to it.groupKey }
        val validDays = validDays(input)
        val entries = input.diary.symptomEntries.filter { it.startedAt <= input.now }
        return buildEpisodes(entries, groups, config.episodeMergeWindowMin).filter { it.epochDay in validDays }
    }

    /** Ingredients eaten often enough without any plausible complaint (TRD 2.9). */
    private fun safeIngredients(foodStats: Map<Candidate, ExposureStats>): List<Long> =
        foodStats
            .filter { (candidate, stats) ->
                candidate is Candidate.Ingredient &&
                    stats.exposureCount >= config.safeList.minExposures &&
                    stats.hitCount == 0
            }
            .keys
            .map { (it as Candidate.Ingredient).id }
            .sorted()

    private fun knownAllergyExposures(input: AnalysisInput): List<KnownAllergyExposure> {
        val known = input.reference.knownAllergyPropertyIds
        if (known.isEmpty()) return emptyList()
        val catalog = FoodCatalog(input.reference)
        return input.diary.meals.flatMap { meal ->
            meal.items.flatMap { item ->
                catalog.propertiesByIngredient[item.ingredientId].orEmpty()
                    .filter { it in known }
                    .map { KnownAllergyExposure(meal.id, item.ingredientId, it) }
            }
        }
    }
}
