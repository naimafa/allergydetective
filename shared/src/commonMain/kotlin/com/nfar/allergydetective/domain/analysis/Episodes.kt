package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.SymptomEntry

/** Several symptom entries of one group, merged (TRD 2.2). */
internal data class Episode(
    val groupKey: String,
    val startedAt: Long,
    val tzOffsetMin: Int,
    val maxIntensity: Int,
) {
    val epochDay: Long get() = localEpochDay(startedAt, tzOffsetMin)
}

/**
 * Merges entries of the same symptom group that start within [mergeWindowMin] of the episode start.
 * Same symptom implies same group, so grouping by group covers both rules of TRD 2.2.
 * Entries with an unknown symptom are ignored. Result is sorted by start time.
 */
internal fun buildEpisodes(
    entries: List<SymptomEntry>,
    groupBySymptomId: Map<Long, String>,
    mergeWindowMin: Int,
): List<Episode> {
    val mergeWindowMillis = mergeWindowMin * MILLIS_PER_MINUTE
    return entries
        .mapNotNull { entry -> groupBySymptomId[entry.symptomId]?.let { it to entry } }
        .groupBy({ it.first }, { it.second })
        .flatMap { (group, groupEntries) ->
            val episodes = mutableListOf<Episode>()
            for (entry in groupEntries.sortedBy { it.startedAt }) {
                val current = episodes.lastOrNull()
                if (current != null && entry.startedAt - current.startedAt <= mergeWindowMillis) {
                    episodes[episodes.lastIndex] = current.copy(maxIntensity = maxOf(current.maxIntensity, entry.intensity))
                } else {
                    episodes += Episode(group, entry.startedAt, entry.tzOffsetMin, entry.intensity)
                }
            }
            episodes
        }
        .sortedBy { it.startedAt }
}
