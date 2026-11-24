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

/** Day analysis with a delay (KONZEPT_MUSTERERKENNUNG.md, user 24.11.2026). */
class DayLagTest {

    private val engine = AnalysisEngine()

    @Test
    fun complaintsOnTheNextDayAreFoundWithTheirDelay() {
        // Milk every fourth day, bloating the following afternoon.
        val diary = DiaryBuilder().apply {
            for (i in 0 until 24) {
                day(i) {
                    meal(8, BREAD)
                    if (i % 4 == 0) meal(19, MILK)
                    if (i % 4 == 1) symptom(15, 0, BLOATING, intensity = 4)
                }
            }
        }

        val milk = engine.analyze(diary.build()).resultFor(Candidate.Ingredient(MILK))!!

        assertEquals(1, milk.lagDays)
        assertTrue(milk.level >= EvidenceLevel.SUSPICION)
    }

    @Test
    fun complaintsTwoDaysLaterAreFoundToo() {
        val diary = DiaryBuilder().apply {
            for (i in 0 until 24) {
                day(i) {
                    meal(8, BREAD)
                    if (i % 4 == 0) meal(12, MILK)
                    if (i % 4 == 2) symptom(10, 0, BLOATING, intensity = 3)
                }
            }
        }

        val milk = engine.analyze(diary.build()).resultFor(Candidate.Ingredient(MILK))!!

        assertEquals(2, milk.lagDays)
        assertTrue(milk.level >= EvidenceLevel.TRACE)
    }

    @Test
    fun aMildComplaintOfOneDoesNotCount() {
        val diary = DiaryBuilder().apply {
            for (i in 0 until 24) {
                day(i) {
                    meal(8, BREAD)
                    if (i % 4 == 0) {
                        meal(12, MILK)
                        symptom(14, 0, BLOATING, intensity = 1)
                    }
                }
            }
        }

        val milk = engine.analyze(diary.build()).resultFor(Candidate.Ingredient(MILK))!!

        assertEquals(0, milk.stats.hitCount)
        assertEquals(null, milk.lagDays)
    }

    @Test
    fun anIngredientWithoutTriggerIsAnalysedToo() {
        val diary = DiaryBuilder().apply {
            for (i in 0 until 24) {
                day(i) {
                    meal(8, BREAD)
                    if (i % 4 == 0) {
                        meal(12, RICE)
                        symptom(14, 0, BLOATING, intensity = 4)
                    }
                }
            }
        }

        val rice = engine.analyze(diary.build()).resultFor(Candidate.Ingredient(RICE))

        assertTrue(rice != null && rice.level >= EvidenceLevel.SUSPICION)
    }
}
