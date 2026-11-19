package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.SymptomDao
import com.nfar.allergydetective.data.analysis.toEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntryEntity
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
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

    override suspend fun isSickDay(epochDay: Long): Boolean = dao.isSickDay(epochDay)
}
