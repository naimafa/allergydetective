package com.nfar.allergydetective.domain.analysis.model

/*
 * Input of the analysis engine (TRD 2). Plain domain types without Room annotations,
 * so the engine stays independent of the database. A repository maps the entities.
 * Timestamps are UTC epoch millis, days are local epoch days.
 */

data class AnalysisInput(
    val reference: ReferenceData,
    val diary: DiaryData,
    /** Point in time of the analysis (UTC epoch millis). Only closed windows before [now] count. */
    val now: Long,
    /** Offset of the device time zone at [now], used to find "today". */
    val nowTzOffsetMin: Int,
)

data class ReferenceData(
    val properties: List<TriggerProperty>,
    val ingredients: List<IngredientRef>,
    val symptoms: List<SymptomRef>,
    /** Known allergies (TRD 2.6): not ranked, reported separately. */
    val knownAllergyPropertyIds: Set<Long> = emptySet(),
)

/** Reaction window in minutes after eating, both ends inclusive. */
data class TimeWindow(val startMin: Int, val endMin: Int)

data class TriggerProperty(
    val id: Long,
    val windows: List<TimeWindow>,
    val symptomGroups: Set<String>,
    /** Destroyed by cooking (birch pollen cross-reaction): ignored for cooked items. */
    val heatLabile: Boolean = false,
)

data class IngredientRef(val id: Long, val propertyIds: Set<Long>)

data class SymptomRef(val id: Long, val groupKey: String)

data class DiaryData(
    val meals: List<Meal>,
    val symptomEntries: List<SymptomEntry>,
    val dayChecks: List<DayCheck>,
    val dayFactors: List<DayFactor>,
)

data class Meal(
    val id: Long,
    val eatenAt: Long,
    val tzOffsetMin: Int,
    val items: List<MealItem>,
)

/**
 * [preparation] is only set for ingredients where it matters (self-test 18.11.):
 * cooking destroys heat-labile triggers like the birch pollen cross-reaction.
 */
data class MealItem(val ingredientId: Long, val portion: Portion, val preparation: Preparation? = null)

enum class Preparation { RAW, COOKED }

/** Portion size (TRD 1.4). The analysis factor is configured in [com.nfar.allergydetective.domain.analysis.AnalysisConfig]. */
enum class Portion { SMALL, MEDIUM, LARGE }

data class SymptomEntry(
    val id: Long,
    val symptomId: Long,
    val startedAt: Long,
    val tzOffsetMin: Int,
    /** 1–5 */
    val intensity: Int,
)

/**
 * Evening check of a day. [confirmed] is true for NO_SYMPTOMS and COMPLETE (TRD 2.2).
 * Sleep values describe the night before this day.
 */
data class DayCheck(
    val epochDay: Long,
    val confirmed: Boolean,
    val sleepHours: Float?,
    val sleepQuality: Int?,
    val stress: Int?,
)

data class DayFactor(val epochDay: Long, val factorId: Long)
