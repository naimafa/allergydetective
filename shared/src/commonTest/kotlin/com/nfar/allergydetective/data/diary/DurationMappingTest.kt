package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.domain.diary.SymptomDuration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DurationMappingTest {
    @Test
    fun everyDurationSurvivesTheDatabase() {
        SymptomDuration.entries.forEach { assertEquals(it, it.toEntity().toDomain()) }
    }

    @Test
    fun theFormOffersTheFinerStepsButNotTheOldOne() {
        // User 25.11.2026: 3–6 h and 6–12 h instead of 3–12 h.
        assertEquals(
            listOf(SymptomDuration.UNDER_1H, SymptomDuration.H1_TO_3, SymptomDuration.H3_TO_6, SymptomDuration.H6_TO_12, SymptomDuration.OVER_12H),
            SymptomDuration.offered,
        )
        assertFalse(SymptomDuration.H3_TO_12 in SymptomDuration.offered)
    }
}
