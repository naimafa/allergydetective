package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.fixtures.DiaryBuilder
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BLOATING
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BREAD
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.GLUTEN
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.HEADACHE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.LACTOSE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.MILK
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.RICE
import com.nfar.allergydetective.domain.analysis.model.AnalysisResult
import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.CandidateResult
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.analysis.model.Portion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalysisEngineFoodTest {

    private val engine = AnalysisEngine()

    private fun AnalysisResult.level(candidate: Candidate): EvidenceLevel = resultFor(candidate)?.level ?: EvidenceLevel.INSUFFICIENT_DATA

    /** Bread every morning, rice at lunch, milk every third afternoon followed by bloating after 1.5h. */
    private fun lactoseDiary(days: Int, confirmed: Boolean = true) = DiaryBuilder().apply {
        days(days, confirmed) { i ->
            meal(8, BREAD)
            meal(12, RICE)
            if (i % 3 == 0) {
                meal(15, MILK)
                symptom(16, 30, BLOATING, intensity = 4)
            }
        }
    }

    @Test
    fun lactoseCausingBloatingAfterOneToThreeHoursIsDetected() {
        val result = engine.analyze(lactoseDiary(days = 21).build())

        assertTrue(result.level(Candidate.Property(LACTOSE)) >= EvidenceLevel.SUSPICION)
        assertTrue(result.level(Candidate.Ingredient(MILK)) >= EvidenceLevel.SUSPICION)
    }

    @Test
    fun dailyBreadIsNotSuspiciousWhenLactoseCausesTheComplaints() {
        val result = engine.analyze(lactoseDiary(days = 21).build())

        assertTrue(result.level(Candidate.Ingredient(BREAD)) < EvidenceLevel.TRACE)
        assertTrue(result.level(Candidate.Property(GLUTEN)) < EvidenceLevel.TRACE)
    }

    @Test
    fun dailyBreadWithoutComplaintsIsNotSuspiciousAndOnTheSafeList() {
        val diary = DiaryBuilder().apply {
            days(14) {
                meal(8, BREAD)
                meal(12, RICE)
            }
        }

        val result = engine.analyze(diary.build())

        val bread = result.resultFor(Candidate.Ingredient(BREAD))!!
        assertEquals(EvidenceLevel.NONE, bread.level)
        assertEquals(0.0, bread.stats.k1)
        assertTrue(BREAD in result.safeIngredientIds)
    }

    @Test
    fun ingredientWithHitsIsNotOnTheSafeList() {
        val result = engine.analyze(lactoseDiary(days = 21).build())

        assertFalse(MILK in result.safeIngredientIds)
    }

    @Test
    fun unconfirmedDaysAreIgnored() {
        val result = engine.analyze(lactoseDiary(days = 21, confirmed = false).build())

        assertEquals(EvidenceLevel.INSUFFICIENT_DATA, result.level(Candidate.Property(LACTOSE)))
        assertTrue(result.safeIngredientIds.isEmpty())
    }

    @Test
    fun mealIsNotCountedBeforeItsWindowHasPassed() {
        val diary = DiaryBuilder().apply {
            day(0) {
                meal(15, MILK)
                symptom(16, 30, BLOATING, intensity = 4)
            }
        }
        // 17:00 on the same day: the lactose window (up to 6h) is still open
        val result = engine.analyze(diary.build(now = diary.utcMillis(0, 17)))

        assertEquals(0, result.resultFor(Candidate.Property(LACTOSE))?.stats?.exposureCount ?: 0)
    }

    @Test
    fun implausibleSymptomGroupIsNoHit() {
        val diary = DiaryBuilder().apply {
            days(21) { i ->
                meal(8, BREAD)
                if (i % 3 == 0) {
                    meal(15, MILK)
                    symptom(16, 30, HEADACHE, intensity = 4) // head does not match lactose
                }
            }
        }

        val result = engine.analyze(diary.build())

        assertEquals(0.0, result.resultFor(Candidate.Property(LACTOSE))!!.stats.k1)
    }

    @Test
    fun largePortionCountsDouble() {
        val diary = DiaryBuilder().apply {
            days(3) { meal(15, 0, MILK to Portion.LARGE) }
        }

        val stats = engine.analyze(diary.build()).resultFor(Candidate.Ingredient(MILK))!!.stats

        assertEquals(6.0, stats.n1)
        assertEquals(3, stats.exposureCount)
    }

    @Test
    fun knownAllergyIsExcludedFromRankingAndReportedSeparately() {
        val input = lactoseDiary(days = 21).build(reference = TestCatalog.reference(knownAllergyPropertyIds = setOf(LACTOSE)))

        val result = engine.analyze(input)

        assertEquals(null, result.resultFor(Candidate.Property(LACTOSE)))
        assertEquals(null, result.resultFor(Candidate.Ingredient(MILK)))
        assertEquals(7, result.knownAllergyExposures.size)
        assertTrue(result.knownAllergyExposures.all { it.ingredientId == MILK && it.propertyId == LACTOSE })
    }

    @Test
    fun suspicionBecomesStrongOnlyAfterFourteenStableDays() {
        val short = engine.analyze(lactoseDiary(days = 21).build())
        val long = engine.analyze(lactoseDiary(days = 30).build())

        assertEquals(EvidenceLevel.SUSPICION, short.level(Candidate.Property(LACTOSE)))
        assertEquals(EvidenceLevel.STRONG_SUSPICION, long.level(Candidate.Property(LACTOSE)))
    }

    @Test
    fun unexposedMealsAreCountedForTheComparison() {
        val stats = engine.analyze(lactoseDiary(days = 21).build()).resultFor(Candidate.Property(LACTOSE))!!.stats

        // 21 × bread + 21 × rice without lactose; the bloating at 16:30 falls into the window of the 12:00 rice 7 times
        assertEquals(42, stats.unexposedCount)
        assertEquals(7, stats.unexposedHitCount)
    }

    @Test
    fun rankingIsSortedByLevelThenLift() {
        val ranking = engine.analyze(lactoseDiary(days = 21).build()).ranking

        val sorted = ranking.sortedWith(compareByDescending<CandidateResult> { it.level }.thenByDescending { it.lift })
        assertEquals(sorted, ranking)
    }
}
