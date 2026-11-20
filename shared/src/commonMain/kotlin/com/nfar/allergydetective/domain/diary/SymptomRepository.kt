package com.nfar.allergydetective.domain.diary

import com.nfar.allergydetective.domain.analysis.model.IllnessRelation

/** A symptom of the catalog (CONTENT_Symptome 1). */
data class SymptomInfo(
    val id: Long,
    val key: String,
    val groupKey: String,
    /** Emergency symptom (swelling, shortness of breath): a notice comes first (TRD 3.4). */
    val isRedFlag: Boolean,
)

/** Write and read access for reporting symptoms (UX 3.3). */
interface SymptomRepository {
    suspend fun symptoms(): List<SymptomInfo>

    /** Most often reported symptoms, shown on top of the grid (CONTENT_Symptome 5.2). */
    suspend fun mostUsedSymptomIds(limit: Int): List<Long>

    suspend fun saveEntry(symptomId: Long, startedAt: Long, tzOffsetMin: Int, intensity: Int, note: String?): Long

    /** Time of the latest meal at or before [beforeMillis], for "after the meal". */
    suspend fun lastMealAt(beforeMillis: Long): Long?

    /** Undo of [saveEntry]. */
    suspend fun deleteEntry(entryId: Long)

    /** On a sick day: does the complaint belong to the illness (self-test 19.11.)? Null = not answered. */
    suspend fun setIllnessRelation(entryId: Long, relation: IllnessRelation?) = Unit

    /** How long the complaint lasted (feedback 20.11.); null = not given. */
    suspend fun setDuration(entryId: Long, duration: SymptomDuration?) = Unit

    /** A logged complaint, for correcting it from the history (feedback 19.11.). */
    suspend fun entry(entryId: Long): LoggedSymptom? = null

    /** Corrects time, intensity and note; the symptom itself stays. */
    suspend fun updateEntry(entryId: Long, startedAt: Long, tzOffsetMin: Int, intensity: Int, note: String?) = Unit

    /** In a period of illness or marked as sick in the day check. */
    suspend fun isSickDay(epochDay: Long): Boolean = false
}

data class LoggedSymptom(
    val id: Long,
    val symptomId: Long,
    val startedAt: Long,
    val intensity: Int,
    val note: String?,
    val illnessRelation: IllnessRelation?,
    val duration: SymptomDuration? = null,
)

/** How long a complaint lasted, optional (feedback 20.11.). Shown in the history and the report for the doctor. */
enum class SymptomDuration { UNDER_1H, H1_TO_3, H3_TO_12, OVER_12H }
