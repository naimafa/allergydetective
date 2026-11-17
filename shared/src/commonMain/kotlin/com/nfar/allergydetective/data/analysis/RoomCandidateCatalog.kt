package com.nfar.allergydetective.data.analysis

import com.nfar.allergydetective.data.db.dao.AnalysisDao
import com.nfar.allergydetective.data.db.dao.DayCheckDao
import com.nfar.allergydetective.data.db.dao.MealDao
import com.nfar.allergydetective.domain.analysis.CandidateCatalog
import com.nfar.allergydetective.domain.analysis.CandidateKeys
import com.nfar.allergydetective.domain.diary.IngredientInfo

class RoomCandidateCatalog(
    private val analysisDao: AnalysisDao,
    private val mealDao: MealDao,
    private val dayCheckDao: DayCheckDao,
) : CandidateCatalog {
    override suspend fun keys(): CandidateKeys = CandidateKeys(
        ingredients = mealDao.ingredients().associate { it.id to IngredientInfo(it.id, it.key, it.customName) },
        properties = analysisDao.properties().associate { it.id to it.key },
        factors = dayCheckDao.lifestyleFactors().associate { it.id to it.key },
    )
}
