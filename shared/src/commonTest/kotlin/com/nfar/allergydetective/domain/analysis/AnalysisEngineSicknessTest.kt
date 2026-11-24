package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.fixtures.DiaryBuilder
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BLOATING
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BREAD
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.MILK
import com.nfar.allergydetective.domain.analysis.model.AnalysisInput
import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.ExposureStats
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Self-test 19.11.: sick days count with a lower weight; per symptom the user says whether it
 * belongs to the illness.
 */
class AnalysisEngineSicknessTest {

    // Same-day comparison only, so the sick-day weights are checked on one fixed delay.
    private val engine = AnalysisEngine(AnalysisConfig(dayLag = DayLagThresholds(maxLagDays = 0)))

    /** Milk on every day, bloating 1.5 h later; days 0–2 are sick days with [relation]. */
    private fun diary(relation: IllnessRelation?, sickDays: Int = 3): AnalysisInput = DiaryBuilder().apply {
        for (i in 0 until 9) {
            day(i, sick = i < sickDays) {
                meal(8, BREAD)
                meal(15, MILK)
                symptom(16, 30, BLOATING, intensity = 4, relation = relation.takeIf { i < sickDays })
            }
        }
    }.build()

    private fun milkStats(input: AnalysisInput): ExposureStats =
        engine.analyze(input).resultFor(Candidate.Ingredient(MILK))!!.stats

    @Test
    fun mealsOnSickDaysCountWithHalfTheWeight() {
        val healthy = milkStats(diary(relation = null, sickDays = 0))
        val sick = milkStats(diary(relation = IllnessRelation.UNRELATED))

        assertEquals(9.0, healthy.n1)
        assertEquals(6.0 + 3 * 0.5, sick.n1)
        // Every meal is still counted once for the evidence thresholds.
        assertEquals(9, sick.exposureCount)
    }

    @Test
    fun complaintsOfTheIllnessDoNotCount() {
        val stats = milkStats(diary(IllnessRelation.RELATED))

        // Only the six healthy days have a hit: 6 × 4/5.
        assertEquals(6 * 0.8, stats.k1, 1e-9)
        assertEquals(6, stats.hitCount)
    }

    @Test
    fun unknownRelationCountsLessThanUnrelated() {
        val unrelated = milkStats(diary(IllnessRelation.UNRELATED))
        val unknown = milkStats(diary(IllnessRelation.UNKNOWN))
        val notAnswered = milkStats(diary(relation = null))

        // Sick day hit: 0.8 × 0.5 (day) for unrelated, × 0.5 again for unknown.
        assertEquals(6 * 0.8 + 3 * 0.8 * 0.5, unrelated.k1, 1e-9)
        assertEquals(6 * 0.8 + 3 * 0.8 * 0.25, unknown.k1, 1e-9)
        assertEquals(unknown.k1, notAnswered.k1, 1e-9)
        assertTrue(unknown.k1 < unrelated.k1)
    }

    @Test
    fun lifestyleCandidatesLeaveSickDaysOut() {
        val input = DiaryBuilder().apply {
            for (i in 0 until 6) day(i, sleepHours = 4f, sick = i < 2)
        }.build()

        val badSleep = engine.analyze(input).resultFor(Candidate.BadSleep)!!.stats

        assertEquals(4.0, badSleep.n1)
    }
}
