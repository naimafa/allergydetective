package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.fixtures.DiaryBuilder
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.HEADACHE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.RICE
import com.nfar.allergydetective.domain.analysis.model.Candidate
import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalysisEngineLifestyleTest {

    private val engine = AnalysisEngine()

    @Test
    fun highStressDaysWithHeadacheAreDetected() {
        val diary = DiaryBuilder().apply {
            for (i in 0 until 21) {
                val stressed = i % 3 == 0
                day(i, stress = if (stressed) 5 else 2) {
                    meal(12, RICE)
                    if (stressed) symptom(18, symptomId = HEADACHE, intensity = 3)
                }
            }
        }

        val result = engine.analyze(diary.build())

        assertTrue(result.resultFor(Candidate.HighStress)!!.level >= EvidenceLevel.SUSPICION)
        assertEquals(7, result.resultFor(Candidate.HighStress)!!.stats.exposureCount)
        assertEquals(14, result.resultFor(Candidate.HighStress)!!.stats.unexposedCount)
        assertEquals(0, result.resultFor(Candidate.HighStress)!!.stats.unexposedHitCount)
    }

    @Test
    fun badSleepCountsByQualityOrHours() {
        val diary = DiaryBuilder().apply {
            day(0, sleepQuality = 2, sleepHours = 8f)
            day(1, sleepQuality = 4, sleepHours = 5.5f)
            day(2, sleepQuality = 4, sleepHours = 7f)
        }

        val stats = engine.analyze(diary.build()).resultFor(Candidate.BadSleep)!!.stats

        assertEquals(2, stats.exposureCount)
        assertEquals(1.0, stats.n0)
    }

    @Test
    fun dayFactorIsACandidate() {
        val sport = 501L
        val diary = DiaryBuilder().apply {
            for (i in 0 until 12) {
                day(i, factorIds = if (i % 2 == 0) setOf(sport) else emptySet())
            }
        }

        val result = engine.analyze(diary.build()).resultFor(Candidate.DayFactor(sport))!!

        assertEquals(6, result.stats.exposureCount)
        assertEquals(EvidenceLevel.NONE, result.level)
    }
}
