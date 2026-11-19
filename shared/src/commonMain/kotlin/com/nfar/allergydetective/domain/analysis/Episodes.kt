package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.SymptomEntry

/**
 * Several symptom entries of one group, merged (TRD 2.2).
 * [weight] lowers a complaint of a sick day whose relation to the illness is unknown (self-test 19.11.).
 */
internal data class Episode(
    val groupKey: String,
    val startedAt: Long,
    val tzOffsetMin: Int,
    val maxIntensity: Int,
    val weight: Double = 1.0,
) {
    val epochDay: Long get() = localEpochDay(startedAt, tzOffsetMin)
}

/**
 * Merges entries of the same symptom group that start within [mergeWindowMin] of the episode start.
 * Same symptom implies same group, so grouping by group covers both rules of TRD 2.2.
 * Entries with an unknown symptom or a [weightOf] of 0 are ignored; a merged episode keeps the
 * highest weight. Result is sorted by start time.
 */
internal fun buildEpisodes(
    entries: List<SymptomEntry>,
    groupBySymptomId: Map<Long, String>,
    mergeWindowMin: Int,
    weightOf: (SymptomEntry) -> Double = { 1.0 },
): List<Episode> {
    val mergeWindowMillis = mergeWindowMin * MILLIS_PER_MINUTE
    return entries
        .filter { weightOf(it) > 0.0 }
        .mapNotNull { entry -> groupBySymptomId[entry.symptomId]?.let { it to entry } }
        .groupBy({ it.first }, { it.second })
        .flatMap { (group, groupEntries) ->
            val episodes = mutableListOf<Episode>()
            for (entry in groupEntries.sortedBy { it.startedAt }) {
                val current = episodes.lastOrNull()
                if (current != null && entry.startedAt - current.startedAt <= mergeWindowMillis) {
                    episodes[episodes.lastIndex] = current.copy(
                        maxIntensity = maxOf(current.maxIntensity, entry.intensity),
                        weight = maxOf(current.weight, weightOf(entry)),
                    )
                } else {
                    episodes += Episode(group, entry.startedAt, entry.tzOffsetMin, entry.intensity, weightOf(entry))
                }
            }
            episodes
        }
        .sortedBy { it.startedAt }
}
