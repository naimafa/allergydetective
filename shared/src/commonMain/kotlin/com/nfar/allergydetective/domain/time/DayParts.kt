package com.nfar.allergydetective.domain.time

/**
 * Meals and symptoms are logged by part of the day instead of clock time (self-test 18.11.).
 * The analysis needs a timestamp, so every part stands for a fixed local time.
 * The times are chosen so that a symptom "morning" lies before breakfast and
 * "midday" / "evening" lie a little after lunch / dinner (ENTSCHEIDUNGEN, 18.11.2026).
 */
enum class MealSlot(val hour: Int, val minute: Int) {
    BREAKFAST(8, 0),
    LUNCH(12, 30),
    DINNER(19, 0),

    /** Snacks keep their clock time: "now" unless changed in the time dialog. */
    SNACK(-1, -1),
}

/** When a symptom started, as the user remembers it. */
enum class SymptomDayPart(val hour: Int, val minute: Int) {
    NOW(-1, -1),
    MORNING(7, 0),
    MIDDAY(13, 0),
    AFTERNOON(16, 0),
    EVENING(20, 0),

    /** All day long: counted from the morning on. */
    ALL_DAY(7, 0),

    /** Shortly after the last logged meal ([AFTER_MEAL_MINUTES]). */
    AFTER_MEAL(-1, -1),
    ;

    companion object {
        const val AFTER_MEAL_MINUTES = 30
    }
}

/** Today's timestamp of [slot], never in the future; a snack is "now". */
fun AppClock.timeOf(slot: MealSlot): Long {
    val now = nowMillis()
    if (slot == MealSlot.SNACK) return now
    return millisAt(today(), slot.hour, slot.minute).coerceAtMost(now)
}

/**
 * Today's timestamp of [part], or null when it cannot be used:
 * a part that has not started yet, or "after the meal" without a meal today.
 */
fun AppClock.timeOf(part: SymptomDayPart, lastMealAt: Long?): Long? {
    val now = nowMillis()
    return when (part) {
        SymptomDayPart.NOW -> now
        SymptomDayPart.AFTER_MEAL -> lastMealAt
            ?.takeIf { epochDayOf(it) == today() }
            ?.let { (it + SymptomDayPart.AFTER_MEAL_MINUTES * 60_000L).coerceAtMost(now) }
        else -> millisAt(today(), part.hour, part.minute).takeIf { it <= now }
    }
}
