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
 * User 25.11.2026: also one part of a name with a slash - "Kekse" goes into "Kekse / Plätzchen".
 */
class DuplicateIngredientMerger(
    private val dao: IngredientDao,
    private val names: DisplayNameResolver,
) {
    /** Returns the number of merged ingredients. */
    suspend fun mergeIntoSeed(): Int {
        val ingredients = dao.allIngredients()
        val seed = ingredients.filter { !it.isCustom && it.key != null }
        val seedByName = seedByName(seed.map { names.name(NamePrefix.INGREDIENT, it.key!!) to it })
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

        /**
         * Normalized full names, plus each part of a name like "Kekse / Plätzchen". A part that belongs to
         * more than one seed ingredient is left out (it is not clear which one is meant); full names win.
         */
        fun seedByName(named: List<Pair<String, IngredientEntity>>): Map<String, IngredientEntity> {
            val full = named.associate { (name, entity) -> normalizeForSearch(name) to entity }
            val parts = named.flatMap { (name, entity) ->
                name.split("/").map { normalizeForSearch(it) }.filter { it.isNotBlank() }.map { it to entity }
            }.groupBy({ it.first }, { it.second })
                .filterValues { entities -> entities.distinctBy { it.id }.size == 1 }
                .mapValues { it.value.first() }
            return parts + full
        }
    }
}
