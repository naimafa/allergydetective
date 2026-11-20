package com.nfar.allergydetective.domain.report

import com.nfar.allergydetective.domain.diary.HistoryEntry
import com.nfar.allergydetective.domain.diary.SymptomDuration
import com.nfar.allergydetective.domain.time.AppClock
import com.nfar.allergydetective.domain.time.MealSlot

/*
 * Report for the doctor (feedback 20.11.): the diary of a period in numbers, so the
 * user can take it to an appointment. Only counts and the user's own notes; the
 * wording stays "observation", never "diagnosis" (PRD 1.1).
 */

/** How often a complaint was logged and how strong it was. */
data class SymptomStat(
    val symptomKey: String,
    val count: Int,
    val averageIntensity: Double,
    val maxIntensity: Int,
)

/** Complaints of one calendar week (Monday first), for the course of the intensity. */
data class WeekStat(
    /** Epoch day of the Monday. */
    val weekStart: Long,
    val count: Int,
    val averageIntensity: Double,
)

/** Part of the day a complaint started in, by local clock time. */
enum class ReportDayTime(val fromHour: Int) {
    NIGHT(0),
    MORNING(6),
    MIDDAY(11),
    AFTERNOON(14),
    EVENING(18),
    ;

    companion object {
        fun ofHour(hour: Int): ReportDayTime = entries.last { hour >= it.fromHour }
    }
}

/** A complaint with the user's note. */
data class ReportNote(
    val time: Long,
    val symptomKey: String,
    val intensity: Int,
    val note: String,
)

data class DoctorReport(
    val fromDay: Long,
    val toDay: Long,
    /** Days with at least one entry. */
    val documentedDays: Int,
    val mealCount: Int,
    val drinkCount: Int,
    val symptomCount: Int,
    /** Most frequent first. */
    val symptoms: List<SymptomStat>,
    /** Weeks with complaints, oldest first. */
    val weeks: List<WeekStat>,
    /** Number of complaints per part of the day, all parts present. */
    val dayTimes: Map<ReportDayTime, Int>,
    /** Number of complaints per given duration; complaints without a duration are not counted. */
    val durations: Map<SymptomDuration, Int>,
    /** Oldest first. */
    val notes: List<ReportNote>,
) {
    val days: Int get() = (toDay - fromDay + 1).toInt()
}

/** Builds the report for the local days [fromDay] to [toDay] (both included) from history entries. */
fun buildDoctorReport(entries: List<HistoryEntry>, fromDay: Long, toDay: Long, clock: AppClock): DoctorReport {
    val inRange = entries.filter { clock.epochDayOf(it.time) in fromDay..toDay }
    val meals = inRange.filterIsInstance<HistoryEntry.MealEntry>()
    val symptoms = inRange.filterIsInstance<HistoryEntry.SymptomEntry>().sortedBy { it.time }
    return DoctorReport(
        fromDay = fromDay,
        toDay = toDay,
        documentedDays = inRange.map { clock.epochDayOf(it.time) }.distinct().size,
        mealCount = meals.count { it.slot != MealSlot.DRINK },
        drinkCount = meals.count { it.slot == MealSlot.DRINK },
        symptomCount = symptoms.size,
        symptoms = symptoms.groupBy { it.symptomKey }
            .map { (key, list) -> SymptomStat(key, list.size, list.map { it.intensity }.average(), list.maxOf { it.intensity }) }
            .sortedWith(compareByDescending<SymptomStat> { it.count }.thenByDescending { it.averageIntensity }),
        weeks = symptoms.groupBy { weekStart(clock.epochDayOf(it.time)) }
            .map { (start, list) -> WeekStat(start, list.size, list.map { it.intensity }.average()) }
            .sortedBy { it.weekStart },
        dayTimes = ReportDayTime.entries.associateWith { part ->
            symptoms.count { ReportDayTime.ofHour(clock.localTime(it.time).first) == part }
        },
        durations = symptoms.mapNotNull { it.duration }.groupingBy { it }.eachCount(),
        notes = symptoms.mapNotNull { s -> s.note?.takeIf { it.isNotBlank() }?.let { ReportNote(s.time, s.symptomKey, s.intensity, it.trim()) } },
    )
}

/** Monday of the week of [epochDay]; 1970-01-01 was a Thursday. */
internal fun weekStart(epochDay: Long): Long = epochDay - ((epochDay + 3) % 7 + 7) % 7
