package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.nfar.allergydetective.data.db.entity.SymptomEntity
import com.nfar.allergydetective.data.db.entity.SymptomEntryEntity

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

    @Query("DELETE FROM symptom_entries WHERE id = :entryId")
    suspend fun deleteEntry(entryId: Long)
}
