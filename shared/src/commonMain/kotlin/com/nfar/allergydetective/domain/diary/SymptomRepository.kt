package com.nfar.allergydetective.domain.diary

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

    /** Undo of [saveEntry]. */
    suspend fun deleteEntry(entryId: Long)
}
