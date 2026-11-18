package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.entity.IngredientEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class DuplicateIngredientMergerTest {

    private fun seed(id: Long, key: String) = IngredientEntity(id, key, null, 1, isCustom = false, baseIngredientId = null)
    private fun custom(id: Long, name: String) = IngredientEntity(id, null, name, 1, isCustom = true, baseIngredientId = 104)

    @Test
    fun customIngredientWithTheNameOfASeedIngredientIsMerged() {
        // Self-test 18.11.: "Dinkelvollkornbrot" was created by hand before it shipped as id 114.
        val seedByName = mapOf("dinkelvollkornbrot" to seed(114, "spelt_wholemeal_bread"))

        val pairs = DuplicateIngredientMerger.duplicatePairs(
            listOf(custom(100_000, "Dinkelvollkornbrot "), custom(100_001, "Dinkelbrötchen vom Bäcker")),
            seedByName,
        )

        assertEquals(listOf(100_000L to 114L), pairs)
    }
}
