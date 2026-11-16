package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.CandidateGroup
import com.nfar.allergydetective.domain.analysis.model.CandidateResult
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.analysis.model.ExposureStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroupingTest {

    private val thresholds = GroupingThresholds()
    private val a = Candidate.Ingredient(1)
    private val b = Candidate.Ingredient(2)
    private val c = Candidate.Ingredient(3)

    private fun result(candidate: Candidate, level: EvidenceLevel = EvidenceLevel.SUSPICION) =
        CandidateResult(candidate, ExposureStats(0.0, 0.0, 0.0, 0.0, 0, 0), lift = 1.0, level = level)

    private fun ids(vararg ranges: IntRange): Set<Long> = ranges.flatMap { range -> range.map { it.toLong() } }.toSet()

    @Test
    fun candidatesSharingEightyPercentOfTheirExposuresAreGrouped() {
        val exposures = mapOf(a to ids(1..5), b to ids(1..4, 6..6))

        val groups = indistinguishableGroups(listOf(result(a), result(b)), exposures, thresholds)

        assertEquals(listOf(CandidateGroup(listOf(a, b))), groups)
    }

    @Test
    fun candidatesSharingLessThanEightyPercentAreNotGrouped() {
        val exposures = mapOf(a to ids(1..5), b to ids(1..3, 6..7))

        val groups = indistinguishableGroups(listOf(result(a), result(b)), exposures, thresholds)

        assertTrue(groups.isEmpty())
    }

    @Test
    fun candidateThatAlsoOccursAloneOftenIsDistinguishable() {
        // a is always eaten together with b, but b is eaten alone most of the time
        val exposures = mapOf(a to ids(1..4), b to ids(1..10))

        val groups = indistinguishableGroups(listOf(result(a), result(b)), exposures, thresholds)

        assertTrue(groups.isEmpty())
    }

    @Test
    fun groupNeedsAtLeastOneMemberWithATrace() {
        val exposures = mapOf(a to ids(1..5), b to ids(1..5))
        val ranking = listOf(result(a, EvidenceLevel.NONE), result(b, EvidenceLevel.NONE))

        assertTrue(indistinguishableGroups(ranking, exposures, thresholds).isEmpty())
    }

    @Test
    fun suspectIsGroupedWithItsUnsuspiciousCompanion() {
        val exposures = mapOf(a to ids(1..5), b to ids(1..5))
        val ranking = listOf(result(a, EvidenceLevel.TRACE), result(b, EvidenceLevel.NONE))

        assertEquals(listOf(CandidateGroup(listOf(a, b))), indistinguishableGroups(ranking, exposures, thresholds))
    }

    @Test
    fun foodAndLifestyleCandidatesAreNeverGrouped() {
        // meal ids and epoch days are different units, equal numbers mean nothing
        val exposures = mapOf(a to ids(1..5), Candidate.HighStress to ids(1..5))
        val ranking = listOf(result(a), result(Candidate.HighStress))

        assertTrue(indistinguishableGroups(ranking, exposures, thresholds).isEmpty())
    }

    @Test
    fun chainedCandidatesFormOneGroupInRankingOrder() {
        // a~b and b~c, but a and c share only 70 %
        val exposures = mapOf(
            a to ids(1..10),
            b to ids(1..9, 11..11),
            c to ids(1..7, 11..13),
        )

        val groups = indistinguishableGroups(listOf(result(c), result(a), result(b)), exposures, thresholds)

        assertEquals(listOf(CandidateGroup(listOf(c, a, b))), groups)
    }
}
