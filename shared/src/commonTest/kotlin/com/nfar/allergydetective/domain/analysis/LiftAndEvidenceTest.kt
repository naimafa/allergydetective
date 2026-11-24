package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.EvidenceLevel
import com.nfar.allergydetective.domain.analysis.model.ExposureStats
import kotlin.test.Test
import kotlin.test.assertEquals

class LiftAndEvidenceTest {

    private val config = AnalysisConfig()

    private fun stats(n1: Double, k1: Double, n0: Double = 20.0, k0: Double = 0.0) =
        ExposureStats(n1 = n1, k1 = k1, n0 = n0, k0 = k0, exposureCount = n1.toInt(), hitCount = k1.toInt())

    @Test
    fun liftWithoutDataIsNeutral() {
        assertEquals(1.0, computeLift(stats(0.0, 0.0, 0.0, 0.0), config), 1e-9)
    }

    @Test
    fun liftUsesLaplaceSmoothing() {
        // ((3+1)/(4+2)) / ((1+1)/(8+2)) = 0.6667 / 0.2 = 3.333
        assertEquals(3.3333, computeLift(stats(n1 = 4.0, k1 = 3.0, n0 = 8.0, k0 = 1.0), config), 1e-3)
    }

    @Test
    fun singleEventDoesNotProduceExtremeLift() {
        // One meal, one hit, no baseline hits: (2/3) / (1/22) ≈ 14.7, but n1 < 3 keeps it at INSUFFICIENT_DATA
        val s = stats(n1 = 1.0, k1 = 1.0)
        assertEquals(EvidenceLevel.INSUFFICIENT_DATA, classifyEvidence(s, computeLift(s, config), false, config))
    }

    @Test
    fun summedHitWeightsReachThresholdDespiteRoundingErrors() {
        // 0.6 added ten times is 5.999999999999999 in floating point
        val k1 = (1..10).fold(0.0) { sum, _ -> sum + 0.6 }
        val s = ExposureStats(n1 = 10.0, k1 = k1 / 2, n0 = 20.0, k0 = 0.0, exposureCount = 10, hitCount = 5)
        assertEquals(EvidenceLevel.SUSPICION, classifyEvidence(s, lift = 2.0, wasSuspicionBefore = false, config = config))
    }

    @Test
    fun evidenceLevelsFollowTheThresholdTable() {
        fun level(n1: Double, k1: Double, lift: Double, stable: Boolean = false) =
            classifyEvidence(stats(n1, k1), lift, stable, config)

        assertEquals(EvidenceLevel.INSUFFICIENT_DATA, level(2.5, 2.0, 5.0))
        assertEquals(EvidenceLevel.NONE, level(5.0, 1.0, 1.4))
        assertEquals(EvidenceLevel.TRACE, level(3.0, 2.0, 1.5))
        // User 24.11.2026: no trace without real complaints, however high the smoothed lift.
        assertEquals(EvidenceLevel.NONE, level(5.0, 0.0, 3.0))
        assertEquals(EvidenceLevel.NONE, level(5.0, 1.0, 3.0))
        assertEquals(EvidenceLevel.TRACE, level(5.0, 2.9, 3.0)) // lift high, but k1 < 3
        assertEquals(EvidenceLevel.SUSPICION, level(5.0, 3.0, 2.0))
        assertEquals(EvidenceLevel.SUSPICION, level(8.0, 5.0, 3.0, stable = false)) // not yet stable
        assertEquals(EvidenceLevel.SUSPICION, level(5.0, 5.0, 3.0, stable = true)) // n1 < 6
        assertEquals(EvidenceLevel.STRONG_SUSPICION, level(6.0, 4.0, 2.5, stable = true))
    }
}
