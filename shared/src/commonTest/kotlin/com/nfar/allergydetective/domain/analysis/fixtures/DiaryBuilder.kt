package com.nfar.allergydetective.domain.analysis.fixtures

import com.nfar.allergydetective.domain.analysis.model.AnalysisInput
import com.nfar.allergydetective.domain.analysis.model.DayCheck
import com.nfar.allergydetective.domain.analysis.model.DayFactor
import com.nfar.allergydetective.domain.analysis.model.DiaryData
import com.nfar.allergydetective.domain.analysis.model.Meal
import com.nfar.allergydetective.domain.analysis.model.MealItem
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.analysis.model.ReferenceData
import com.nfar.allergydetective.domain.analysis.model.SymptomEntry

/**
 * Builds a synthetic diary day by day in local time.
 *
 * Day index 0 is [startEpochDay]. All times are local wall-clock times in [tzOffsetMin]
 * and converted to UTC epoch millis like the app does.
 */
class DiaryBuilder(
    private val startEpochDay: Long = 20_000L,
    private val tzOffsetMin: Int = 60,
) {
    private val meals = mutableListOf<Meal>()
    private val symptoms = mutableListOf<SymptomEntry>()
    private val dayChecks = mutableListOf<DayCheck>()
    private val dayFactors = mutableListOf<DayFactor>()
    private var nextId = 1L
    private var lastDayIndex = -1

    fun day(
        index: Int,
        confirmed: Boolean = true,
        sleepHours: Float? = null,
        sleepQuality: Int? = null,
        stress: Int? = null,
        factorIds: Set<Long> = emptySet(),
        block: DayScope.() -> Unit = {},
    ) {
        val epochDay = startEpochDay + index
        dayChecks += DayCheck(epochDay, confirmed, sleepHours, sleepQuality, stress)
        factorIds.forEach { dayFactors += DayFactor(epochDay, it) }
        DayScope(index).block()
        if (index > lastDayIndex) lastDayIndex = index
    }

    /** Same content for days 0 until [count]. */
    fun days(count: Int, confirmed: Boolean = true, block: DayScope.(index: Int) -> Unit) {
        for (i in 0 until count) day(i, confirmed) { block(i) }
    }

    inner class DayScope(private val index: Int) {
        fun meal(hour: Int, minute: Int = 0, vararg items: Pair<Long, Portion>) {
            meals += Meal(
                id = nextId++,
                eatenAt = utcMillis(index, hour, minute),
                tzOffsetMin = tzOffsetMin,
                items = items.map { (ingredientId, portion) -> MealItem(ingredientId, portion) },
            )
        }

        fun meal(hour: Int, vararg ingredientIds: Long) =
            meal(hour, 0, *ingredientIds.map { it to Portion.MEDIUM }.toTypedArray())

        fun symptom(hour: Int, minute: Int = 0, symptomId: Long, intensity: Int) {
            symptoms += SymptomEntry(
                id = nextId++,
                symptomId = symptomId,
                startedAt = utcMillis(index, hour, minute),
                tzOffsetMin = tzOffsetMin,
                intensity = intensity,
            )
        }
    }

    /** UTC epoch millis for a local time on day [dayIndex]. */
    fun utcMillis(dayIndex: Int, hour: Int, minute: Int = 0): Long {
        val localMinutes = (startEpochDay + dayIndex) * MINUTES_PER_DAY + hour * 60 + minute
        return (localMinutes - tzOffsetMin) * MILLIS_PER_MINUTE
    }

    /**
     * Default "now": noon, three days after the last logged day.
     * Long windows (gluten 48h) of the last days touch unconfirmed days and are not counted.
     */
    fun build(
        reference: ReferenceData = TestCatalog.reference(),
        now: Long = utcMillis(lastDayIndex + 3, 12),
    ) = AnalysisInput(
        reference = reference,
        diary = DiaryData(meals.toList(), symptoms.toList(), dayChecks.toList(), dayFactors.toList()),
        now = now,
        nowTzOffsetMin = tzOffsetMin,
    )

    private companion object {
        const val MINUTES_PER_DAY = 1_440L
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
