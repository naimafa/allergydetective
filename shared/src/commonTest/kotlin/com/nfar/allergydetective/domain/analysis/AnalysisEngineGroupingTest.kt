package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.fixtures.DiaryBuilder
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BLOATING
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.BREAD
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.HARD_CHEESE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.HEADACHE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.LACTOSE
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.MILK
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.MILK_PROTEIN
import com.nfar.allergydetective.domain.analysis.fixtures.TestCatalog.RICE
import com.nfar.allergydetective.domain.analysis.model.AnalysisResult
import com.nfar.allergydetective.domain.analysis.model.Candidate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalysisEngineGroupingTest {

    private val engine = AnalysisEngine()

    private fun AnalysisResult.groupSets(): List<Set<Candidate>> = groups.map { it.members.toSet() }

    @Test
    fun lactoseAndMilkProteinAreIndistinguishableWhenMilkIsTheOnlySource() {
        val diary = DiaryBuilder().apply {
            days(21) { i ->
                meal(8, BREAD)
                meal(12, RICE)
                if (i % 3 == 0) {
                    meal(15, MILK)
                    symptom(16, 30, BLOATING, intensity = 4)
                }
            }
        }

        val result = engine.analyze(diary.build())

        val milkGroup = setOf(Candidate.Property(LACTOSE), Candidate.Property(MILK_PROTEIN), Candidate.Ingredient(MILK))
        assertEquals(listOf<Set<Candidate>>(milkGroup), result.groupSets())
    }

    @Test
    fun toleratedHardCheeseSeparatesMilkProteinFromLactose() {
        val diary = DiaryBuilder().apply {
            days(21) { i ->
                meal(8, BREAD)
                meal(12, RICE)
                if (i % 3 == 0) {
                    meal(15, MILK)
                    symptom(16, 30, BLOATING, intensity = 4)
                }
                if (i % 3 == 1) meal(15, HARD_CHEESE) // milk protein without lactose, no complaints
            }
        }

        val result = engine.analyze(diary.build())

        val lactoseGroup = setOf(Candidate.Property(LACTOSE), Candidate.Ingredient(MILK))
        assertEquals(listOf<Set<Candidate>>(lactoseGroup), result.groupSets())
    }

    @Test
    fun unsuspiciousFoodsEatenTogetherAreNotReportedAsGroup() {
        val diary = DiaryBuilder().apply {
            days(14) {
                meal(8, BREAD)
                meal(12, RICE)
            }
        }

        assertTrue(engine.analyze(diary.build()).groups.isEmpty())
    }

    @Test
    fun badSleepAndHighStressOnTheSameDaysAreIndistinguishable() {
        val diary = DiaryBuilder().apply {
            for (i in 0 until 21) {
                val badDay = i % 3 == 0
                day(i, stress = if (badDay) 5 else 2, sleepQuality = if (badDay) 1 else 4) {
                    meal(12, RICE)
                    if (badDay) symptom(18, symptomId = HEADACHE, intensity = 3)
                }
            }
        }

        val result = engine.analyze(diary.build())

        assertEquals(listOf<Set<Candidate>>(setOf(Candidate.BadSleep, Candidate.HighStress)), result.groupSets())
    }
}
