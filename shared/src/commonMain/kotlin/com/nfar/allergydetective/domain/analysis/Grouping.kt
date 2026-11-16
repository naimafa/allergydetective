package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.CandidateGroup
import com.nfar.allergydetective.domain.analysis.model.CandidateResult
import com.nfar.allergydetective.domain.analysis.model.Meal

/**
 * Meal ids per food candidate, for the co-occurrence check (TRD 2.7).
 * Unlike the stats this ignores the reaction windows: candidates with different windows
 * (gluten 48h, fructans 12h) would otherwise look distinguishable at the end of the diary.
 */
internal fun foodExposureMeals(
    meals: List<Meal>,
    validDays: Set<Long>,
    now: Long,
    catalog: FoodCatalog,
): Map<Candidate, Set<Long>> {
    val exposures = mutableMapOf<Candidate, MutableSet<Long>>()
    for (meal in meals) {
        if (meal.eatenAt > now || localEpochDay(meal.eatenAt, meal.tzOffsetMin) !in validDays) continue
        for (item in meal.items) {
            exposures.getOrPut(Candidate.Ingredient(item.ingredientId)) { mutableSetOf() } += meal.id
            for (propertyId in catalog.propertiesByIngredient[item.ingredientId].orEmpty()) {
                exposures.getOrPut(Candidate.Property(propertyId)) { mutableSetOf() } += meal.id
            }
        }
    }
    return exposures
}

/**
 * Groups of candidates that the diary cannot tell apart yet (TRD 2.7).
 *
 * Two candidates are linked when each of them shares at least [GroupingThresholds.minSharedRatio]
 * of its exposures with the other. Linked candidates form one group, also over a chain (a~b, b~c).
 * Only groups with at least one member at [GroupingThresholds.minLevel] or above are returned.
 * Members and groups keep the order of [ranking].
 *
 * [exposures] holds meal ids for food candidates and epoch days for lifestyle candidates,
 * so food and lifestyle candidates are never compared with each other.
 */
internal fun indistinguishableGroups(
    ranking: List<CandidateResult>,
    exposures: Map<Candidate, Set<Long>>,
    thresholds: GroupingThresholds,
): List<CandidateGroup> {
    val nodes = ranking.filter { !exposures[it.candidate].isNullOrEmpty() }
    val groupOf = IntArray(nodes.size) { it }

    fun root(index: Int): Int {
        var i = index
        while (groupOf[i] != i) i = groupOf[i]
        return i
    }

    for (i in nodes.indices) {
        for (j in i + 1 until nodes.size) {
            val first = nodes[i].candidate
            val second = nodes[j].candidate
            if (isFood(first) != isFood(second)) continue
            if (occurTogether(exposures.getValue(first), exposures.getValue(second), thresholds.minSharedRatio)) {
                // Always attach the later root to the earlier one, so a root is the best-ranked member.
                val rootI = root(i)
                val rootJ = root(j)
                if (rootI < rootJ) groupOf[rootJ] = rootI else groupOf[rootI] = rootJ
            }
        }
    }

    return nodes.indices
        .groupBy { root(it) }
        .values
        .map { members -> members.map { nodes[it] } }
        .filter { members -> members.size > 1 && members.any { it.level >= thresholds.minLevel } }
        .map { members -> CandidateGroup(members.map { it.candidate }) }
}

private fun isFood(candidate: Candidate): Boolean =
    candidate is Candidate.Ingredient || candidate is Candidate.Property

/** True when the shared exposures are at least [minRatio] of both sets. */
private fun occurTogether(first: Set<Long>, second: Set<Long>, minRatio: Double): Boolean {
    val smaller = if (first.size <= second.size) first else second
    val larger = if (smaller === first) second else first
    // The shared part can never be larger than the smaller set.
    if (smaller.size.toDouble() / larger.size < minRatio) return false
    val shared = smaller.count { it in larger }
    return shared.toDouble() / larger.size >= minRatio
}
