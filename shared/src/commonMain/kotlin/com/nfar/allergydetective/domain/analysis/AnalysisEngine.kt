package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.AnalysisInput
import com.nfar.allergydetective.domain.analysis.model.AnalysisResult
import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.CandidateResult
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.analysis.model.ExposureStats
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import com.nfar.allergydetective.domain.analysis.model.KnownAllergyExposure
import com.nfar.allergydetective.domain.analysis.model.TimeWindow

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
            safeIngredientIds = safeIngredients(foodStats.mapValues { it.value.stats }),
            knownAllergyExposures = knownAllergyExposures(input),
        )
    }

    private fun evaluate(
        input: AnalysisInput,
        earlierSuspicions: Set<Candidate>,
        foodResults: Map<Candidate, DayLagResult> = foodStatsByCandidate(input),
    ): List<CandidateResult> {
        val lifestyle = lifestyleStats(
            diary = input.diary,
            episodes = episodes(input),
            // An illness changes sleep and stress, too: lifestyle candidates leave sick days out.
            validDays = validDays(input) - sickDays(input),
            today = localEpochDay(input.now, input.nowTzOffsetMin),
            config = config,
        )
        // Several delays are tested for food, so a trace needs a clearer difference (KONZEPT_MUSTERERKENNUNG.md).
        val foodConfig = config.copy(evidence = config.evidence.copy(traceMinLift = maxOf(config.evidence.traceMinLift, config.dayLag.traceMinLift)))
        val food = foodResults.map { (candidate, result) ->
            val lift = computeLift(result.stats, foodConfig)
            val level = if (result.tooOften) EvidenceLevel.INSUFFICIENT_DATA else classifyEvidence(result.stats, lift, candidate in earlierSuspicions, foodConfig)
            CandidateResult(candidate, result.stats, lift, level, result.lagDays, result.tooOften, result.fast)
        }
        val life = lifestyle.map { (candidate, stats) ->
            val lift = computeLift(stats, config)
            CandidateResult(candidate, stats, lift, classifyEvidence(stats, lift, candidate in earlierSuspicions, config))
        }
        return (food + life).sortedWith(compareByDescending<CandidateResult> { it.level }.thenByDescending { it.lift })
    }

    /**
     * Day analysis with a delay for every food candidate eaten at least once (user 24.11.2026): all trigger properties
     * and every ingredient, also those without a trigger. Known allergies are left out (TRD 2.6).
     */
    private fun foodStatsByCandidate(input: AnalysisInput): Map<Candidate, DayLagResult> {
        val catalog = FoodCatalog(input.reference)
        val validDays = validDays(input).filter { it < localEpochDay(input.now, input.nowTzOffsetMin) }.toSet()
        val sickDays = sickDays(input)
        val complaints = complaintDays(input.diary, validDays, sickDays, config)
        val known = input.reference.knownAllergyPropertyIds
        val eaten = input.diary.meals.flatMap { meal -> meal.items.map { it.ingredientId } }.toSet()
        val candidates = input.reference.properties.filter { it.id !in known }.map { Candidate.Property(it.id) } +
            eaten.filter { !catalog.hasKnownAllergy(it) }.map { Candidate.Ingredient(it) }
        // Fast reactions (user 24.11.2026): within 2 hours after a meal, checked per meal - a day would hide them.
        val fastEpisodes = episodes(input).filter { it.maxIntensity >= config.dayLag.minIntensity }
        val allGroups = input.reference.symptoms.map { it.groupKey }.toSet()
        val fastWindow = listOf(TimeWindow(0, config.dayLag.fastWindowMin))
        val foodConfig = config.copy(evidence = config.evidence.copy(traceMinLift = maxOf(config.evidence.traceMinLift, config.dayLag.traceMinLift)))
        return candidates.mapNotNull { candidate ->
            val exposure = exposureDays(input.diary, candidate, catalog, config).filterKeys { it in validDays }
            if (exposure.isEmpty()) return@mapNotNull null
            val byDay = dayLagStats(exposure, complaints, validDays, sickDays, config)
            val fastStats = foodStats(FoodProfile(candidate, fastWindow, allGroups), input.diary.meals, fastEpisodes, validDays, input.now, catalog, config, sickDays)
            val dayLevel = classifyEvidence(byDay.stats, computeLift(byDay.stats, foodConfig), false, foodConfig)
            val fastLevel = classifyEvidence(fastStats, computeLift(fastStats, foodConfig), false, foodConfig)
            // The clearer of both; on a tie the day view, which also covers later complaints.
            candidate to if (fastLevel > dayLevel && fastLevel >= EvidenceLevel.TRACE) {
                DayLagResult(fastStats, lagDays = 0, tooOften = false, fast = true)
            } else {
                byDay
            }
        }.toMap()
    }

    /** Exposures of all candidates for the grouping (TRD 2.7): meal ids for food, days for lifestyle. */
    private fun exposures(input: AnalysisInput): Map<Candidate, Set<Long>> {
        val validDays = validDays(input)
        val today = localEpochDay(input.now, input.nowTzOffsetMin)
        return foodExposureMeals(input.diary.meals, validDays, input.now, FoodCatalog(input.reference)) +
            lifestyleExposureDays(input.diary, validDays - sickDays(input), today, config)
    }

    /** Only days confirmed as NO_SYMPTOMS or COMPLETE (TRD 2.2). */
    private fun validDays(input: AnalysisInput): Set<Long> =
        input.diary.dayChecks.filter { it.confirmed }.map { it.epochDay }.toSet()

    /** Confirmed days in a period of illness or marked as sick (self-test 19.11.: they count with a lower weight). */
    private fun sickDays(input: AnalysisInput): Set<Long> =
        input.diary.dayChecks.filter { it.confirmed && it.sick }.map { it.epochDay }.toSet()

    /**
     * Episodes that started before "now" on valid days. On a sick day a complaint that belongs to
     * the illness is left out, one with an unknown relation counts less.
     */
    private fun episodes(input: AnalysisInput): List<Episode> {
        val groups = input.reference.symptoms.associate { it.id to it.groupKey }
        val validDays = validDays(input)
        val sickDays = sickDays(input)
        val entries = input.diary.symptomEntries.filter { it.startedAt <= input.now }
        return buildEpisodes(entries, groups, config.episodeMergeWindowMin) { entry ->
            if (localEpochDay(entry.startedAt, entry.tzOffsetMin) !in sickDays) {
                1.0
            } else {
                when (entry.illnessRelation) {
                    IllnessRelation.RELATED -> 0.0
                    IllnessRelation.UNRELATED -> 1.0
                    IllnessRelation.UNKNOWN, null -> config.sickness.unknownRelationWeight
                }
            }
        }.filter { it.epochDay in validDays }
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
                catalog.propertiesOf(item)
                    .filter { it in known }
                    .map { KnownAllergyExposure(meal.id, item.ingredientId, it) }
            }
        }
    }
}
