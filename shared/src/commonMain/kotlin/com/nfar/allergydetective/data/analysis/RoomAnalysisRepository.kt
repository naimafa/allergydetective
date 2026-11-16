package com.nfar.allergydetective.data.analysis

import com.nfar.allergydetective.data.db.dao.AnalysisDao
import com.nfar.allergydetective.domain.analysis.AnalysisRepository
import com.nfar.allergydetective.domain.analysis.model.AnalysisInput

class RoomAnalysisRepository(private val dao: AnalysisDao) : AnalysisRepository {
    override suspend fun loadInput(now: Long, nowTzOffsetMin: Int): AnalysisInput =
        dao.loadAll().toAnalysisInput(now, nowTzOffsetMin)
}
