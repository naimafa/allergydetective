package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.nfar.allergydetective.data.db.entity.SymptomEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntryEntity
import com.nfar.allergydetective.data.db.model.IllnessRelationKind

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptoms ORDER BY id")
    suspend fun symptoms(): List<SymptomEntity>

    @Query(
        """
        SELECT symptomId FROM symptom_entries
        GROUP BY symptomId
        ORDER BY COUNT(*) DESC, MAX(startedAt) DESC
        LIMIT :limit
        """,
    )
    suspend fun mostUsedSymptomIds(limit: Int): List<Long>

    @Insert
    suspend fun insertEntry(entry: SymptomEntryEntity): Long

    @Query("SELECT * FROM symptom_entries WHERE id = :entryId")
    suspend fun entry(entryId: Long): SymptomEntryEntity?

    @Query("UPDATE symptom_entries SET startedAt = :startedAt, tzOffsetMin = :tzOffsetMin, intensity = :intensity, note = :note WHERE id = :entryId")
    suspend fun updateEntry(entryId: Long, startedAt: Long, tzOffsetMin: Int, intensity: Int, note: String?)

    @Query("SELECT MAX(eatenAt) FROM meals WHERE eatenAt <= :beforeMillis")
    suspend fun lastMealAt(beforeMillis: Long): Long?

    @Query("UPDATE symptom_entries SET illnessRelation = :relation WHERE id = :entryId")
    suspend fun setIllnessRelation(entryId: Long, relation: IllnessRelationKind?)

    /** In a period of illness or marked as sick in the day check (factor key "sick"). */
    @Query(
        """
        SELECT EXISTS(SELECT 1 FROM sick_periods WHERE startDay <= :epochDay AND (endDay IS NULL OR endDay >= :epochDay))
            OR EXISTS(SELECT 1 FROM day_factors df JOIN lifestyle_factors lf ON lf.id = df.factorId
                WHERE df.epochDay = :epochDay AND lf.`key` = 'sick')
        """,
    )
    suspend fun isSickDay(epochDay: Long): Boolean

    @Query("DELETE FROM symptom_entries WHERE id = :entryId")
    suspend fun deleteEntry(entryId: Long)
}
