package com.nfar.allergydetective.ui.results

import com.nfar.allergydetective.domain.analysis.model.DayCheck
import com.nfar.allergydetective.domain.analysis.model.DiaryData
import com.nfar.allergydetective.domain.analysis.model.SymptomEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class NoPatternReasonTest {
    private val day = 86_400_000L

    private fun diary(finishedDays: Int, complaintDays: List<Int>) = DiaryData(
        meals = emptyList(),
        symptomEntries = complaintDays.mapIndexed { i, d -> SymptomEntry(id = i.toLong(), symptomId = 1, startedAt = d * day + 12 * 3_600_000L, tzOffsetMin = 0, intensity = 3) },
        dayChecks = (0 until finishedDays).map { DayCheck(epochDay = it.toLong(), confirmed = true, sleepHours = null, sleepQuality = null, stress = null) },
        dayFactors = emptyList(),
    )

    @Test
    fun tooFewFinishedDays() = assertEquals(NoPatternReason.TOO_FEW_DAYS, noPatternReason(diary(6, listOf(1, 2)), 7))

    @Test
    fun noComplaints() = assertEquals(NoPatternReason.NO_COMPLAINTS, noPatternReason(diary(7, emptyList()), 7))

    @Test
    fun complaintsOnAnOpenDayDoNotCount() = assertEquals(NoPatternReason.NO_COMPLAINTS, noPatternReason(diary(7, listOf(9)), 7))

    @Test
    fun veryLightComplaintsDoNotCount() {
        val light = diary(7, listOf(2)).let { d -> d.copy(symptomEntries = d.symptomEntries.map { it.copy(intensity = 1) }) }
        assertEquals(NoPatternReason.NO_COMPLAINTS, noPatternReason(light, 7))
    }

    @Test
    fun complaintsOnOneDayOnly() = assertEquals(NoPatternReason.FEW_COMPLAINT_DAYS, noPatternReason(diary(7, listOf(3, 3)), 7))

    @Test
    fun complaintsEveryDay() = assertEquals(NoPatternReason.NO_GOOD_DAYS, noPatternReason(diary(7, (0 until 7).toList()), 7))

    @Test
    fun enoughOfBothKinds() = assertEquals(NoPatternReason.NOTHING_STANDS_OUT, noPatternReason(diary(7, listOf(1, 4)), 7))
}
