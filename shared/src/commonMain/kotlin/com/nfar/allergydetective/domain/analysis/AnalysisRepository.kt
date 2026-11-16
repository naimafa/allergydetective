package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.analysis.model.AnalysisInput

/** Provides the diary and reference data for one analysis run. The domain layer only knows this interface. */
interface AnalysisRepository {
    /**
     * @param now point in time of the analysis (UTC epoch millis)
     * @param nowTzOffsetMin offset of the device time zone at [now]
     */
    suspend fun loadInput(now: Long, nowTzOffsetMin: Int): AnalysisInput
}
