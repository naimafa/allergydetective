package com.nfar.allergydetective.domain.analysis

import com.nfar.allergydetective.domain.diary.IngredientInfo

/** Stable keys for showing candidates by name. The UI turns the keys into display names. */
data class CandidateKeys(
    val ingredients: Map<Long, IngredientInfo>,
    val properties: Map<Long, String>,
    val factors: Map<Long, String>,
)

fun interface CandidateCatalog {
    suspend fun keys(): CandidateKeys
}
