package com.nfar.allergydetective.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.nfar.allergydetective.data.db.entity.SickPeriodEntity

@Dao
interface SickPeriodDao {
    @Query("SELECT * FROM sick_periods ORDER BY startDay DESC")
    suspend fun periods(): List<SickPeriodEntity>

    @Insert
    suspend fun insert(period: SickPeriodEntity): Long

    @Query("UPDATE sick_periods SET endDay = :endDay WHERE id = :id")
    suspend fun setEnd(id: Long, endDay: Long?)

    @Query("UPDATE sick_periods SET startDay = :startDay, endDay = :endDay WHERE id = :id")
    suspend fun update(id: Long, startDay: Long, endDay: Long?)

    @Query("DELETE FROM sick_periods WHERE id = :id")
    suspend fun delete(id: Long)
}
