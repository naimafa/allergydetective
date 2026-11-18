package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.SickPeriodDao
import com.nfar.allergydetective.data.db.entity.SickPeriodEntity
import com.nfar.allergydetective.domain.diary.SickPeriod
import com.nfar.allergydetective.domain.diary.SickPeriodRepository

class RoomSickPeriodRepository(private val dao: SickPeriodDao) : SickPeriodRepository {
    override suspend fun periods(): List<SickPeriod> = dao.periods().map { SickPeriod(it.id, it.startDay, it.endDay) }

    override suspend fun add(startDay: Long, endDay: Long?): Long = dao.insert(SickPeriodEntity(startDay = startDay, endDay = endDay))

    override suspend fun setEnd(id: Long, endDay: Long?) = dao.setEnd(id, endDay)

    override suspend fun delete(id: Long) = dao.delete(id)
}
