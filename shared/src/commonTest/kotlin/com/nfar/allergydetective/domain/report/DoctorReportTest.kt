package com.nfar.allergydetective.domain.report

import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.diary.SymptomDuration
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.time.MealSlot
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DoctorReportTest {

    private val clock = AppClock(clock = object : Clock { override fun now() = Instant.parse("2026-11-20T12:00:00Z") }, zone = { TimeZone.of("Europe/Berlin") })
    private val today = clock.today()
    private val coffee = IngredientInfo(1101, "coffee", null)

    private var nextId = 1L
    private fun symptom(daysAgo: Int, hour: Int, key: String, intensity: Int, note: String? = null, duration: SymptomDuration? = null) =
        HistoryEntry.SymptomEntry(nextId++, clock.millisAt(today - daysAgo, hour, 0), key, intensity, note, duration = duration)

    private fun meal(daysAgo: Int, hour: Int, slot: MealSlot) =
        HistoryEntry.MealEntry(nextId++, clock.millisAt(today - daysAgo, hour, 0), listOf(coffee to Portion.MEDIUM), slot = slot)

    @Test
    fun countsFrequencyIntensityTimesAndNotesOfThePeriod() {
        val entries = listOf(
            symptom(0, 9, "headache", 4, note = " nach Kaffee ", duration = SymptomDuration.H1_TO_3),
            symptom(1, 20, "headache", 2),
            symptom(2, 13, "bloating", 3, duration = SymptomDuration.H1_TO_3),
            symptom(20, 9, "headache", 5), // outside the 14 days
            meal(0, 8, MealSlot.BREAKFAST),
            meal(0, 11, MealSlot.DRINK),
            meal(3, 19, MealSlot.DINNER),
        )

        val report = buildDoctorReport(entries, today - 13, today, clock)

        assertEquals(14, report.days)
        assertEquals(4, report.documentedDays)
        assertEquals(2, report.mealCount)
        assertEquals(1, report.drinkCount)
        assertEquals(3, report.symptomCount)
        assertEquals(listOf(SymptomStat("headache", 2, 3.0, 4), SymptomStat("bloating", 1, 3.0, 3)), report.symptoms)
        assertEquals(1, report.dayTimes[ReportDayTime.MORNING])
        assertEquals(1, report.dayTimes[ReportDayTime.MIDDAY])
        assertEquals(1, report.dayTimes[ReportDayTime.EVENING])
        assertEquals(0, report.dayTimes[ReportDayTime.NIGHT])
        assertEquals(mapOf(SymptomDuration.H1_TO_3 to 2), report.durations)
        assertEquals(listOf("nach Kaffee"), report.notes.map { it.note })
    }

    @Test
    fun weeksStartOnMondayAndAreSortedOldestFirst() {
        // 20.11.2026 is a Friday: Monday of its week is the 16th, the week before starts on the 9th.
        val entries = listOf(symptom(0, 9, "headache", 4), symptom(4, 9, "headache", 2), symptom(5, 9, "headache", 3))

        val report = buildDoctorReport(entries, today - 13, today, clock)

        assertEquals(listOf(today - 11, today - 4), report.weeks.map { it.weekStart })
        assertEquals(listOf(1, 2), report.weeks.map { it.count })
        assertEquals(3.0, report.weeks.last().averageIntensity)
    }

    @Test
    fun partOfTheDayFollowsTheLocalHour() {
        assertEquals(ReportDayTime.NIGHT, ReportDayTime.ofHour(2))
        assertEquals(ReportDayTime.MORNING, ReportDayTime.ofHour(6))
        assertEquals(ReportDayTime.MIDDAY, ReportDayTime.ofHour(13))
        assertEquals(ReportDayTime.AFTERNOON, ReportDayTime.ofHour(17))
        assertEquals(ReportDayTime.EVENING, ReportDayTime.ofHour(23))
    }
}
