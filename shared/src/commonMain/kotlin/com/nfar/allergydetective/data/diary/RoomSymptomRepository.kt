package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.SymptomDao
import com.nfar.allergydetective.data.analysis.toDomain
import com.nfar.allergydetective.data.analysis.toEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntryEntity
import com.nfar.allergydetective.data.db.model.DurationKind
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import com.nfar.allergydetective.domain.diary.LoggedSymptom
import com.nfar.allergydetective.domain.diary.SymptomDuration
import com.nfar.allergydetective.domain.diary.SymptomInfo
import com.nfar.allergydetective.domain.diary.SymptomRepository

class RoomSymptomRepository(private val dao: SymptomDao) : SymptomRepository {

    override suspend fun symptoms(): List<SymptomInfo> =
        dao.symptoms().map { SymptomInfo(it.id, it.key, it.groupKey, it.isRedFlag) }

    override suspend fun mostUsedSymptomIds(limit: Int): List<Long> = dao.mostUsedSymptomIds(limit)

    override suspend fun saveEntry(symptomId: Long, startedAt: Long, tzOffsetMin: Int, intensity: Int, note: String?): Long =
        dao.insertEntry(
            SymptomEntryEntity(
                symptomId = symptomId,
                startedAt = startedAt,
                tzOffsetMin = tzOffsetMin,
                intensity = intensity,
                note = note,
            ),
        )

    override suspend fun lastMealAt(beforeMillis: Long): Long? = dao.lastMealAt(beforeMillis)

    override suspend fun deleteEntry(entryId: Long) = dao.deleteEntry(entryId)

    override suspend fun setIllnessRelation(entryId: Long, relation: IllnessRelation?) =
        dao.setIllnessRelation(entryId, relation?.toEntity())

    override suspend fun setDuration(entryId: Long, duration: SymptomDuration?) =
        dao.setDuration(entryId, duration?.toEntity())

    override suspend fun isSickDay(epochDay: Long): Boolean = dao.isSickDay(epochDay)

    override suspend fun entry(entryId: Long): LoggedSymptom? = dao.entry(entryId)?.let {
        LoggedSymptom(it.id, it.symptomId, it.startedAt, it.intensity, it.note, it.illnessRelation?.toDomain(), it.duration?.toDomain())
    }

    override suspend fun updateEntry(entryId: Long, startedAt: Long, tzOffsetMin: Int, intensity: Int, note: String?) =
        dao.updateEntry(entryId, startedAt, tzOffsetMin, intensity, note)
}

internal fun DurationKind.toDomain(): SymptomDuration = when (this) {
    DurationKind.UNDER_1H -> SymptomDuration.UNDER_1H
    DurationKind.H1_TO_3 -> SymptomDuration.H1_TO_3
    DurationKind.H3_TO_6 -> SymptomDuration.H3_TO_6
    DurationKind.H6_TO_12 -> SymptomDuration.H6_TO_12
    DurationKind.H3_TO_12 -> SymptomDuration.H3_TO_12
    DurationKind.OVER_12H -> SymptomDuration.OVER_12H
}

internal fun SymptomDuration.toEntity(): DurationKind = when (this) {
    SymptomDuration.UNDER_1H -> DurationKind.UNDER_1H
    SymptomDuration.H1_TO_3 -> DurationKind.H1_TO_3
    SymptomDuration.H3_TO_6 -> DurationKind.H3_TO_6
    SymptomDuration.H6_TO_12 -> DurationKind.H6_TO_12
    SymptomDuration.H3_TO_12 -> DurationKind.H3_TO_12
    SymptomDuration.OVER_12H -> DurationKind.OVER_12H
}
