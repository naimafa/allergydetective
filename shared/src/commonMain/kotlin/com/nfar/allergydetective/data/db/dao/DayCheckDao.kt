package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.nfar.allergydetective.data.db.entity.DayCheckEntity
import com.nfar.allergydetective.data.db.entity.DayFactorEntity
import com.nfar.allergydetective.data.db.entity.LifestyleFactorEntity

@Dao
interface DayCheckDao {
    @Query("SELECT * FROM day_checks WHERE epochDay = :epochDay")
    suspend fun dayCheck(epochDay: Long): DayCheckEntity?

    @Query("SELECT factorId FROM day_factors WHERE epochDay = :epochDay ORDER BY factorId")
    suspend fun factorIds(epochDay: Long): List<Long>

    @Query("SELECT * FROM lifestyle_factors ORDER BY id")
    suspend fun lifestyleFactors(): List<LifestyleFactorEntity>

    @Query("SELECT COUNT(*) FROM symptom_entries WHERE startedAt >= :fromMillis AND startedAt < :toMillis")
    suspend fun symptomCount(fromMillis: Long, toMillis: Long): Int

    @Query("SELECT epochDay FROM day_checks WHERE status != 'OPEN' AND epochDay IN (:days)")
    suspend fun confirmedDays(days: List<Long>): List<Long>

    @Query("SELECT COUNT(*) FROM day_checks WHERE status != 'OPEN'")
    suspend fun confirmedDayCount(): Int

    @Query("SELECT COUNT(*) FROM day_checks WHERE status != 'OPEN' AND epochDay >= :fromDay")
    suspend fun confirmedDayCountSince(fromDay: Long): Int

    @Upsert
    suspend fun upsertDayCheck(check: DayCheckEntity)

    @Query("DELETE FROM day_factors WHERE epochDay = :epochDay")
    suspend fun deleteFactors(epochDay: Long)

    @Insert
    suspend fun insertFactors(factors: List<DayFactorEntity>)

    @Transaction
    suspend fun save(check: DayCheckEntity, factors: List<DayFactorEntity>) {
        upsertDayCheck(check)
        deleteFactors(check.epochDay)
        insertFactors(factors)
    }
}
