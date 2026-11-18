package com.nfar.allergydetective.data.diary

import com.nfar.allergydetective.data.db.dao.IngredientDao
import com.nfar.allergydetective.data.db.entity.IngredientEntity
import com.nfar.allergydetective.domain.diary.normalizeForSearch
import com.nfar.allergydetective.ui.common.DisplayNameResolver
import com.nfar.allergydetective.ui.common.NamePrefix

/**
 * Self-test 18.11.: a custom ingredient created before the app shipped the same one
 * ("Dinkelvollkornbrot") splits the diary in two. On start, every custom ingredient whose
 * name equals the name of a seed ingredient is merged into it (ENTSCHEIDUNGEN 2).
 */
class DuplicateIngredientMerger(
    private val dao: IngredientDao,
    private val names: DisplayNameResolver,
) {
    /** Returns the number of merged ingredients. */
    suspend fun mergeIntoSeed(): Int {
        val ingredients = dao.allIngredients()
        val seedByName = ingredients.filter { !it.isCustom && it.key != null }
            .associateBy { normalizeForSearch(names.name(NamePrefix.INGREDIENT, it.key!!)) }
        val pairs = duplicatePairs(ingredients.filter { it.isCustom }, seedByName)
        pairs.forEach { (from, to) -> dao.mergeCustomInto(from, to) }
        return pairs.size
    }

    companion object {
        /** (custom id, seed id) for every custom ingredient with the name of a seed ingredient. */
        fun duplicatePairs(custom: List<IngredientEntity>, seedByName: Map<String, IngredientEntity>): List<Pair<Long, Long>> =
            custom.mapNotNull { ingredient ->
                val name = normalizeForSearch(ingredient.customName.orEmpty())
                seedByName[name]?.let { ingredient.id to it.id }
            }
    }
}
