package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BLOATING
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.DIARRHEA
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.DIGESTIVE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.HEAD
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.HEADACHE
import com.nfar.allergydetective.domain.analysis.model.SymptomEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodeBuilderTest {

    private val groups = mapOf(BLOATING to DIGESTIVE, DIARRHEA to DIGESTIVE, HEADACHE to HEAD)

    private fun entry(minute: Long, symptomId: Long, intensity: Int) =
        SymptomEntry(id = minute, symptomId = symptomId, startedAt = minute * 60_000L, tzOffsetMin = 0, intensity = intensity)

    @Test
    fun sameGroupWithinThreeHoursBecomesOneEpisode() {
        val entries = listOf(entry(600, BLOATING, 2), entry(700, DIARRHEA, 4), entry(760, BLOATING, 3))

        val episodes = buildEpisodes(entries, groups, mergeWindowMin = 180)

        assertEquals(1, episodes.size)
        assertEquals(600 * 60_000L, episodes[0].startedAt)
        assertEquals(4, episodes[0].maxIntensity)
        assertEquals(DIGESTIVE, episodes[0].groupKey)
    }

    @Test
    fun entryAfterThreeHoursStartsNewEpisode() {
        val entries = listOf(entry(600, BLOATING, 2), entry(781, BLOATING, 3))

        val episodes = buildEpisodes(entries, groups, mergeWindowMin = 180)

        assertEquals(2, episodes.size)
    }

    @Test
    fun differentGroupsStaySeparate() {
        val entries = listOf(entry(600, BLOATING, 2), entry(610, HEADACHE, 3))

        val episodes = buildEpisodes(entries, groups, mergeWindowMin = 180)

        assertEquals(setOf(DIGESTIVE, HEAD), episodes.map { it.groupKey }.toSet())
    }

    @Test
    fun unsortedInputIsHandled() {
        val entries = listOf(entry(760, BLOATING, 3), entry(600, BLOATING, 2))

        val episodes = buildEpisodes(entries, groups, mergeWindowMin = 180)

        assertEquals(1, episodes.size)
        assertEquals(600 * 60_000L, episodes[0].startedAt)
    }

    @Test
    fun unknownSymptomIsIgnored() {
        val episodes = buildEpisodes(listOf(entry(600, 9999L, 3)), groups, mergeWindowMin = 180)

        assertEquals(0, episodes.size)
    }
}
