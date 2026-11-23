package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.DayCheckDao
import com.nfar.allergydetective.data.db.entity.DayCheckEntity
import com.nfar.allergydetective.data.db.entity.DayFactorEntity
import com.nfar.allergydetective.data.db.model.DayStatus
import com.nfar.allergydetective.domain.diary.DayCheckData
import com.nfar.allergydetective.domain.diary.DayCheckRepository
import com.nfar.allergydetective.domain.diary.DayCheckStatus
import com.nfar.allergydetective.domain.diary.FactorInfo

class RoomDayCheckRepository(private val dao: DayCheckDao) : DayCheckRepository {

    override suspend fun load(epochDay: Long): DayCheckData? {
        val check = dao.dayCheck(epochDay) ?: return null
        return DayCheckData(
            epochDay = check.epochDay,
            wellbeing = check.wellbeing,
            sleepHours = check.sleepHours,
            sleepQuality = check.sleepQuality,
            stress = check.stress,
            status = check.status.toDomain(),
            factorIds = dao.factorIds(epochDay).toSet(),
        )
    }

    override suspend fun lifestyleFactors(): List<FactorInfo> = dao.lifestyleFactors().map { FactorInfo(it.id, it.key) }

    override suspend fun symptomCount(fromMillis: Long, toMillis: Long): Int = dao.symptomCount(fromMillis, toMillis)

    override suspend fun save(check: DayCheckData) {
        dao.save(
            check = DayCheckEntity(
                epochDay = check.epochDay,
                wellbeing = check.wellbeing,
                sleepHours = check.sleepHours,
                sleepQuality = check.sleepQuality,
                stress = check.stress,
                status = check.status.toEntity(),
            ),
            factors = check.factorIds.sorted().map { DayFactorEntity(check.epochDay, it) },
        )
    }

    override suspend fun confirmedDays(days: List<Long>): Set<Long> = dao.confirmedDays(days).toSet()

    override suspend fun confirmedDayCount(): Int = dao.confirmedDayCount()
    override suspend fun confirmedDayCountSince(fromDay: Long): Int = dao.confirmedDayCountSince(fromDay)
}

private fun DayStatus.toDomain(): DayCheckStatus = when (this) {
    DayStatus.OPEN -> DayCheckStatus.OPEN
    DayStatus.NO_SYMPTOMS -> DayCheckStatus.NO_SYMPTOMS
    DayStatus.COMPLETE -> DayCheckStatus.COMPLETE
}

private fun DayCheckStatus.toEntity(): DayStatus = when (this) {
    DayCheckStatus.OPEN -> DayStatus.OPEN
    DayCheckStatus.NO_SYMPTOMS -> DayStatus.NO_SYMPTOMS
    DayCheckStatus.COMPLETE -> DayStatus.COMPLETE
}
