package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.ExposureStats
import com.nfar.allergydetective.domain.analysis.model.Meal
import com.nfar.allergydetective.domain.analysis.model.ReferenceData
import com.nfar.allergydetective.domain.analysis.model.TimeWindow

/** Windows and plausible symptom groups of a food candidate (TRD 2.3). */
internal data class FoodProfile(
    val candidate: Candidate,
    val windows: List<TimeWindow>,
    val symptomGroups: Set<String>,
) {
    val maxEndMin: Int = windows.maxOf { it.endMin }
}

/** Lookup tables built once per analysis. */
internal class FoodCatalog(reference: ReferenceData) {
    val propertiesByIngredient: Map<Long, Set<Long>> = reference.ingredients.associate { it.id to it.propertyIds }
    private val propertiesById = reference.properties.associateBy { it.id }
    private val known = reference.knownAllergyPropertyIds

    fun hasKnownAllergy(ingredientId: Long): Boolean =
        propertiesByIngredient[ingredientId].orEmpty().any { it in known }

    /**
     * Profiles of all properties and ingredients except known allergies (TRD 2.6).
     * An ingredient uses the union of the windows and groups of its properties;
     * ingredients without any property have no window and are not analysed.
     */
    fun profiles(): List<FoodProfile> {
        val propertyProfiles = propertiesById.values
            .filter { it.id !in known && it.windows.isNotEmpty() }
            .map { FoodProfile(Candidate.Property(it.id), it.windows, it.symptomGroups) }
        val ingredientProfiles = propertiesByIngredient
            .filterKeys { !hasKnownAllergy(it) }
            .mapNotNull { (ingredientId, propertyIds) ->
                val properties = propertyIds.mapNotNull { propertiesById[it] }
                val windows = properties.flatMap { it.windows }.distinct()
                if (windows.isEmpty()) return@mapNotNull null
                FoodProfile(Candidate.Ingredient(ingredientId), windows, properties.flatMap { it.symptomGroups }.toSet())
            }
        return propertyProfiles + ingredientProfiles
    }

    /** Largest portion factor of the candidate in this meal, or null if the meal does not contain it. */
    fun exposurePortion(meal: Meal, candidate: Candidate, config: AnalysisConfig): Double? {
        val items = when (candidate) {
            is Candidate.Ingredient -> meal.items.filter { it.ingredientId == candidate.id }
            is Candidate.Property -> meal.items.filter { candidate.id in propertiesByIngredient[it.ingredientId].orEmpty() }
            else -> emptyList()
        }
        return items.maxOfOrNull { config.portionFactor(it.portion) }
    }
}

/**
 * Counts n1/k1 (meals with the candidate) and n0/k0 (meals without), TRD 2.3–2.4.
 * A meal only counts when the candidate's window has fully passed and all touched days are valid.
 */
internal fun foodStats(
    profile: FoodProfile,
    meals: List<Meal>,
    episodes: List<Episode>,
    validDays: Set<Long>,
    now: Long,
    catalog: FoodCatalog,
    config: AnalysisConfig,
): ExposureStats {
    var n1 = 0.0
    var k1 = 0.0
    var n0 = 0.0
    var k0 = 0.0
    var exposureCount = 0
    var hitCount = 0
    for (meal in meals) {
        if (!isWindowClosed(meal, profile, validDays, now)) continue
        val weight = hitWeight(meal, profile, episodes, config)
        val portion = catalog.exposurePortion(meal, profile.candidate, config)
        if (portion != null) {
            n1 += portion
            k1 += weight * portion
            exposureCount++
            if (weight > 0.0) hitCount++
        } else {
            n0 += 1.0
            k0 += weight
        }
    }
    return ExposureStats(n1, k1, n0, k0, exposureCount, hitCount)
}

private fun isWindowClosed(meal: Meal, profile: FoodProfile, validDays: Set<Long>, now: Long): Boolean {
    val windowEnd = meal.eatenAt + profile.maxEndMin * MILLIS_PER_MINUTE
    if (windowEnd > now) return false
    val firstDay = localEpochDay(meal.eatenAt, meal.tzOffsetMin)
    val lastDay = localEpochDay(windowEnd, meal.tzOffsetMin)
    return (firstDay..lastDay).all { it in validDays }
}

/** Highest weight (intensity / max) of a plausible episode starting in one of the windows, 0 if none. */
private fun hitWeight(meal: Meal, profile: FoodProfile, episodes: List<Episode>, config: AnalysisConfig): Double {
    var maxIntensity = 0
    for (window in profile.windows) {
        val from = meal.eatenAt + window.startMin * MILLIS_PER_MINUTE
        val to = meal.eatenAt + window.endMin * MILLIS_PER_MINUTE
        var i = firstIndexAtOrAfter(episodes, from)
        while (i < episodes.size && episodes[i].startedAt <= to) {
            val episode = episodes[i]
            if (episode.groupKey in profile.symptomGroups) maxIntensity = maxOf(maxIntensity, episode.maxIntensity)
            i++
        }
    }
    return maxIntensity.toDouble() / config.maxIntensity
}

/** Binary search in episodes sorted by start time. */
private fun firstIndexAtOrAfter(episodes: List<Episode>, time: Long): Int {
    var low = 0
    var high = episodes.size
    while (low < high) {
        val mid = (low + high) ushr 1
        if (episodes[mid].startedAt < time) low = mid + 1 else high = mid
    }
    return low
}
